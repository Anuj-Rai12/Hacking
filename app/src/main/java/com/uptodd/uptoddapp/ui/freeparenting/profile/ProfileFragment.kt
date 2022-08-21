package com.uptodd.uptoddapp.ui.freeparenting.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.FreeParentingDemoActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ProfileLayoutFragmentBinding
import com.uptodd.uptoddapp.datamodel.changeprofie.ChangeProfileRequest
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.FreeParentingResponse
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse
import com.uptodd.uptoddapp.ui.freeparenting.profile.viewmodel.ProfileViewModel
import com.uptodd.uptoddapp.utils.*
import com.uptodd.uptoddapp.utils.dialog.showDialogBox

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
        binding.updateInfoBtn.setOnClickListener {
            val phone = binding.userPhoneEd.text.toString()
            val name = binding.userNameEd.text.toString()
            if (loginSingletonResponse.getLoginResponse()?.data?.phone == phone
                && loginSingletonResponse.getLoginResponse()?.data?.name == name
            ) {
                return@setOnClickListener
            }
            if (checkUserInput(phone)) {
                setToastMsg("Phone number cannot be empty")
                return@setOnClickListener
            }
            if (checkUserInput(name)) {
                setToastMsg("User Name cannot be empty")
                return@setOnClickListener
            }
            if (!isValidPhone(phone)) {
                setToastMsg("Invalid Phone number")
                return@setOnClickListener
            }
            viewModel.updateProfileDetail(
                ChangeProfileRequest(
                    loginSingletonResponse.getLoginResponse()!!.data.id,
                    name = name,
                    phone = phone,
                )
            )

        }
        binding.toolbarNav.accountIcon.setOnClickListener {
            val action=ProfileFragmentDirections.actionProfileFragmentToChildProfileFragment()
            findNavController().navigate(action)
        }
        getProfileResponse()
    }

    private fun setToastMsg(msg: String) {
        binding.root.showSnackBarMsg(
            msg,
            anchor = (activity as FreeParentingDemoActivity).getBottomNav()
        )
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
                        } ?: showErrorDialogBox("Failed to show user response")
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
        binding.userEmailEd.isEnabled = false
        binding.toolbarNav.accountIcon.show()
        (activity as FreeParentingDemoActivity?)?.showBottomNavBar()
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