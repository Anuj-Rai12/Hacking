package com.example.hackerstudent.auth

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hackerstudent.utils.*
import com.example.hackerstudent.MainActivity
import com.example.hackerstudent.R
import com.example.hackerstudent.TAG
import com.example.hackerstudent.databinding.LoginWithEmailPasswordBinding
import com.example.hackerstudent.viewmodels.PrimaryViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginWithEmailPassword : Fragment(R.layout.login_with_email_password) {
    private lateinit var binding: LoginWithEmailPasswordBinding
    private val primaryViewModel: PrimaryViewModel by viewModels()


    @Inject
    lateinit var customProgress: CustomProgress
    private val onActivityStart = registerForActivityResult(ActivityDataInfo()) { output ->
        output.email?.let { email ->
            if (output.requestCode)
                binding.emailText.setText(email)
        }
    }

    private fun showLoading(string: String) = customProgress.showLoading(requireActivity(), string)
    private fun hideLoading() = customProgress.hideLoading()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LoginWithEmailPasswordBinding.bind(view)
        primaryViewModel.read.observe(viewLifecycleOwner) {
            Log.i(TAG, "onViewCreated: for login $it")
            binding.emailText.setText(it.email)
            binding.passwordText.setText(it.password)
            binding.remeberme.isChecked = it.flag
        }
        if (primaryViewModel.mutableStateFlow.value == null)
            getEmail()
        if (primaryViewModel.mutableStateFlow.value?.flag == true)
            checkEmail(
                password = primaryViewModel.mutableStateFlow.value?.password!!,
                email = primaryViewModel.mutableStateFlow.value?.email!!,
                string = primaryViewModel.mutableStateFlow.value?.phone!!
            )

        binding.backSign.setOnClickListener {
            //Create Acc
            val action =
                LoginWithEmailPasswordDirections.actionLoginWithEmailPasswordToCreateUserAccount()
            findNavController().navigate(action)
        }
        binding.forpass.setOnClickListener {
            //Forget Password
            val action =
                LoginWithEmailPasswordDirections.actionLoginWithEmailPasswordToForgetFragment()
            findNavController().navigate(action)
        }
        binding.nextBtn.setOnClickListener {
            val email = binding.emailText.text.toString()
            val password = binding.passwordText.text.toString()
            val flag=if (binding.remeberme.isChecked)
                getString(R.string.Exception_one)
            else
                getString(R.string.Exception_two)

            if (checkFieldValue(email) || checkFieldValue(password)) {
                Snackbar.make(
                    requireView(),
                    getString(R.string.wrong_detail),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            checkEmail(email, password,flag)
        }
    }

    private fun checkEmail(email: String, password: String,string: String) {
        primaryViewModel.mutableStateFlow.value =
            UserStore(
                email,
                password,
                flag = true,
                ipAddress = getLocalIpAddress() ?: "",
                string,
                "firstName",
                "lastName"
            )
        primaryViewModel.checkEmailOfUsers(email, password).observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    primaryViewModel.mutableStateFlow.value?.flag = false
                    val action = LoginWithEmailPasswordDirections.actionGlobalPasswordDialog(
                        "Error",
                        it.exception?.localizedMessage!!
                    )
                    findNavController().navigate(action)
                }
                is MySealed.Loading -> showLoading(it.data as String)
                is MySealed.Success -> {
                    hideLoading()
                    primaryViewModel.mutableStateFlow.value?.flag = false
                    saveData(email, password)
                    dir()
                }
            }
        }
    }

    private fun saveData(email: String, password: String) {
        if (primaryViewModel.mutableStateFlow.value?.phone==getString(R.string.Exception_one)) {
            primaryViewModel.storeUserInfo(email, password,true)
        }
    }

    override fun onPause() {
        super.onPause()
        hideLoading()
    }
    private fun getEmail() {
        try {
            if (MainActivity.emailAuthLink == null)
                onActivityStart.launch(InputDataBitch(getIntent()))
        } catch (e: ActivityNotFoundException) {
            Log.i(TAG, "onViewCreated: Activity Not Found Exception")
        }
    }

    private fun dir() {
        val action = LoginWithEmailPasswordDirections.actionLoginWithEmailPasswordToAdminActivity()
        findNavController().navigate(action)
        activity?.finish()
    }

    override fun onStart() {
        super.onStart()
        MainActivity.emailAuthLink?.let {
            val action = LoginWithEmailPasswordDirections.actionGlobalPhoneNumberOtp()
            findNavController().navigate(action)
        }
    }
}