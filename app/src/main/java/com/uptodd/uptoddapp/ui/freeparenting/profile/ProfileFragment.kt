package com.uptodd.uptoddapp.ui.freeparenting.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ProfileLayoutFragmentBinding
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.FreeParentingResponse
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse
import com.uptodd.uptoddapp.ui.freeparenting.profile.viewmodel.ProfileViewModel
import com.uptodd.uptoddapp.utils.ApiResponseWrapper
import com.uptodd.uptoddapp.utils.dialog.showDialogBox
import com.uptodd.uptoddapp.utils.hide
import com.uptodd.uptoddapp.utils.setLogCat
import com.uptodd.uptoddapp.utils.show

class ProfileFragment : Fragment(R.layout.profile_layout_fragment) {

    private lateinit var binding: ProfileLayoutFragmentBinding

    private val viewModel: ProfileViewModel by viewModels()

    private val loginSingletonResponse by lazy {
        LoginSingletonResponse.getInstance()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ProfileLayoutFragmentBinding.bind(view)

        viewModel.event.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { err ->
                showErrorDialogBox(err)
            }
        }
        binding.toolbarNav.accountIcon.setOnClickListener {
            //Working on it
        }
        getProfileResponse()
    }

    private fun getProfileResponse() {
        viewModel.profileResponse.observe(viewLifecycleOwner) { res ->
            res?.let {
                when (it) {
                    is ApiResponseWrapper.Error -> {
                        hidePb()
                        if (it.data == null) {
                            it.exception?.localizedMessage?.let { err ->
                                showErrorDialogBox(err)
                            }
                        } else {
                            showErrorDialogBox("${it.data}")
                        }
                    }
                    is ApiResponseWrapper.Loading -> {
                        showPb()
                        binding.loadingTxt.text = "${it.data}"
                    }
                    is ApiResponseWrapper.Success -> {
                        hidePb()
                        val data = it.data as FreeParentingResponse?
                        data?.let { res ->
                            setUpUI(res)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        binding.toolbarNav.topAppBar.navigationIcon = null
        binding.toolbarNav.titleTxt.text = "My Profile"
        binding.toolbarNav.accountIcon.show()
        viewModel.getProfile(loginSingletonResponse.getLoginResponse()?.data?.id!!.toLong())
    }

    private fun setUpUI(data: FreeParentingResponse) {
        binding.userProfileTxt.text = data.data.name.first().uppercaseChar().toString()
        binding.userTitle.text = data.data.name.split("\\s".toRegex())[0]
        binding.userNameEd.setText(data.data.name)
        binding.userEmailEd.setText(data.data.email)
        binding.userPhoneEd.setText(data.data.phone)
    }

    private fun hidePb() {
        binding.progressForProfile.hide()
        binding.loadingTxt.hide()
        binding.profileLayout.show()
    }

    private fun showPb() {
        binding.profileLayout.hide()
        binding.progressForProfile.show()
        binding.loadingTxt.show()
    }

    private fun showErrorDialogBox(msg: String) {
        activity?.showDialogBox(
            title = "Failed",
            desc = msg,
            icon = android.R.drawable.stat_notify_error
        ) {
            setLogCat("showErrorDialogBox", "nothing")
        }
    }

}