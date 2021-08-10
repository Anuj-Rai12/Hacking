package com.example.hackingwork.auth


import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hackingwork.MainActivity
import com.example.hackingwork.R
import com.example.hackingwork.TAG
import com.example.hackingwork.databinding.PhoneOtpFaragmentBinding
import com.example.hackingwork.utils.*
import com.example.hackingwork.viewmodels.PrimaryViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class PhoneNumberOtp : Fragment(R.layout.phone_otp_faragment) {
    private lateinit var binding: PhoneOtpFaragmentBinding
    private val primaryViewModel: PrimaryViewModel by viewModels()
    private var verificationProg: Boolean = false
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var myCallBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    private val args: PhoneNumberOtpArgs by navArgs()

    @Inject
    lateinit var customProgress: CustomProgress
    private val timer = object : CountDownTimer(60000, 1000) {
        @SuppressLint("SetTextI18n")
        override fun onTick(millisUntilFinished: Long) {
            binding.otpCountDown.text = "${millisUntilFinished / 1000} sec"
        }

        override fun onFinish() {
            binding.resendotp.isVisible = true
            binding.otpCountDown.text = ""
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = PhoneOtpFaragmentBinding.bind(view)
        savedInstanceState?.let {
            verificationProg = it.getBoolean(GetConstStringObj.USERS)
        }
        getCallBack()
        MainActivity.emailAuthLink?.let { link ->
            Log.i(TAG, "onViewCreated: OTP Link ->$link")
            primaryViewModel.read.observe(viewLifecycleOwner) {
                Log.i(TAG, "onViewCreated: The Value of Create User Detail is -> $it")
                binding.phoneno.text = it.phone
                if (primaryViewModel.credential == null)
                    signInWithEmailLink(link, it.email, it.phone)
            }
        }
        args.userphone?.let { phone ->
            Log.i(TAG, "onViewCreated: Login Via Phone Number $phone")
            binding.phoneno.text = phone
            signInWithPhoneNumber(phone)
        }
        binding.verify.setOnClickListener {
            binding.errorMsg.isVisible = false
            val otp = binding.pinView.text.toString()
            if (checkFieldValue(otp)) {
                Snackbar.make(requireView(), getText(R.string.wrong_detail), Snackbar.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            checkCode(verificationId, otp)
        }
        binding.resendotp.setOnClickListener {
            val phone = binding.phoneno.text.toString()
            if (checkFieldValue(phone)) {
                Snackbar.make(requireView(), getText(R.string.wrong_detail), Snackbar.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            MainActivity.emailAuthLink?.let {
                resendCode(phone, resendToken)
            }
            args.userphone?.let {
                resendCode(phone, resendToken)
            }
            Log.i(TAG, "onViewCreated: Sending Resend Code")
        }
    }

    private fun signInWithEmailLink(link: String, email: String, phone: String) {
        primaryViewModel.createInWithEmail(email, link).observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    val str = it.exception?.localizedMessage!!
                    Log.i(TAG, "signInWithEmailLink:Error-> $str")
                    if (str != getString(R.string.Exception_one) && str != getString(R.string.Exception_one))
                        dir(message = str)
                }
                is MySealed.Loading -> {
                    showLoading(it.data as String)
                }
                is MySealed.Success -> {
                    hideLoading()
                    Log.i(TAG, "signInWithEmailLink: Phone is Send $phone")
                    signInWithPhoneNumber(phone)
                }
            }
        }
    }


    private fun resendCode(phone: String, resendToken: PhoneAuthProvider.ForceResendingToken?) {
        val provide = PhoneAuthOptions.newBuilder()
            .setPhoneNumber(phone)
            .setTimeout(60, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(myCallBack!!)
            .setForceResendingToken(resendToken!!).build()
        PhoneAuthProvider.verifyPhoneNumber(provide)
        timer.start()
        binding.resendotp.isVisible = false
        Toast.makeText(activity, "OTP Sent Successfully", Toast.LENGTH_SHORT).show()
    }

    private fun getCallBack() {
        myCallBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.i(TAG, "onVerificationCompleted: Credential Created Successfully")
                verificationProg = false
                primaryViewModel.credential = credential
                signWithCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                binding.errorMsg.isVisible = true
                binding.errorMsg.text = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        "Error :(\nInvalid Phone Number"
                    }
                    is FirebaseTooManyRequestsException -> {
                        "Error :(\nUsed Too Many Messages Try After 8 hrs."
                    }
                    else -> "Error :(\n ${e.message.toString()}"
                }
                verificationProg = false
                timer.cancel()
                binding.otpCountDown.text = ""
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationId, token)
                activity?.let {
                    Toast.makeText(activity, "OTP Sent Successfully", Toast.LENGTH_SHORT).show()
                }
                this@PhoneNumberOtp.verificationId = verificationId
                resendToken = token
            }
        }
    }


    private fun checkCode(verificationId: String?, code: String) {
        verificationId?.let { Id ->
            val pro = PhoneAuthProvider.getCredential(Id, code)
            signWithCredential(pro)
        }
    }

    private fun signWithCredential(pro: PhoneAuthCredential) {
        args.userphone?.let { phone ->
            Log.i(TAG, "signWithCredential: Sign With Phone Credential")
            signInCurrentUsers(phone, pro)
            return
        }
        primaryViewModel.read.observe(viewLifecycleOwner) {
            Log.i(TAG, "signWithCredential: Updating User Detail")
            updatePhoneNo(pro, it)
        }
    }

    private fun signInCurrentUsers(phone: String, pro: PhoneAuthCredential) {
        primaryViewModel.checkoutCredential(phone = phone, credential = pro)
            .observe(viewLifecycleOwner) {
                when (it) {
                    is MySealed.Error -> {
                        hideLoading()
                        dir(message = "${it.exception?.localizedMessage}")
                    }
                    is MySealed.Loading -> {
                        showLoading(it.data as String)
                    }
                    is MySealed.Success -> {
                        hideLoading()
                        if (it.data == GetConstStringObj.My_Dialog_Once) {
                            dir(message = "User Not Exits So,\nPlease Create Your Account")
                            primaryViewModel.getCurrentUser()?.delete()
                        } else
                            dir(23)
                    }
                }
            }
    }

    private fun updatePhoneNo(credential: PhoneAuthCredential, password: UserStore) {
        primaryViewModel.updatePhoneNumber(credential, password.password)
            .observe(viewLifecycleOwner) {
                when (it) {
                    is MySealed.Error -> {
                        hideLoading()
                        Log.i(TAG, "updatePhoneNo: Error While Updating Phone Number And Password")
                        dir(message = it.exception?.localizedMessage!!)
                    }

                    is MySealed.Loading -> {
                        showLoading(it.data as String)
                    }
                    is MySealed.Success -> {
                        hideLoading()
                        activity?.let {
                            Toast.makeText(activity, "Password is Updated", Toast.LENGTH_SHORT)
                                .show()
                        }
                        updateUser(password)
                    }
                }
            }
    }

    private fun updateUser(userStore: UserStore) {
        primaryViewModel.createUserAccount(userStore).observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                }
                is MySealed.Loading -> {
                    showLoading(it.data as String)
                }
                is MySealed.Success -> {
                    hideLoading()
                    dir(23)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        MainActivity.emailAuthLink?.let {
            if (verificationProg) {
                primaryViewModel.read.observe(viewLifecycleOwner) {
                    signInWithPhoneNumber(it.phone)
                }
            }
        }
        args.userphone?.let { phone ->
            if (verificationProg)
                signInWithPhoneNumber(phone = phone)
        }
    }


    private fun signInWithPhoneNumber(phone: String) {
        val action = PhoneAuthOptions.newBuilder()
            .setPhoneNumber(phone)
            .setTimeout(60, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(myCallBack!!)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(action)
        verificationProg = true
        timer.start()
        binding.resendotp.isVisible = false
        Log.i(TAG, "signInWithPhoneNumber: WELCOME")
    }


    override fun onPause() {
        super.onPause()
        hideLoading()
    }

    private fun dir(choose: Int = 0, title: String = "Error", message: String = "") {
        val action = when (choose) {
            0 -> PhoneNumberOtpDirections.actionGlobalPasswordDialog(title, message)
            else -> PhoneNumberOtpDirections.actionPhoneNumberOtpToAdminActivity()
        }
        findNavController().navigate(action)
        if (choose != 0)
            activity?.finish()
    }

    private fun showLoading(message: String) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        customProgress.showLoading(requireActivity(), string = message)
    }

    private fun hideLoading() {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        customProgress.hideLoading()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        verificationProg.let {
            outState.putBoolean(GetConstStringObj.USERS, it)
        }
    }
}