package com.example.hackingwork.auth

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
import androidx.navigation.fragment.findNavController
import com.example.hackingwork.R
import com.example.hackingwork.TAG
import com.example.hackingwork.databinding.CreateUserAccountBinding
import com.example.hackingwork.utils.*
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.material.snackbar.Snackbar

class CreateUserAccount : Fragment(R.layout.create_user_account) {
    private lateinit var binding: CreateUserAccountBinding
    private var dialogPhoneFlag:Boolean?=null
    private val requestPhone =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activity ->
            val credential: Credential? = activity.data?.getParcelableExtra(Credential.EXTRA_KEY)
            credential?.apply {
                getPhoneNumber(credential)
            }
            getEmailId()
        }

    private val requestEmail = registerForActivityResult(ActivityDataInfo()) { output ->
        output.email?.let { email ->
            if (output.requestCode)
                binding.emailAddress.setText(email)
        }
    }

    private fun getPhoneNumber(credential: Credential) {
        val codedPhoneNumber = credential.id
        if (codedPhoneNumber.contains("+91")) {
            binding.phone.setText(codedPhoneNumber.split("+91").last())
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CreateUserAccountBinding.bind(view)
        savedInstanceState?.let {
            dialogPhoneFlag=it.getBoolean(GetConstStringObj.My_Dialog_Once)
        }
        if (dialogPhoneFlag ==null){
            phoneSelection()
        }
        binding.nextBtn.setOnClickListener {
            val firstName=binding.firstName.text.toString()
            val lastName=binding.lastName.text.toString()
            val email=binding.emailAddress.text.toString()
            val pass=binding.password.text.toString()
            val phone=binding.countryCode.selectedCountryCodeWithPlus+binding.phone.text.toString()
            if (checkFieldValue(firstName)
                || checkFieldValue(lastName)||
                checkFieldValue(email)||
                checkFieldValue(pass)||
                checkFieldValue(phone)
            ){
                Snackbar.make(requireView(),getString(R.string.wrong_detail),Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isValidEmail(email)){
                Snackbar.make(requireView(),getString(R.string.wrong_email),Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isValidPassword(pass)){
                Snackbar.make(requireView(),getString(R.string.wrong_password),Snackbar.LENGTH_LONG).setAction("INFO"){
                    dir(1,message = msg())
                }.show()
                return@setOnClickListener
            }
            if (!isValidPhone(phone)){
                Snackbar.make(requireView(),getString(R.string.wrong_phone),Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Log.i(TAG, "onViewCreated: Email ->$email")
            Log.i(TAG, "onViewCreated: Phone ->$phone")
            Log.i(TAG, "onViewCreated: LastName ->$lastName")
            Log.i(TAG, "onViewCreated: firstName ->$firstName")
            Log.i(TAG, "onViewCreated: passWord ->$$pass")
        }
        binding.backTo.setOnClickListener {
            //Back to Set On click
            dir()
        }
    }

    private fun dir(choose: Int = 0, title: String = "Good Password", message: String = "") {
        val action = when (choose) {
            1 -> {
                CreateUserAccountDirections.actionGlobalPasswordDialog(title, message)
            }
            else -> {
                CreateUserAccountDirections.actionCreateUserAccountToLoginWithEmailPassword()
            }
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

    @SuppressLint("BanParcelableUsage", "WrongConstant")
    private fun phoneSelection() {
        dialogPhoneFlag=true
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
            outState.putBoolean(GetConstStringObj.My_Dialog_Once,it)
        }
    }
}