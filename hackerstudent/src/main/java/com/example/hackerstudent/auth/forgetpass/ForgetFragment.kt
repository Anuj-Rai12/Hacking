package com.example.hackerstudent.auth.forgetpass

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hackerstudent.utils.*
import com.example.hackerstudent.R
import com.example.hackerstudent.TAG
import com.example.hackerstudent.databinding.ForgetFragmentBinding
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.material.snackbar.Snackbar

class ForgetFragment : Fragment(R.layout.forget_fragment) {
    private lateinit var binding: ForgetFragmentBinding
    private val requestPhone =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activity ->
            val credential: Credential? =
                activity.data?.getParcelableExtra(Credential.EXTRA_KEY)
            credential?.apply {
                getPhoneNumber(credential)?.let { binding.phoneText.setText(it) }
            }
            getEmailId()
        }

    private val requestEmail = registerForActivityResult(ActivityDataInfo()) { output ->
        output.email?.let { email ->
            if (output.requestCode)
                binding.emailText.setText(email)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ForgetFragmentBinding.bind(view)
        phoneSelection()
        binding.nextBtn.setOnClickListener {
            var phone = binding.phoneText.text.toString()
            val email = binding.emailText.text.toString()
            if (checkFieldValue(phone) || checkFieldValue(email)) {
                giveMsg(getString(R.string.wrong_detail))
                return@setOnClickListener
            }
            phone = binding.codePicker.selectedCountryCodeWithPlus + phone
            if (!isValidPhone(phone)) {
                giveMsg(getString(R.string.wrong_phone))
                return@setOnClickListener
        }
        if (!isValidEmail(email)) {
            giveMsg(getString(R.string.wrong_email))
            return@setOnClickListener
        }
            dir(phone, email)
        }
        binding.createacc.setOnClickListener {
            val action = ForgetFragmentDirections.actionForgetFragmentToCreateUserAccount()
            findNavController().navigate(action)
        }
    }

    private fun getEmailId() {
        try {
            requestEmail.launch(InputDataBitch(getIntent()))
        } catch (e: ActivityNotFoundException) {
            Log.i(TAG, "onViewCreated: Activity Not Found Exception")
        }
    }

    private fun giveMsg(string: String) {
        Snackbar.make(requireView(), string, Snackbar.LENGTH_SHORT).show()
    }

    @SuppressLint("WrongConstant")
    private fun phoneSelection() {
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

    private fun dir(phone: String, email: String) {
        val action = ForgetFragmentDirections.actionForgetFragmentToMakeSelection(phone, email)
        findNavController().navigate(action)
    }
}