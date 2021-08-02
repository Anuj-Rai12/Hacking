package com.example.hackingwork.auth.forgetpass

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hackingwork.R
import com.example.hackingwork.TAG
import com.example.hackingwork.databinding.MkeFramgentBinding
import com.example.hackingwork.utils.CustomProgress
import com.example.hackingwork.utils.GetConstStringObj
import com.example.hackingwork.utils.MySealed
import com.example.hackingwork.viewmodels.PrimaryViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MakeSelection : Fragment(R.layout.mke_framgent) {
    private lateinit var binding: MkeFramgentBinding
    private val args: MakeSelectionArgs by navArgs()
    private var stringFlag: Boolean? = null
    private val primaryViewModel: PrimaryViewModel by viewModels()

    @Inject
    lateinit var customProgress: CustomProgress

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MkeFramgentBinding.bind(view)
        savedInstanceState?.let {
            stringFlag = it.getBoolean(GetConstStringObj.EMAIL)
        }
        binding.emailDetail.text = args.email
        binding.phoneDetail.text = args.phone
        Log.i(TAG, "onViewCreated: String_Flag $stringFlag")
        if (stringFlag==true) {
            sendPasswordRestEmail()
        }
        binding.emailOption.setOnClickListener {
            sendPasswordRestEmail()
        }
        binding.phoneOption.setOnClickListener {
            dir(2)
        }
    }

    private fun dir(choose: Int = 0, title: String = "Error", message: String = "") {
        val action = when (choose) {
            0 -> MakeSelectionDirections.actionGlobalPasswordDialog(
                title = title,
                message = message
            )
            else -> MakeSelectionDirections.actionGlobalPhoneNumberOtp(userphone = args.phone)
        }
        findNavController().navigate(action)
    }

    private fun sendPasswordRestEmail() {
        stringFlag = true
        primaryViewModel.sendPasswordRestEmail(args.email).observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    stringFlag = false
                    customProgress.hideLoading()
                    dir(message = it.exception?.localizedMessage!!)
                }
                is MySealed.Loading -> {
                    customProgress.showLoading(requireActivity(), it.data as String)
                }
                is MySealed.Success -> {
                    stringFlag = false
                    customProgress.hideLoading()
                    dir(
                        title = "Success!!",
                        message = "Password reset link is Sent to your Registered Email Address"
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        customProgress.hideLoading()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        stringFlag?.let {
            outState.putBoolean(GetConstStringObj.EMAIL, it)
        }
    }
}