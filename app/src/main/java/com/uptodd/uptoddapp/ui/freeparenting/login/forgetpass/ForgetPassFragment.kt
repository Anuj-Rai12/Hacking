package com.uptodd.uptoddapp.ui.freeparenting.login.forgetpass

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FreeParentingForgetPassLayoutBinding
import com.uptodd.uptoddapp.utils.getEmojiByUnicode

class ForgetPassFragment : Fragment(R.layout.free_parenting_forget_pass_layout) {

    private lateinit var binding: FreeParentingForgetPassLayoutBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FreeParentingForgetPassLayoutBinding.bind(view)
        binding.backIconImage.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.checkEmailBtn.setOnClickListener {
            val action = ForgetPassFragmentDirections.actionForgetPassFragmentToOtpFragment()
            findNavController().navigate(action)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        binding.pageTitle.text = "${getEmojiByUnicode(0x1F50F)} ${binding.pageTitle.text}"
        binding.pageDesc.text = "To recover your account please add your register ${
            getEmojiByUnicode(0x1F4E7)
        } e-mail"
    }
}