package com.uptodd.uptoddapp.media.resources

import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.media.resource.ResourceFiles
import com.uptodd.uptoddapp.databinding.ResourceFragmentBinding
import com.uptodd.uptoddapp.media.poem.PoemAdapter
import com.uptodd.uptoddapp.media.poem.PoemViewModel
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import com.uptodd.uptoddapp.utilities.downloadmanager.JishnuDownloadManager
import java.io.File

class ResourceFragment:Fragment(), ResourceAdapterInterface {

    var binding:ResourceFragmentBinding?=null
    private lateinit var uptoddDialogs: UpToddDialogs
    private lateinit var viewModel: ResourceViewModel
    private lateinit var preferences: SharedPreferences
    private val adapter = ResourceAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fadeThrough = MaterialFadeThrough().apply {
            duration = 1000
        }

        enterTransition = fadeThrough
        reenterTransition = fadeThrough

        binding=DataBindingUtil.inflate(inflater, R.layout.resource_fragment,container,false)
        viewModel=ViewModelProvider(this)[ResourceViewModel::class.java]
        uptoddDialogs = UpToddDialogs(requireContext())
        binding?.resourceListRecyclerView?.adapter=adapter
        viewModel.getAllResources()
        viewModel.isLoading.observe(viewLifecycleOwner, Observer {

            when(it) {
                1 -> {
                    uptoddDialogs.showLoadingDialog(findNavController())
                }
                0 -> {
                    uptoddDialogs.dismissDialog()
                }
                -1 -> {
                    uptoddDialogs.dismissDialog()
                    uptoddDialogs.showDialog(R.drawable.network_error,
                        getString(R.string.an_error_has_occurred),
                        getString(R.string.close),
                        object : UpToddDialogs.UpToddDialogListener {
                            override fun onDialogButtonClicked(dialog: Dialog) {
                                uptoddDialogs.dismissDialog()
                                findNavController().navigateUp()
                            }
                        })
                }
                else -> {

                }
            }
        })
        viewModel.resources.observe(viewLifecycleOwner, Observer {
            adapter.updateList(it)
        })

        binding?.resourceRefresh?.setOnRefreshListener {
            viewModel.getAllResources()
            binding?.resourceRefresh?.isRefreshing=false
        }
        return binding?.root
    }

    private fun downloadGuidelinesPdf(url:String,name:String) {
        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
            JishnuDownloadManager(
                url,
                name,
                File(
                    requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "Downloads"
                ),
                requireContext(),
                requireActivity()
            )
        } else {
            val snackbar = binding?.resourceConstraint?.let {
                Snackbar.make(
                    it,
                    getString(R.string.no_internet_connection),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(getString(R.string.retry)) {
                        downloadGuidelinesPdf(url,name)
                    }
            }
            snackbar?.show()
        }

    }

    override fun onClickPoem(resourceFiles: ResourceFiles) {
        resourceFiles.name?.let {
            resourceFiles.name?.let { AllUtil.getResourceUrl(it) }?.let { it1 ->
                downloadGuidelinesPdf(
                    it1,
                    it
                )
            }
        }
    }
}