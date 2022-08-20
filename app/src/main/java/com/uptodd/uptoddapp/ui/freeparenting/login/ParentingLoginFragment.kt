package com.uptodd.uptoddapp.ui.freeparenting.login

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.GoogleAuthUtil
import com.uptodd.uptoddapp.FreeParentingDemoActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.LoginParentingFragmentBinding
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.FreeParentingLoginRequest
import com.uptodd.uptoddapp.ui.freeparenting.login.viewmodel.LoginViewModel
import com.uptodd.uptoddapp.utils.*
import com.uptodd.uptoddapp.utils.dialog.showDialogBox

class ParentingLoginFragment : Fragment(R.layout.login_parenting_fragment) {
    private lateinit var binding: LoginParentingFragmentBinding
    private val viewModel: LoginViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LoginParentingFragmentBinding.bind(view)

        viewModel.event.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { res ->
                view.showSnackbar(msg = res, color = Color.RED)
                showErrorDialogBox(res)
            }
        }

        getEmailId()
        binding.forgetPass.text =
            Html.fromHtml("<font color='#2A73CC'><u>Forget Password<u></font>")
        binding.backIconImage.setOnClickListener {
            (activity as FreeParentingDemoActivity?)?.goBack()
        }
        binding.goToDemoDashBoard.setOnClickListener {
            val email = binding.emailIdEd.text.toString()
            val pass = binding.userPassEd.text.toString()
            if (checkUserInput(email) || checkUserInput(pass)) {
                binding.root.showSnackbar(
                    "Enter the Required Credential"
                )
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {//Return True means that Email is Not Valid
                binding.root.showSnackbar(
                    "Invalid Email Address!!"
                )
                return@setOnClickListener
            }

            val request = FreeParentingLoginRequest(
                email = email,
                pass = pass
            )
            viewModel.fetchResponse(request)
        }

        getLoginResponse()

    }


    private fun showErrorDialogBox(msg: String) {
        activity?.showDialogBox(
            title = "Failed",
            desc = msg,
            icon = android.R.drawable.stat_notify_error
        ) {
            setLogCat("showErrorDialogBox", "nothing")
        }
    }

    private fun getLoginResponse() {
        viewModel.loginResponse.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResponseWrapper.Error -> {
                    showBtn()
                    if (it.data == null) {
                        it.exception?.localizedMessage?.let { err ->
                            setLogCat("Error_Data", err)
                            //activity?.toastMsg("Error $err")
                            showErrorDialogBox(err)
                        }
                    } else {
                        setLogCat("Error_Data", "${it.data}")
                        //activity?.toastMsg(" Error Data ${it.data}")
                        showErrorDialogBox("${it.data}")
                    }
                }
                is ApiResponseWrapper.Loading -> {
                    hideBtn()
                }
                is ApiResponseWrapper.Success -> {
                    showBtn()
                    //activity?.toastMsg("Success")
                    goToParentingDashBoard()
                }
            }
        }
    }

    private fun hideBtn() {
        binding.goToDemoDashBoard.invisible()
        binding.pbBtn.isVisible = true
    }

    private fun showBtn() {
        binding.goToDemoDashBoard.show()
        binding.pbBtn.isVisible = false
    }

    private fun goToParentingDashBoard() {
        val action = ParentingLoginFragmentDirections
            .actionParentingLoginFragmentToFreeDemoBashBoardFragment()
        findNavController().navigate(action)
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


    override fun onResume() {
        super.onResume()
        (activity as FreeParentingDemoActivity?)?.hideBottomNavBar()
    }

}
