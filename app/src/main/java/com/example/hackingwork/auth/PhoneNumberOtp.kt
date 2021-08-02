package com.example.hackingwork.auth


import android.annotation.SuppressLint
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
    private var verificationProg: Boolean? = null
    private var flag: Boolean? = null
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
        Log.i(TAG, "onViewCreated: OPT value is -> ${primaryViewModel.read.value}")
        primaryViewModel.read.observe(viewLifecycleOwner) {
            binding.phoneno.text = args.userphone ?: it.phone
        }
        savedInstanceState?.let {
            flag = it.getBoolean(GetConstStringObj.USERS)
        }
        getCallBack()
        if (MainActivity.emailAuthLink != null) {
            forFirstTimeSignIn()
        }
        Log.i(TAG, "onViewCreated: flag value is -> $flag")
        if (args.userphone != null) {
            Log.i(TAG, "onViewCreated: Enter in the UserPhone ")
            Log.i(TAG, "onViewCreated: ${primaryViewModel.credential}")
            if (primaryViewModel.credential != null && flag == false)
                signInWithExitingUser(pro = primaryViewModel.credential!!)
            else if (primaryViewModel.credential == null && flag == null)
                signInWithPhoneNumber(args.userphone!!)
        }
        binding.verify.setOnClickListener {
            if (checkFieldValue(binding.pinView.text.toString())) {
                Snackbar.make(requireView(), "Please Enter the OTP", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            checkCode(verificationId, code = binding.pinView.text.toString())
        }
        binding.resendotp.setOnClickListener {
            resendCode(getPhone()!!, resendToken)
        }
    }

    private fun forFirstTimeSignIn() {
        if (flag == null) {
            Log.i(TAG, "onViewCreated: Phone $flag")
            initialAccountVerification()
        }
        if (flag == true) {
            Log.i(TAG, "onViewCreated:Phone  Flags is $flag")
            signInWithLink(
                link = MainActivity.emailAuthLink!!,
                email = primaryViewModel.read.value?.email!!
            )
        }
        if (primaryViewModel.mutableStateFlow.value?.email == GetConstStringObj.My_Dialog_Once && primaryViewModel.credential != null) {
            Log.i(TAG, "onViewCreated: ${primaryViewModel.mutableStateFlow.value?.email}")
            Log.i(TAG, "onViewCreated: ${primaryViewModel.credential}")
            signInWithCredential(primaryViewModel.credential!!)
        }
        if (primaryViewModel.mutableStateFlow.value?.firstname == GetConstStringObj.USERS) {
            Log.i(TAG, "onViewCreated: ${primaryViewModel.mutableStateFlow.value?.firstname}")
            createUserAccount()
        }
    }

    private fun initialAccountVerification() {
        MainActivity.emailAuthLink?.let { link ->
            primaryViewModel.read.observe(viewLifecycleOwner) { store ->
                signInWithLink(store.email, link)
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
                if (args.userphone != null)
                    signInWithExitingUser(credential)
                else
                    signInWithCredential(credential)
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

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        val password = primaryViewModel.read.value?.password!!
        primaryViewModel.mutableStateFlow.value =
            UserStore(
                GetConstStringObj.My_Dialog_Once,
                "pass",
                flag = true,
                ipAddress = "",
                "phone",
                "firstName",
                "lastName"
            )
        primaryViewModel.credential = credential
        primaryViewModel.updatePhoneNumber(credential, password).observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    primaryViewModel.credential = null
                    timer.cancel()
                    dir(message = it.exception?.localizedMessage!!)
                }
                is MySealed.Loading -> showLoading(it.data as String)
                is MySealed.Success -> {
                    hideLoading()
                    primaryViewModel.credential = null
                    timer.cancel()
                    createUserAccount()
                }
            }
        }
    }

    private fun createUserAccount() {
        val userData = primaryViewModel.read.value
        primaryViewModel.mutableStateFlow.value?.firstname = GetConstStringObj.USERS
        primaryViewModel.createUserAccount(userData!!).observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    primaryViewModel.mutableStateFlow.value?.firstname =
                        GetConstStringObj.My_Dialog_Once
                    dir(message = it.exception?.localizedMessage ?: "UnWanted Error")
                }
                is MySealed.Loading -> showLoading(it.data as String)
                is MySealed.Success -> {
                    hideLoading()
                    primaryViewModel.mutableStateFlow.value?.firstname =
                        GetConstStringObj.My_Dialog_Once
                    primaryViewModel.storeInitUserDetail(
                        ipAddress = "",
                        firstname = "",
                        lastname = "",
                        phone = "",
                        password = "",
                        email = ""
                    )
                    dir(2)
                    activity?.finish()
                }
            }
        }
    }

    private fun checkCode(verificationId: String?, code: String) {
        verificationId?.let {
            val pro = PhoneAuthProvider.getCredential(it, code)
            if (args.userphone != null)
                signInWithExitingUser(pro)
            else
                signInWithCredential(pro)
        }
    }

    private fun signInWithExitingUser(pro: PhoneAuthCredential) {
        primaryViewModel.credential = pro
        primaryViewModel.checkoutCredential(pro, args.userphone!!).observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    primaryViewModel.credential = null
                    dir(message = it.exception?.localizedMessage!!)
                }
                is MySealed.Loading -> showLoading(it.data as String)
                is MySealed.Success -> {
                    hideLoading()
                    primaryViewModel.credential = null
                    if (it.data == GetConstStringObj.My_Dialog_Once) {
                        flag = true
                        dir(message = getString(R.string.Miss_request))
                    } else {
                        dir(32)
                        activity?.finish()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (verificationProg == true) {
            Log.i(TAG, "onStart: SingInWithPhoneNumber Activated ${args.userphone}")
            signInWithPhoneNumber(getPhone()!!)
        }
    }

    private fun getPhone() = if (primaryViewModel.read.value?.phone.isNullOrEmpty())
        args.userphone
    else
        primaryViewModel.read.value?.phone

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

    private fun signInWithLink(email: String, link: String) {
        flag = true
        primaryViewModel.createInWithEmail(email, link).observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    flag = false
                    val e = it.exception?.localizedMessage!!
                    Log.i(TAG, "createInWithEmail:$e")
                    if (e != getString(R.string.Exception_one) && e != getString(R.string.Exception_two))
                        dir(message = e)
                }
                is MySealed.Loading -> {
                    showLoading(it.data as String)
                }
                is MySealed.Success -> {
                    hideLoading()
                    flag = false
                    Log.i(TAG, "signInWithLink: Sent Otp for SignInWithLink")
                    signInWithPhoneNumber(primaryViewModel.read.value?.phone!!)
                }
            }
        }
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
    }

    private fun showLoading(message: String) =
        customProgress.showLoading(requireActivity(), string = message)

    private fun hideLoading() = customProgress.hideLoading()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        flag?.let {
            outState.putBoolean(GetConstStringObj.USERS, it)
        }
    }
}