package com.uptodd.uptoddapp.ui.freeparenting.login.otp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FreeParentingOtpLayoutBinding

class OtpFragment : Fragment(R.layout.free_parenting_otp_layout) {
    private lateinit var binding: FreeParentingOtpLayoutBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FreeParentingOtpLayoutBinding.bind(view)
        binding.backIconImage.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.checkOtpBtn.setOnClickListener {
            val action=OtpFragmentDirections.actionOtpFragmentToUpdateUserPasswordFragment()
            findNavController().navigate(action)
        }
    }
}