package com.uptodd.uptoddapp.ui.login.babyname

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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.logindetails.UserInfo
import com.uptodd.uptoddapp.databinding.FragmentBabyNameBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.login.selectaddress.AddressViewModel
import com.uptodd.uptoddapp.ui.todoScreens.TodosListActivity
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.UpToddDialogs

class BabyNameFragment : Fragment() {

    private lateinit var binding: FragmentBabyNameBinding
    private lateinit var viewModel: BabyNameViewModel

    var preferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_baby_name, container, false)
        binding.lifecycleOwner = this


        viewModel = ViewModelProvider(this).get(BabyNameViewModel::class.java)

        preferences = activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        editor = preferences!!.edit()

        viewModel.uid = preferences!!.getString("uid", "")
        viewModel.loginMethod = preferences!!.getString("loginMethod", "google")
        viewModel.parentType = preferences!!.getString("parentType", "mother")
        viewModel.babyGender = preferences!!.getString("gender", "girl")
        viewModel.email = preferences!!.getString("email", "")
        viewModel.stage=UptoddSharedPreferences.getInstance(requireContext()).getStage()

        binding.buttonNext.setOnClickListener { onClickStart() }
        binding.buttonArrow.setOnClickListener { onClickStart() }

        return binding.root
    }

    private fun onClickStart() {
        if (binding.editTextName.text.isEmpty())
            Toast.makeText(activity, getString(R.string.enter_valid_name), Toast.LENGTH_LONG).show()
        else {
            if (AppNetworkStatus.getInstance(requireContext()).isOnline) {

                var address = UptoddSharedPreferences.getInstance(requireContext())
                    .getAddress(requireContext())

                preferences?.edit()?.putString(UserInfo::babyName.name, binding.editTextName.text.toString())?.apply()

                if ((address == "null" || address == "" || address == null ) && !AllUtil.isRow(requireContext()))
                {
                    AddressViewModel.isGenderName=true
                    findNavController()?.navigate(R.id.action_babyNameFragment_to_addressFragment)
                }
                else
                {
                    viewModel.isLoadingDialogVisible.value = true
                    showLoadingDialog()
                    viewModel.babyName = binding.editTextName.text.toString()
                    viewModel.insertLoginDetails()
                }
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
                    editor?.putString("babyName", viewModel.babyName)
                    editor?.commit()

                    var stage = UptoddSharedPreferences.getInstance(requireContext()).getStage()

                    val kidsDOB = preferences?.getString("kidsDob", "")

                    if ((stage == "postnatal" || stage == "post birth") && !AllUtil.isRow(
                            requireContext()
                        ) && (kidsDOB == "null" || kidsDOB == null || kidsDOB == "")
                    ) {
                        findNavController()?.navigate(R.id.action_babyNameFragment_to_dobFragment)
                    }
                    else {
                        preferences?.edit()?.putBoolean(UserInfo::isNewUser.name,false)?.apply()
                        //view?.findNavController()?.navigate(BabyNameFragmentDirections.actionBabyNameFragmentToHomeFragment())
                        startActivity(Intent(activity, TodosListActivity::class.java))
                        activity?.finishAffinity()
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