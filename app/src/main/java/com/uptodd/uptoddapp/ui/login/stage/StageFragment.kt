package com.uptodd.uptoddapp.ui.login.stage

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.UptoddViewModelFactory
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.databinding.FragmentStageBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.todoScreens.TodosListActivity
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.UpToddDialogs

class StageFragment : Fragment() {

    lateinit var binding: FragmentStageBinding
    lateinit var viewModel: StageViewModel

    var stage: String = "pre birth"

    var preferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_stage, container, false)
        binding.lifecycleOwner = this

        viewModel = ViewModelProvider(this).get(StageViewModel::class.java)

        preferences = activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        editor = preferences!!.edit()

        viewModel.uid = preferences!!.getString("uid", "")
        viewModel.loginMethod = preferences!!.getString("loginMethod", "google")
        viewModel.parentType = preferences!!.getString("parentType", "mother")
        Log.d(
            "div",
            "StageFragment L49 ${viewModel.uid} ${viewModel.loginMethod} ${viewModel.parentType} "
        )

        binding.imageButtonBorn.setOnClickListener {
            editor?.putString("stage", "pre birth")
            editor?.commit()
            viewModel.stage="postnatal"
            UptoddSharedPreferences.getInstance(requireContext()).saveStage("postnatal")
           showLoadingDialog()
            viewModel.insertLoginDetails()
        }
        binding.imageButtonPregnant.setOnClickListener {
            if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
                UptoddSharedPreferences.getInstance(requireContext()).saveStage("prenatal")
                viewModel.stage="prenatal"
                    viewModel.isLoadingDialogVisible.value = true
                    showLoadingDialog()
                    viewModel.insertLoginDetails()
                //viewModel.getLoginDetails()
            } else {
                //showInternetNotConnectedDialog()
                Snackbar.make(
                    binding.layout,
                    getString(R.string.no_internet_connection),
                    Snackbar.LENGTH_LONG
                ).show()
            }

        }

        return binding.root
    }

    private fun showInternetNotConnectedDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_upload,
            getString(R.string.no_internet_connection),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }
            })
    }

    private fun showLoadingDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_upload,
            getString(R.string.loading_please_wait),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }
            })
        viewModel.isLoadingDialogVisible.observe(viewLifecycleOwner, Observer {
            if (!it) {
                upToddDialogs.dismissDialog()
                if (viewModel.isDataLoadedToDatabase) {
                    editor?.putBoolean("loggedIn", true)
                    editor?.putBoolean("isNewUser", false)
                    editor?.commit()
                    if(UptoddSharedPreferences.getInstance(requireContext()).getUserType()=="nonPremium")
                    {
                        view?.findNavController()?.navigate(R.id.action_stageFragment_to_nameFragment)
                    }
                    else {
                        //view?.findNavController()?.navigate(R.id.action_stageFragment_to_homeFragment)
                        startActivity(Intent(activity, TodosListActivity::class.java))
                        activity?.finish()
                    }
                } else {
                    Toast.makeText(
                        activity,
                        getString(R.string.failed_to_load_to_database),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
        val handler = Handler()
        handler.postDelayed({
            upToddDialogs.dismissDialog()
        }, R.string.loadingDuarationInMillis.toLong())

    }

}