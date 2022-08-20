package com.uptodd.uptoddapp.ui.freeparenting.login.updatepass

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.LoginParentingFragmentBinding
import com.uptodd.uptoddapp.utils.getEmojiByUnicode
import com.uptodd.uptoddapp.utils.hide

class UpdateUserPasswordFragment : Fragment(R.layout.login_parenting_fragment) {
    private lateinit var binding: LoginParentingFragmentBinding
    private var flagForBackPressed = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LoginParentingFragmentBinding.bind(view)
        onBackPress()
        binding.backIconImage.setOnClickListener {
            goBack()
        }
        binding.goToDemoDashBoard.setOnClickListener {
            //Do loading
        }

    }


    private fun onBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (flagForBackPressed){
                goBack()
            }
        }.handleOnBackPressed()
    }

    private fun goBack() {
        val action =
            UpdateUserPasswordFragmentDirections.actionGlobalParentingLoginFragment()
        findNavController().navigate(action)
    }


    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        flagForBackPressed = true
        binding.pageTitle.text = "Almost There Anuj,"
        binding.pageDesc.text =
            "you just need to create new Password ${getEmojiByUnicode(0X1F4AA)} strong password"
        binding.userPassTxt.text = "New Password"
        binding.forgetPass.hide()
        binding.backIconImage.setImageResource(R.drawable.ic_cancel)
    }
}