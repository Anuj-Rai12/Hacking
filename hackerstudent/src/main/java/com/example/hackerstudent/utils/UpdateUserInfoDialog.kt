package com.example.hackerstudent.utils

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.example.hackerstudent.R
import com.example.hackerstudent.TAG
import com.example.hackerstudent.databinding.AlertDialogLayoutBinding
import com.example.hackerstudent.viewmodels.PrimaryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UpdateUserInfoDialog constructor(
    private val title: String? = null,
    private val sendEmail: (String) -> Unit,
    private val sendPass: (String, String, String) -> Unit,
    private val sendUser: (String, String) -> Unit
) : DialogFragment() {
    private lateinit var binding: AlertDialogLayoutBinding
    private var alertDialog: AlertDialog.Builder? = null
    private val authViewModel: PrimaryViewModel by activityViewModels()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        binding = AlertDialogLayoutBinding.inflate(layoutInflater)
        alertDialog =
            AlertDialog.Builder(requireActivity()).setView(binding.root)
                .setIcon(R.drawable.hacking_main_icon)
                .setTitle("$title")

        alertDialog?.create()?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        when (title) {
            GetConstStringObj.change_email_address -> {
                getShowBtn()
                binding.changeEmailLayout.show()
                getData()
            }
            GetConstStringObj.change_profile_password -> {
                getShowBtn()
                binding.emailTextPassLayout.show()
                binding.passwordTextCurrLayout.show()
                lifecycleScope.launch {
                    authViewModel.read.asFlow().collectLatest {
                        if (it.email != "") {
                            binding.emailTextPass.setText(it.email)
                        }
                        if (it.password != "") {
                            binding.passwordTextCurr.setText(it.password)
                        }
                    }
                }
                binding.passwordNewLayout.show()
                binding.confirmPassLayout.show()
                getPassWord()
            }
            GetConstStringObj.change_profile_name -> {
                getShowBtn()
                binding.firstNameLayout.show()
                binding.lastNameLayout.show()
                getUserName()
            }
            else -> Log.i(TAG, "onCreateDialog: $title")
        }

        return alertDialog?.create()!!
    }

    private fun getUserName() {
        binding.btnSub.setOnClickListener {
            val firstName = binding.firstName.text.toString()
            val lastName = binding.lastName.text.toString()
            if (checkFieldValue(firstName) || checkFieldValue(lastName)) {
                context?.msg("Please Enter Correct User Detail")
                return@setOnClickListener
            }
            sendUser(firstName, lastName)
        }
    }

    private fun getPassWord() {
        binding.btnSub.setOnClickListener {
            val newPassword = binding.passwordTextNew.text.toString()
            val confirmPassword = binding.passwordTextConfirm.text.toString()
            val email = binding.emailTextPass.text.toString()
            val currentPassword = binding.passwordTextCurr.text.toString()
            if (checkFieldValue(newPassword) || checkFieldValue(confirmPassword) || !isValidPassword(
                    newPassword
                )
                || !isValidPassword(confirmPassword) || confirmPassword != newPassword || !isValidEmail(
                    email
                ) || !isValidPassword(currentPassword)
            ) {
                context?.msg("Please Check Your Password")
                return@setOnClickListener
            }
            sendPass(email, currentPassword, newPassword)
        }
    }

    private fun getData() {
        binding.btnSub.setOnClickListener {
            val email = binding.emailText.text.toString()
            if (checkFieldValue(email) || !isValidEmail(email)) {
                context?.msg("User email is Not Valid")
                return@setOnClickListener
            }
            Log.i(TAG, "getData: Update Email ->$email")
            sendEmail(email)
        }
    }

    private fun getShowBtn() = binding.btnSub.show()

}