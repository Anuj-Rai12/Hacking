package com.uptodd.uptoddapp.ui.freeparenting.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ProfileLayoutFragmentBinding
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse

class ProfileFragment : Fragment(R.layout.profile_layout_fragment) {

    private lateinit var binding: ProfileLayoutFragmentBinding
    private val loginSingletonResponse by lazy {
        LoginSingletonResponse.getInstance()
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
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        binding.toolbarNav.topAppBar.navigationIcon = null
        binding.toolbarNav.titleTxt.text = "My Profile"
    }

}