package com.uptodd.uptoddapp.ui.login.forgetpassword.resetpassword

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FragmentResetPasswordBinding
import com.uptodd.uptoddapp.utilities.UpToddDialogs

class ResetPasswordFragment : Fragment() {

    private lateinit var binding: FragmentResetPasswordBinding
    private lateinit var viewModel: ResetPasswordViewModel

    private lateinit var email: String
    private var userID: Long = -1L

    private var isDoctorReset = false

    private var newPassword: String? = null
    private var confirmPassword: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_reset_password,
            container,
            false
        )
        binding.lifecycleOwner = this

        viewModel = ViewModelProvider(this).get(ResetPasswordViewModel::class.java)

        binding.resetViewModel = viewModel
        val args = ResetPasswordFragmentArgs.fromBundle(requireArguments())
        email = args.email
        isDoctorReset = args.isDoctorReset
        viewModel.setArguments(email, isDoctorReset)


        setupObservers()

        binding.newPassword?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                binding.resetNewPassword?.isErrorEnabled = false
                binding.resetNewPassword?.isPasswordVisibilityToggleEnabled = true
            }
        })

        binding.confirmPassword?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                binding.resetConfirmPassword?.isErrorEnabled = false
                binding.resetConfirmPassword?.isPasswordVisibilityToggleEnabled = true
            }
        })


        // click listener for back button
        binding.goBack?.setOnClickListener {
            findNavController().navigateUp()
        }


        return binding.root
    }

    private fun setupObservers() {
        viewModel.newPasswordError.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.resetNewPassword?.error = viewModel.newPasswordMsg
                binding.resetNewPassword?.isPasswordVisibilityToggleEnabled = false
                viewModel.newPasswordError.value = false
            }
        })

        viewModel.confirmPasswordError.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.resetConfirmPassword?.error = viewModel.confirmPasswordMsg
                binding.resetConfirmPassword?.isPasswordVisibilityToggleEnabled = false
                viewModel.confirmPasswordError.value = false
            }
        })

        viewModel.passwordMisMatch.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.resetConfirmPassword?.isErrorEnabled = true
                binding.resetConfirmPassword?.error = "Password do not match"
                binding.resetConfirmPassword?.isPasswordVisibilityToggleEnabled = false
                viewModel.passwordMisMatch.value = false
            }
        })

        viewModel.passwordUpdateComplete.observe(viewLifecycleOwner, Observer {
            if (it) {
                UpToddDialogs(requireContext()).showDialog(
                    R.drawable.app_icon,
                    "Password updated successfully",
                    "OK",
                    object : UpToddDialogs.UpToddDialogListener {
                        override fun onDialogButtonClicked(dialog: Dialog) {
                            dialog.dismiss()
                            findNavController().navigateUp()
                        }
                    })
            }
        })

        viewModel.showErrorDailog.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrBlank()) return@Observer

            UpToddDialogs(requireContext()).showDialog(
                R.drawable.network_error,
                it,
                "OK",
                object : UpToddDialogs.UpToddDialogListener {
                    override fun onDialogButtonClicked(dialog: Dialog) {
                        dialog.dismiss()
                    }
                })
        })
    }

//    private fun newPasswordEyeToggle() {
//        newPasswordEye = !newPasswordEye
//        if (newPasswordEye)
//            binding.editTextNewPassword.transformationMethod = null
//        else
//            binding.editTextNewPassword.transformationMethod =
//                MyPasswordTransformationMethod()
//    }
//
//    private fun confirmPasswordEyeToggle() {
//        confirmPasswordEye = !confirmPasswordEye
//        if (confirmPasswordEye)
//            binding.editTextConfirmPassword.transformationMethod = null
//        else
//            binding.editTextConfirmPassword.transformationMethod =
//                MyPasswordTransformationMethod()
//    }

//    private fun changePassword() {
//        if (binding.editTextNewPassword.text.isEmpty())
//            binding.textViewPasswordError.text = getString(R.string.enter_new_password)
//        else if (!checkValidPassword(binding.editTextNewPassword.text.toString()))
//            binding.textViewPasswordError.text = getString(R.string.weak_password)
//        else if (binding.editTextNewPassword.text.toString() != binding.editTextConfirmPassword.text.toString())
//            binding.textViewPasswordError.text = getString(R.string.passwords_dont_match)
//        else {
//            if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
//                viewModel.isUpdatingPassword.value = true
//                viewModel.isPasswordUpdated = false
//                showUploadingDialog()
//                viewModel.changePassword(
//                    email!!,
//                    binding.editTextNewPassword.text.toString(),
//                    userID!!,
//                    isDoctorReset
//                )
//                viewModel.isUpdatingPassword.observe(viewLifecycleOwner, Observer {
//                    if (!it) {
//                        if (viewModel.isPasswordUpdated) {
//                            val uptoddDialog = UpToddDialogs(requireContext())
//                            uptoddDialog.showDialog(R.drawable.gif_done,
//                                getString(R.string.your_password_has_been_changed),
//                                getString(R.string.ok)
//                                ,
//                                object : UpToddDialogs.UpToddDialogListener {
//                                    override fun onDialogButtonClicked(dialog: Dialog) {
//                                        uptoddDialog.dismissDialog()
//                                        view?.findNavController()?.navigateUp()
//                                        view?.findNavController()?.navigateUp()
//                                    }
//                                })
//                        }
//                    }
//                })
//            } else {
//                //showInternetNotConnectedDialog()
//                Snackbar.make(
//                    binding.layout,
//                    getString(R.string.no_internet_connection),
//                    Snackbar.LENGTH_LONG
//                )
//                    .setAction(getString(R.string.retry)) {
//                        changePassword()
//                    }.show()
//            }
//        }
//
//    }

//    private fun checkValidPassword(password: String): Boolean {
//        //Enter conditions for valid password
//        var isValid = true
//        var isNumberPresent = false
//        var isSpecialCharacterPresent = false
//        for (c in password) {
//            if (c.isDigit())
//                isNumberPresent = true
//            else if (!c.isLetterOrDigit())
//                isSpecialCharacterPresent = true
//            if (isNumberPresent && isSpecialCharacterPresent)
//                break
//        }
//
//        if (password.length < 8) {
//            binding.textViewPasswordError.text = getString(R.string.password_too_short)
//            isValid = false
//        } else if (!isNumberPresent) {
//            binding.textViewPasswordError.text = getString(R.string.the_password_must_have_a_digit)
//            isValid = false
//        } else if (!isSpecialCharacterPresent) {
//            binding.textViewPasswordError.text =
//                getString(R.string.the_password_must_have_a_special_character)
//            isValid = false
//        }
//        return isValid
//    }

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

//    private fun showUploadingDialog() {
//        val upToddDialogs = UpToddDialogs(requireContext())
//        upToddDialogs.showDialog(R.drawable.gif_upload,
//            getString(R.string.changing_password_please_wait),
//            getString(R.string.back),
//            object : UpToddDialogs.UpToddDialogListener {
//                override fun onDialogButtonClicked(dialog: Dialog) {
//                    dialog.dismiss()
//                    findNavController().navigateUp()
//                }
//            })
//        viewModel.isUpdatingPassword.observe(viewLifecycleOwner, Observer {
//            if (!it) {
//                upToddDialogs.dismissDialog()
//            }
//        })
//        val handler = Handler()
//        handler.postDelayed({
//            upToddDialogs.dismissDialog()
//        }, R.string.loadingDuarationInMillis.toLong())
//
//    }

}