package com.uptodd.uptoddapp.ui.freeparenting.login.otp

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FreeParentingOtpLayoutBinding
import com.uptodd.uptoddapp.utils.checkUserInput
import com.uptodd.uptoddapp.utils.setLogCat
import com.uptodd.uptoddapp.utils.showSnackbar

class OtpFragment : Fragment(R.layout.free_parenting_otp_layout) {
    private lateinit var binding: FreeParentingOtpLayoutBinding
    private val args: OtpFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FreeParentingOtpLayoutBinding.bind(view)
        binding.backIconImage.setOnClickListener {
            findNavController().popBackStack()
        }
        setLogCat("OTP_ARGS", "${args.response}")
        binding.checkOtpBtn.setOnClickListener {
            val otp = binding.etOtp.text.toString()
            if (checkUserInput(otp)) {
                binding.root.showSnackbar(
                    "Enter the Credentials.."
                )
                return@setOnClickListener
            }
            if (otp.toLong() != args.response.data.otp.toLong()) {
                binding.root.showSnackbar("Invalid OTP!!")
                return@setOnClickListener
            }
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                val action =
                    OtpFragmentDirections.actionOtpFragmentToUpdateUserPasswordFragment(args.response)
                findNavController().navigate(action)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        binding.pageTitle.text = "Hey ${args.response.data.name.split("\\s".toRegex())[0]},"
        binding.pageDesc.text =
            "Please Enter the OTP send to your registered ${args.response.data.email}"
    }
}