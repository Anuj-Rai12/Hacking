package com.uptodd.uptoddapp.media.resources

import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.media.resource.ResourceFiles
import com.uptodd.uptoddapp.databinding.ResourceFragmentBinding
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.ToolbarUtils
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import com.uptodd.uptoddapp.utilities.downloadmanager.JishnuDownloadManager

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

        binding?.toolbar?.let { ToolbarUtils.initNCToolbar(requireActivity(),"Resources", it,
        findNavController()) }

        viewModel=ViewModelProvider(this)[ResourceViewModel::class.java]
        uptoddDialogs = UpToddDialogs(requireContext())
        binding?.resourceListRecyclerView?.adapter=adapter
        viewModel.getAllResources(requireContext())

        if(AllUtil.isUserPremium(requireContext()))
        {
            if(!AllUtil.isSubscriptionOverActive(requireContext()))
            {
                binding?.materialButton?.visibility= View.GONE
            }
        }
        binding?.materialButton?.setOnClickListener {

            it.findNavController().navigate(R.id.action_resourcesFragment_to_upgradeFragment)
        }
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
            viewModel.getAllResources(requireContext())
            binding?.resourceRefresh?.isRefreshing=false
        }
        return binding?.root
    }

    private fun downloadGuidelinesPdf(url:String,name:String) {
        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
            requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.let {
                JishnuDownloadManager(
                    url,
                    name,
                    it, requireContext(),
                    requireActivity()
                )
            }
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
        resourceFiles.link?.let {
            resourceFiles.link?.let { AllUtil.getResourceUrl(it) }?.let { it1 ->

                Log.d("ok","$it1")
                downloadGuidelinesPdf(
                    it1,
                    it
                )
            }
        }
    }
}