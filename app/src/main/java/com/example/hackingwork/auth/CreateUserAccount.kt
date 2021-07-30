package com.example.hackingwork.auth

import android.annotation.SuppressLint
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hackingwork.R
import com.example.hackingwork.TAG
import com.example.hackingwork.databinding.CreateUserAccountBinding
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.CredentialsOptions
import com.google.android.gms.auth.api.credentials.HintRequest

class CreateUserAccount : Fragment(R.layout.create_user_account) {
    private lateinit var binding: CreateUserAccountBinding
    private val request =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activty ->
            val credential: Credential? = activty.data?.getParcelableExtra(Credential.EXTRA_KEY)
            credential?.apply {
                Log.i(TAG, "Phone Number: ${credential.id}")
                Toast.makeText(activity, "Phone Number: ${credential.id}", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CreateUserAccountBinding.bind(view)
        phoneSelection()
        binding.nextBtn.setOnClickListener {
            //OTP Screen
            val action = CreateUserAccountDirections.actionGlobalPhoneNumberOtp()
            findNavController().navigate(action)
        }
        binding.backTo.setOnClickListener {
            //Back to Set On click
            val action =
                CreateUserAccountDirections.actionCreateUserAccountToLoginWithEmailPassword()
            findNavController().navigate(action)
        }
    }

    @SuppressLint("BanParcelableUsage", "WrongConstant")
    private fun phoneSelection() {
        val hintRequest = HintRequest.Builder()
            .setPhoneNumberIdentifierSupported(true)
            .build()
        val options = CredentialsOptions.Builder()
            .forceEnableSaveDialog()
            .build()
        val credentialsClient = Credentials.getClient(requireActivity(), options)
        val intent = credentialsClient.getHintPickerIntent(hintRequest)
        try {
            val op = IntentSenderRequest.Builder(intent.intentSender).setFillInIntent(null)
                .setFlags(0, 0).build()
            request.launch(op)
        } catch (e: IntentSender.SendIntentException) {
            Log.i(TAG, "phoneSelection: ${e.localizedMessage}")
            e.printStackTrace()
        }
    }
}