package com.uptodd.uptoddapp.ui.login.nonpremiumform

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FragmentNonpremiumMajorBinding
import com.uptodd.uptoddapp.ui.todoScreens.TodosListActivity
import com.uptodd.uptoddapp.utilities.UpToddDialogs

class MajorFragment : Fragment() {

    lateinit var binding: FragmentNonpremiumMajorBinding
    var viewModel:BirthViewModel?=null
    var editor: SharedPreferences.Editor? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentNonpremiumMajorBinding.inflate(inflater,container,false)
        binding.buttonNext.setOnClickListener {
                viewModel?.putObjective(binding.editTextMajor.text.toString())
                viewModel?.initialSetup(requireContext())
                showLoadingDialog()
        }
        val preferences = activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        editor = preferences!!.edit()
        viewModel= ViewModelProvider(this)[BirthViewModel::class.java]
        return binding.root
    }


    private fun showLoadingDialog() {

        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showSetUpsDialog(
            R.drawable.gif_upload,
            getString(R.string.loading_please_wait),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {

                }

                override fun onDialogDismiss() {
                    if (viewModel?.isDataLoadedToDatabase!!) {
                        editor?.putBoolean("loggedIn", true)
                        editor?.putBoolean("isNewUser", false)
                        editor?.commit()
                        //view?.findNavController()?.navigate(R.id.action_stageFragment_to_homeFragment)
                        startActivity(Intent(activity, TodosListActivity::class.java))
                        activity?.finish()
                    } else {
                        Toast.makeText(
                            activity,
                            getString(R.string.failed_to_load_to_database),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
        viewModel?.isLoadingDialogVisible?.observe(viewLifecycleOwner, Observer {
            if (!it) {
            }
        })
        val handler = Handler()
        handler.postDelayed({
            upToddDialogs.dismissDialog()
        }, R.string.loadingDuarationInMillis.toLong())

    }

}