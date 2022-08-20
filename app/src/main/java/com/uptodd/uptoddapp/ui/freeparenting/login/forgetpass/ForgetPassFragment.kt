package com.uptodd.uptoddapp.ui.freeparenting.login.forgetpass

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.GoogleAuthUtil
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FreeParentingForgetPassLayoutBinding
import com.uptodd.uptoddapp.datamodel.forgetpass.ForgetPassRequest
import com.uptodd.uptoddapp.datamodel.forgetpass.ForgetPassResponse
import com.uptodd.uptoddapp.ui.freeparenting.login.viewmodel.LoginViewModel
import com.uptodd.uptoddapp.utils.*
import com.uptodd.uptoddapp.utils.dialog.showDialogBox

class ForgetPassFragment : Fragment(R.layout.free_parenting_forget_pass_layout) {

    private lateinit var binding: FreeParentingForgetPassLayoutBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FreeParentingForgetPassLayoutBinding.bind(view)
        binding.backIconImage.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.event.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { err ->
                showErrorDialog(err)
            }
        }

        getEmailId()
        getForgetPassResponse()

        binding.checkEmailBtn.setOnClickListener {
            val email = binding.emailIdEd.text.toString()
            if (checkUserInput(email)) {
                binding.root.showSnackbar("Enter the Email ID")
                return@setOnClickListener
            }
            if (!isValidEmail(email)) {
                binding.root.showSnackbar(
                    "Invalid Email Address!!"
                )
                return@setOnClickListener
            }
            viewModel.forgetPassResponse(ForgetPassRequest(email))
        }
    }

    private fun getForgetPassResponse() {
        viewModel.forgetPass.observe(viewLifecycleOwner) {res->
            res?.let {
                when (it) {
                    is ApiResponseWrapper.Error -> {
                        showBtn()
                        if (it.data == null) {
                            it.exception?.localizedMessage?.let { err ->
                                showErrorDialog(err)
                            }
                        } else {
                            showErrorDialog("${it.data}")
                        }
                    }
                    is ApiResponseWrapper.Loading -> {
                        hideBtn()
                    }
                    is ApiResponseWrapper.Success -> {
                        showBtn()
                        val data1 = it.data as ForgetPassResponse?
                        data1?.let { data ->
                            binding.root.showSnackbar("OTP sent!!")
                            val handle=Handler(Looper.getMainLooper())
                            handle.post {
                                val action =
                                    ForgetPassFragmentDirections.actionForgetPassFragmentToOtpFragment(data)
                                findNavController().navigate(action)
                            }
                        } ?: showErrorDialog("Oops something went wrong")
                    }
                }
            }
        }
    }


    private fun showErrorDialog(msg: String) {
        activity?.showDialogBox("Failed", msg, icon = android.R.drawable.stat_notify_error) {}
    }

    private fun hideBtn() {
        binding.checkEmailBtn.invisible()
        binding.pbBtn.isVisible = true
    }

    private fun showBtn() {
        binding.checkEmailBtn.show()
        binding.pbBtn.isVisible = false
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CaptureDeviceInformation.RequestCodeForEmail -> {
                //Email
                if (resultCode != Activity.RESULT_OK) {
                    setLogCat("onActivityResult", "error $resultCode")
                    return
                }
                data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)?.let { email ->
                    binding.emailIdEd.setText(email)
                }
            }
            else -> {
                setLogCat("onResultError", "resultCode is $resultCode")
            }
        }
    }

    private fun getEmailId() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = AccountManager.newChooseAccountIntent(
                    null,
                    null,
                    arrayOf(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE),
                    null,
                    null,
                    null,
                    null
                )
                startActivityForResult(intent, CaptureDeviceInformation.RequestCodeForEmail)
            }
        } catch (e: Exception) {
            Log.i("EMAIL", "getEmailId: Cannot Get Email information ${e.localizedMessage}")
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.forgetPassToNull()
        binding.emailIdEd.setText("")
    }
    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        binding.pageTitle.text = "${getEmojiByUnicode(0x1F50F)} Forget Password?"
        binding.pageDesc.text = "To recover your account please add your register ${
            getEmojiByUnicode(0x1F4E7)
        } e-mail"
    }
}