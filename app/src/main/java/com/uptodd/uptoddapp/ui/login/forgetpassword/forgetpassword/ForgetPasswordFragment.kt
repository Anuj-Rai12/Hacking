package com.uptodd.uptoddapp.ui.login.forgetpassword.forgetpassword

import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FragmentForgetPasswordBinding
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.utilities.UpToddDialogs


class ForgetPasswordFragment : Fragment() {

    private var isDoctorReset: Boolean = false
    private lateinit var binding: FragmentForgetPasswordBinding
    private lateinit var viewModel: ForgetPasswordViewModel

    private var functionality: Int = 1

    /**functionality=1    this is for forget password option
     * functionality=2    this is for enter OTP option
     */

    private var email: String? = null
    private lateinit var shakeAnimation: Animation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.fragment_forget_password,
                container,
                false
            )
        binding.lifecycleOwner = this

        viewModel = ViewModelProvider(this).get(ForgetPasswordViewModel::class.java)

        shakeAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)

        binding.button.setOnClickListener { onClickSendMail() }

        val args = ForgetPasswordFragmentArgs.fromBundle(requireArguments())
        isDoctorReset = args.isDoctorReset


        // click listener for back button
        binding.goBack.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    private fun onClickSendMail() {

        if (binding.editText.text.isNullOrBlank()) {
            binding.editTextContainer.error = "Email cannot be empty"
            binding.editTextContainer.startAnimation(shakeAnimation)
            return
        } else {
            binding.editTextContainer.error = null
            binding.editTextContainer.isErrorEnabled = false
        }


        if (AppNetworkStatus.getInstance(requireContext()).isOnline) {
            viewModel.isLoadingDialogVisible.value = true
            viewModel.isEmailSent = false
            showLoadingDialog()

            email = binding.editText.text.toString()
            viewModel.sendOTP(email!!, isDoctorReset)
            viewModel.isLoadingDialogVisible.observe(viewLifecycleOwner, Observer {
                if (!it) {
                    if (viewModel.isEmailSent) {
                        functionality = 2
                        changeFunctionalityToOTP()
                    } else
                        binding.editTextContainer.error = getString(R.string.unable_to_send_otp)
                }
            })


        } else {
            //showInternetNotConnectedDialog()
            Snackbar.make(
                binding.layout,
                getString(R.string.no_internet_connection),
                Snackbar.LENGTH_LONG
            )
                .setAction(getString(R.string.retry)) {
                    onClickSendMail()
                }.show()
        }
    }


    private fun changeFunctionalityToOTP() {
        binding.textViewHeading.text = getString(R.string.enter_otp)
        binding.textViewSubHeading.text =
            getString(R.string.enter_the_otp_sent_to_your_email_id_below)

        binding.editText.text?.clear()
        binding.editTextContainer.hint = getString(R.string.enter_otp)
        binding.editText.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        binding.editTextContainer.isPasswordVisibilityToggleEnabled = true
        binding.button.text = getString(R.string.submit_otp)
        binding.button.setOnClickListener { submitOTP() }
        startTimer()
    }

    private fun submitOTP() {
        if (!viewModel.checkOTP(binding.editText.text.toString()))
            binding.editTextContainer.error = getString(R.string.wrong_otp)
        else {
            findNavController().navigate(
                ForgetPasswordFragmentDirections.actionForgetPasswordFragment2ToResetPasswordFragment(
                    isDoctorReset,
                    email!!
                )
            )
        }
    }

    private fun startTimer() {

        object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minute = (millisUntilFinished / 60000).toString().padStart(2, '0')
                val sec = ((millisUntilFinished / 1000) % 60).toString().padStart(2, '0')
                binding.editTextContainer.helperText = "$minute:$sec"
            }

            override fun onFinish() {
                binding.resendOtp.isVisible = true
                binding.resendOtp.setOnClickListener {
                    viewModel.sendOTP(email!!, isDoctorReset)
                    binding.editTextContainer.isHelperTextEnabled = false
                    changeFunctionalityToOTP()
                }
            }
        }.start()
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
            }
        })
        val handler = Handler()
        handler.postDelayed({
            upToddDialogs.dismissDialog()
        }, R.string.loadingDuarationInMillis.toLong())

    }
}

