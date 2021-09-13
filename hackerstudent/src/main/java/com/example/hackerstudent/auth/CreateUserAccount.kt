package com.example.hackerstudent.auth

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.hackerstudent.utils.*
import com.example.hackerstudent.R
import com.example.hackerstudent.TAG
import com.example.hackerstudent.databinding.CreateUserAccountBinding
import com.example.hackerstudent.viewmodels.PrimaryViewModel
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CreateUserAccount : Fragment(R.layout.create_user_account) {
    private lateinit var binding: CreateUserAccountBinding
    private var dialogPhoneFlag: Boolean? = null
    private val primaryViewModel: PrimaryViewModel by activityViewModels()

    @Inject
    lateinit var customProgress: CustomProgress
    private val requestPhone =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activity ->
            val credential: Credential? = activity.data?.getParcelableExtra(Credential.EXTRA_KEY)
            credential?.apply {
                getPhoneNumber(credential)?.let { number -> binding.phone.setText(number) }
            }
            getEmailId()
        }

    private val requestEmail = registerForActivityResult(ActivityDataInfo()) { output ->
        output.email?.let { email ->
            if (output.requestCode)
                binding.emailAddress.setText(email)
        }
    }

    private fun hideLoading() = customProgress.hideLoading()
    private fun showLoading(string: String) =
        customProgress.showLoading(context = requireActivity(), string = string)

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CreateUserAccountBinding.bind(view)
        savedInstanceState?.let {
            dialogPhoneFlag = it.getBoolean(GetConstStringObj.My_Dialog_Once)
        }
        if (dialogPhoneFlag == null) {
            phoneSelection()
        }
        if (primaryViewModel.mutableStateFlow.value?.flag == true) {
            Log.i(TAG, "onViewCreated: ${primaryViewModel.mutableStateFlow.value}")
            val firstName = primaryViewModel.mutableStateFlow.value?.firstname!!
            val lastName = primaryViewModel.mutableStateFlow.value?.lastname!!
            val email = primaryViewModel.mutableStateFlow.value?.email!!
            val pass = primaryViewModel.mutableStateFlow.value?.password!!
            val phone = primaryViewModel.mutableStateFlow.value?.phone!!
            sendEmailLink(firstName, lastName, email, pass, phone)
        }
        binding.nextBtn.setOnClickListener {
            val firstName = binding.firstName.text.toString()
            val lastName = binding.lastName.text.toString()
            val email = binding.emailAddress.text.toString()
            val pass = binding.password.text.toString()
            var phone = binding.phone.text.toString()
            if (checkFieldValue(firstName)
                || checkFieldValue(lastName) ||
                checkFieldValue(email) ||
                checkFieldValue(pass) ||
                checkFieldValue(phone)
            ) {
                Snackbar.make(
                    requireView(),
                    getString(R.string.wrong_detail),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (!isValidEmail(email)) {
                Snackbar.make(requireView(), getString(R.string.wrong_email), Snackbar.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            if (!isValidPassword(pass)) {
                Snackbar.make(
                    requireView(),
                    getString(R.string.wrong_password),
                    Snackbar.LENGTH_LONG
                ).setAction("INFO") {
                    dir(1, message = msg())
                }.show()
                return@setOnClickListener
            }
            phone = binding.countryCode.selectedCountryCodeWithPlus + phone
            if (!isValidPhone(phone)) {
                Snackbar.make(requireView(), getString(R.string.wrong_phone), Snackbar.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            sendEmailLink(firstName, lastName, email, pass, phone)
        }
        binding.backTo.setOnClickListener {
            //Back to Set On click
            dir()
        }
    }

    private fun sendEmailLink(
        firstName: String,
        lastName: String,
        email: String,
        pass: String,
        phone: String
    ) {
        primaryViewModel.mutableStateFlow.value =
            UserStore(
                email,
                pass,
                flag = true,
                ipAddress = getLocalIpAddress() ?: "",
                phone,
                firstName,
                lastName
            )
        primaryViewModel.sendEmailLinkWithToVerify(email).observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    primaryViewModel.mutableStateFlow.value?.flag = false
                    dir(1, "Error", "${it.exception?.localizedMessage}")
                }
                is MySealed.Loading -> {
                    showLoading(it.data!!)
                }
                is MySealed.Success -> {
                    hideLoading()
                    primaryViewModel.mutableStateFlow.value?.flag = false
                    primaryViewModel.storeInitUserDetail(
                        ipAddress = getLocalIpAddress() ?: "",
                        firstname = firstName,
                        lastname = lastName,
                        phone = phone,
                        email = email,
                        password = pass
                    )
                    dir(1, "Success", "${it.data}")
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        hideLoading()
    }


    private fun dir(choose: Int = 0, title: String = "Good Password", message: String = "") {
        val action = when (choose) {
            1 -> CreateUserAccountDirections.actionGlobalPasswordDialog(title, message)
            else -> CreateUserAccountDirections.actionCreateUserAccountToLoginWithEmailPassword()
        }
        findNavController().navigate(action)
    }

    private fun getEmailId() {
        try {
            requestEmail.launch(InputDataBitch(getIntent()))
        } catch (e: ActivityNotFoundException) {
            Log.i(TAG, "onViewCreated: Activity Not Found Exception")
        }
    }

    @SuppressLint("WrongConstant")
    private fun phoneSelection() {
        dialogPhoneFlag = true
        val credentialsClient = Credentials.getClient(requireActivity(), options())
        val intent = credentialsClient.getHintPickerIntent(hintRequest())
        try {
            val op = IntentSenderRequest.Builder(intent.intentSender).setFillInIntent(null)
                .setFlags(0, 0).build()
            requestPhone.launch(op)
        } catch (e: IntentSender.SendIntentException) {
            Log.i(TAG, "phoneSelection: ${e.localizedMessage}")
        }
    }

    private fun msg() = "The Good Password Must contain Following thing ;) :- \n\n" +
            "1.At least 1 digit i.e [0-9]\n" +
            "2.At least 1 lower case letter i.e [a-z]\n" +
            "3.At least 1 upper case letter i.e [A-Z]\n" +
            "4.Any letter i.e [A-Z,a-z]\n" +
            "5.At least 1 special character i.e [%^*!&*|)(%#$%]\n" +
            "6.No white spaces\n" +
            "7.At Least 8 Character\n"

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        dialogPhoneFlag?.let {
            outState.putBoolean(GetConstStringObj.My_Dialog_Once, it)
        }
    }
}