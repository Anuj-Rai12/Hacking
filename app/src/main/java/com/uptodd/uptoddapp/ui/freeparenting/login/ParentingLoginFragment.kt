package com.uptodd.uptoddapp.ui.freeparenting.login

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.uptodd.uptoddapp.FreeParentingDemoActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.LoginParentingFragmentBinding
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.FreeParentingLoginRequest
import com.uptodd.uptoddapp.ui.freeparenting.login.viewmodel.LoginViewModel
import com.uptodd.uptoddapp.utils.*
import com.uptodd.uptoddapp.utils.dialog.showDialogBox

class ParentingLoginFragment : Fragment(R.layout.login_parenting_fragment) {
    private lateinit var binding: LoginParentingFragmentBinding

    private var codeId: String? = null
    private val viewModel: LoginViewModel by viewModels()

    private val countryCode = mutableSetOf(
        "${getEmojiByUnicode(0x1F1EE)}${getEmojiByUnicode(0x1F1F3)} +91",
        "${getEmojiByUnicode(0x1F1F9)}${getEmojiByUnicode(0x1F1ED)} +66",
        "${getEmojiByUnicode(0x1F1E6)}${getEmojiByUnicode(0x1F1EA)} +971",
    )
    private val dropDownArray: ArrayAdapter<String> by lazy {
        ArrayAdapter(requireContext(), R.layout.dropdown, countryCode.toTypedArray())
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LoginParentingFragmentBinding.bind(view)

        viewModel.event.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { res ->
                view.showSnackbar(msg = res, color = Color.RED)
                showErrorDialogBox(res)
            }
        }

        phoneSelection()
        binding.backIconImage.setOnClickListener {
            (activity as FreeParentingDemoActivity?)?.goBack()
        }

        binding.goToDemoDashBoard.setOnClickListener {
            val fullName = binding.userNameEd2.text.toString()
            val email = binding.emailIdEd.text.toString()
            val phoneNumber = binding.userPhoneEd.text.toString()
            if (checkUserInput(fullName) || checkUserInput(email) || checkUserInput(phoneNumber)) {
                activity?.toastMsg("Please Enter Correct Information")
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {//Return True means that Email is Not Valid
                activity?.toastMsg("please enter correct Email address")
                return@setOnClickListener
            }

            if (codeId == null) {
                activity?.toastMsg("Selected the Country Code!!")
                return@setOnClickListener
            }

            if (!isValidPhone(phoneNumber)) { // Return True means Phone is No Valid
                activity?.toastMsg("Please enter the correct Phone Number!!")
                return@setOnClickListener
            }

            val request = FreeParentingLoginRequest(
                email = email,
                mobileCode = codeId!!,
                name = fullName,
                phone = phoneNumber
            )
            viewModel.fetchResponse(request)
        }

        getLoginResponse()


        binding.countryCodeEd.setOnItemClickListener { _, _, position, _ ->
            codeId = getCode(countryCode.elementAt(position))
        }
    }

    private fun getCode(str: String): String? {
        val index = str.indexOf('+')
        if (index == -1)
            return null
        return str.substring(index)
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
            CaptureDeviceInformation.RequestCodeForPhone -> {

                if (resultCode != Activity.RESULT_OK) {
                    setLogCat("onActivityResult", "error $resultCode")
                    return
                }
                val credential: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)
                credential?.apply {
                    val res = getPhoneNumber(this.id)
                    binding.userPhoneEd.setText(res.last())
                    if (countryCode.add(res.first().toString())) {
                        binding.countryCodeEd.setAdapter(dropDownArray)
                    }
                }
                getEmailId()
            }
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


    @SuppressLint("WrongConstant")
    private fun phoneSelection() {
        val credentialsClient = Credentials.getClient(requireActivity())
        val intent = credentialsClient.getHintPickerIntent(hintRequest())
        try {

            startIntentSenderForResult(
                intent.intentSender,
                CaptureDeviceInformation.RequestCodeForPhone,
                null,
                0,
                0, 0, null
            )
        } catch (e: IntentSender.SendIntentException) {
            Log.i("Phone", "phoneSelection: ${e.localizedMessage}")
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as FreeParentingDemoActivity?)?.hideBottomNavBar()
    }

}
