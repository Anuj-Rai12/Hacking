package com.uptodd.uptoddapp.ui.freeparenting.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.FreeParentingDemoActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ProfileLayoutFragmentBinding
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse
import com.uptodd.uptoddapp.utils.getEmojiByUnicode
import com.uptodd.uptoddapp.utils.hide

class ProfileFragment : Fragment(R.layout.profile_layout_fragment) {

    private lateinit var binding: ProfileLayoutFragmentBinding
    private val loginSingletonResponse by lazy {
        LoginSingletonResponse.getInstance()
    }

    private val countryCode = mutableSetOf(
        "${getEmojiByUnicode(0x1F1EE)}${getEmojiByUnicode(0x1F1F3)} +91",
        "${getEmojiByUnicode(0x1F1F9)}${getEmojiByUnicode(0x1F1ED)} +66",
        "${getEmojiByUnicode(0x1F1E6)}${getEmojiByUnicode(0x1F1EA)} +971",
    )

    private val dropDownArray: ArrayAdapter<String> by lazy {
        ArrayAdapter(requireContext(), R.layout.dropdown, countryCode.toTypedArray())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ProfileLayoutFragmentBinding.bind(view)
        val name = loginSingletonResponse.getLoginResponse()?.data?.name
        binding.userProfileTxt.text = name?.first()?.uppercaseChar().toString()
        binding.userTitle.text = name?.split("\\s".toRegex())?.get(0)
        binding.userNameEd.setText(name)
        binding.userEmailEd.setText(loginSingletonResponse.getLoginRequest()?.email)
        binding.userPhoneEd.setText(loginSingletonResponse.getLoginResponse()?.data?.phone)
        binding.countryCodeEd.setAdapter(dropDownArray)
        binding.toolbarNav.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        (activity as FreeParentingDemoActivity?)?.hideBottomNavBar()
        binding.toolbarNav.titleTxt.text = "My Profile"
        binding.toolbarNav.titleTxt.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        binding.toolbarNav.topAppBar.setNavigationIcon(R.drawable.arrow)
        binding.toolbarNav.accountIcon.hide()
    }

    override fun onPause() {
        super.onPause()
        (activity as FreeParentingDemoActivity?)?.showBottomNavBar()
    }
}