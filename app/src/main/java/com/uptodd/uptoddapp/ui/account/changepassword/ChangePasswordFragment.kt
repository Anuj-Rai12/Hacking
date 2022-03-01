package com.uptodd.uptoddapp.ui.account.changepassword

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FragmentChangePasswordBinding
import com.uptodd.uptoddapp.utilities.*

class ChangePasswordFragment : Fragment() {

    private lateinit var binding:FragmentChangePasswordBinding
    private lateinit var viewModel:ChangePasswordViewModel

    var preferences: SharedPreferences? = null

    var isDoctor = false

    private var newPassword:String?=null
    private var confirmPassword:String?=null

    private var currentPasswordEye=false            //true=visible ; false=invisible
    private var newPasswordEye=false               //true=visible ; false=invisible
    private var confirmPasswordEye=false           //true=visible ; false=invisible

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ChangeLanguage(requireContext()).setLanguage()

        binding=DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_change_password,
            container,
            false
        )
        binding.lifecycleOwner=this

        viewModel=ViewModelProvider(this).get(ChangePasswordViewModel::class.java)

        preferences = activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        if(preferences!!.contains("uid"))
            viewModel.userId=preferences!!.getString("uid","")
        if(preferences!!.contains("token"))
            viewModel.token= preferences!!.getString("token","")

        val args = ChangePasswordFragmentArgs.fromBundle(requireArguments())
        isDoctor = args.doctorReset

        (requireActivity() as AppCompatActivity?)?.supportActionBar?.title=getString(R.string.change_password)
        (requireActivity() as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        binding.toolbar?.let {
            ToolbarUtils.initNCToolbar(requireActivity(),"Change Password", it,
                findNavController())
        }

        binding.editTextCurrentPassword.transformationMethod = MyPasswordTransformationMethod()
        binding.editTextNewPassword.transformationMethod = MyPasswordTransformationMethod()
        binding.editTextConfirmPassword.transformationMethod = MyPasswordTransformationMethod()

        binding.buttonChangePassword.setOnClickListener { changePassword()}
        binding.imageButtonCurrentPasswordEye.setOnClickListener{currentPasswordEyeToggle()}
        binding.imageButtonNewPasswordEye.setOnClickListener{newPasswordEyeToggle()}
        binding.imageButtonConfirmPasswordEye.setOnClickListener{confirmPasswordEyeToggle()}


        return binding.root
    }

    private fun newPasswordEyeToggle() {
        newPasswordEye=!newPasswordEye
        if (newPasswordEye)
            binding.editTextNewPassword.transformationMethod = null
        else
            binding.editTextNewPassword.transformationMethod = MyPasswordTransformationMethod()
    }

    private fun confirmPasswordEyeToggle() {
        confirmPasswordEye=!confirmPasswordEye
        if (confirmPasswordEye)
            binding.editTextConfirmPassword.transformationMethod = null
        else
            binding.editTextConfirmPassword.transformationMethod = MyPasswordTransformationMethod()
    }

    private fun currentPasswordEyeToggle() {
        currentPasswordEye=!currentPasswordEye
        if (currentPasswordEye)
            binding.editTextCurrentPassword.transformationMethod = null
        else
            binding.editTextCurrentPassword.transformationMethod = MyPasswordTransformationMethod()
    }

    private fun changePassword() {
        if(binding.editTextCurrentPassword.text.isEmpty())
            binding.textViewPasswordError.text=getString(R.string.enter_current_password)
        else if(binding.editTextNewPassword.text.isEmpty())
            binding.textViewPasswordError.text=getString(R.string.enter_new_password)
        else if(!checkValidPassword(binding.editTextNewPassword.text.toString()))
            binding.textViewPasswordError.text=getString(R.string.enter_valid_password)
        else if(binding.editTextNewPassword.text.toString()!=binding.editTextConfirmPassword.text.toString())
            binding.textViewPasswordError.text=getString(R.string.passwords_dont_match)
       // else if(!viewModel.checkCurrentPassword(binding.editTextCurrentPassword.text.toString()))
         //   binding.textViewPasswordError.text="Wrong Current Password"



        else
        {
            if(AppNetworkStatus.getInstance(requireContext()).isOnline)
            {
                showUploadingDialog()
                viewModel.changePassword(binding.editTextNewPassword.text.toString(),binding.editTextCurrentPassword.text.toString(), isDoctor)
                viewModel.isUpdatingPassword.observe(viewLifecycleOwner, Observer {
                    if (!it) {
                        if (viewModel.isPasswordUpdated) {
                            UpToddDialogs(requireContext()).showDialog(R.drawable.gif_done,
                                getString(R.string.password_updated_successfully), getString(R.string.close),
                                object : UpToddDialogs.UpToddDialogListener {
                                    override fun onDialogButtonClicked(dialog: Dialog) {
                                        dialog.dismiss()
                                        findNavController().navigateUp()
                                    }
                                })
                        }
                    } else {
                        //showInternetNotConnectedDialog()
                        val snackbar = Snackbar.make(binding.layout, viewModel.errorInChangePassword, Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.retry)) {
                                changePassword()
                            }
                        snackbar.show()
                    }
                })
            }
            else {
                //showInternetNotConnectedDialog()
                val snackbar = Snackbar.make(binding.layout,
                    getString(R.string.no_internet_connection),
                    Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.retry)) {
                        changePassword()
                    }
                snackbar.show()
            }
        }
    }

    private fun checkValidPassword(password: String): Boolean {
        //Enter conditions for valid password
        var isValid=true
        var isNumberPresent=false
        var isSpecialCharacterPresent=false
        for(c in password)
        {
            if(c.isDigit())
                isNumberPresent=true
            else if(!c.isLetterOrDigit())
                isSpecialCharacterPresent=true
            if(isNumberPresent && isSpecialCharacterPresent)
                break
        }

        if(password.length<8)
        {
            binding.textViewPasswordError.text=getString(R.string.password_too_short)
            isValid=false
        }
        else if(!isNumberPresent)
        {
            binding.textViewPasswordError.text=getString(R.string.password_must_have_a_digit)
            isValid=false
        }
        else if(!isSpecialCharacterPresent)
        {
            binding.textViewPasswordError.text=getString(R.string.the_password_must_have_a_special_character)
            isValid=false
        }
        return isValid
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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


    private fun showUploadingDialog() {
        val upToddDialogs = UpToddDialogs(requireContext())
        upToddDialogs.showDialog(R.drawable.gif_upload,
            getString(R.string.changing_password_please_wait),
            getString(R.string.back),
            object : UpToddDialogs.UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }
            })
        viewModel.isUpdatingPassword.observe(viewLifecycleOwner, Observer {
            if(!it)
            {
                upToddDialogs.dismissDialog()
            }
        })
        val handler= Handler()
        handler.postDelayed({
            upToddDialogs.dismissDialog()
        },R.string.loadingDuarationInMillis.toLong())

    }
}