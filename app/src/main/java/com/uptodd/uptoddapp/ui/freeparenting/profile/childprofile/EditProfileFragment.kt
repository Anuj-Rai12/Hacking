package com.uptodd.uptoddapp.ui.freeparenting.profile.childprofile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.FreeParentingDemoActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FreeParentingBabyEditProfileFragmentBinding
import com.uptodd.uptoddapp.datamodel.changeprofie.ChangeProfileRequest
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.FreeParentingResponse
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse
import com.uptodd.uptoddapp.ui.freeparenting.profile.viewmodel.ProfileViewModel
import com.uptodd.uptoddapp.utils.*
import com.uptodd.uptoddapp.utils.dialog.showDialogBox

class EditProfileFragment : Fragment(R.layout.free_parenting_baby_edit_profile_fragment) {

    private lateinit var binding: FreeParentingBabyEditProfileFragmentBinding

    private var genderPosition: Int? = null

    private val viewModel: ProfileViewModel by viewModels()

    private val profileDetail by lazy {
        LoginSingletonResponse.getInstance()
    }

    private var isCalenderClick = false
    private val genderCode = mutableSetOf(
        "${getEmojiByUnicode(0x2642)} Male",
        "${getEmojiByUnicode(0x2640)} Female"
    )

    private val dropDownArray: ArrayAdapter<String> by lazy {
        ArrayAdapter(requireContext(), R.layout.dropdown, genderCode.toTypedArray())
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FreeParentingBabyEditProfileFragmentBinding.bind(view)

        viewModel.event.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { err ->
                showErrorDialogBox(err)
            }
        }
        binding.userGenderEd.setAdapter(dropDownArray)
        profileDetail.getLoginResponse()?.let {
            setUI(it)
        }

        binding.userGenderEd.setOnItemClickListener { _, _, position, _ ->
            genderPosition = position
        }

        binding.updateInfoBtn.setOnClickListener {
            val name = binding.userNameEd.text.toString()
            val dob = binding.dateEd.text.toString()
            if (checkUserInput(name)) {
                setToastMsg("Invalid Name found!!")
                return@setOnClickListener
            }
            if (checkUserInput(dob)) {
                setToastMsg("Invalid Date of Birth found")
                return@setOnClickListener
            }
            if (genderPosition == null) {
                setToastMsg("Select Gender!!")
                return@setOnClickListener
            }
            val genderTxt = if (genderPosition == 0) "Male" else "Female"
            viewModel.updateProfileDetail(
                ChangeProfileRequest(
                    id = profileDetail.getLoginResponse()?.data?.id ?: profileDetail.getUserId()!!
                        .toInt(),
                    kidsDob = dob,
                    kidsName = name,
                    kidsGender = genderTxt
                )
            )
        }

        binding.dateEd.setOnClickListener {
            if (!isCalenderClick) {
                isCalenderClick = true
                activity?.calenderPicker(childFragmentManager, cancel = {
                    isCalenderClick = false
                }, dateListener = {
                    binding.dateEd.setText(it)
                    isCalenderClick = false
                })
            } else {
                activity?.toastMsg("Opening Date Picker ${getEmojiByUnicode(0x1F4C5)}")
            }
        }


        getProfileResponse()
        binding.toolbarNav.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUI(response: FreeParentingResponse) {
        binding.userTitle.text = response.data.kidsName.split("\\s".toRegex())[0] + "\n"

        binding.userProfileTxt.text =
            response.data.kidsName.first().uppercaseChar().toString()
        binding.userNameEd.setText(response.data.kidsName)
        binding.dateEd.setText(response.data.kidsDob)
        val gender = response.data.kidsGender
        if (gender.equals("male", true)) {
            genderPosition = 0
            binding.userGenderEd.setText(genderCode.elementAt(0), false)
        } else if (gender.equals("female", true)) {
            genderPosition = 1
            binding.userGenderEd.setText(genderCode.elementAt(1), false)
        }
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
                            setUI(res)
                        } ?: showErrorDialogBox("Failed to show user response")
                    }
                }
            }
        }
    }


    private fun setToastMsg(msg: String) {
        binding.root.showSnackBarMsg(
            msg,
            anchor = (activity as FreeParentingDemoActivity).getBottomNav()
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        binding.toolbarNav.topAppBar.setNavigationIcon(R.drawable.arrow)
        binding.toolbarNav.titleTxt.text = "Edit Profile"
        binding.toolbarNav.titleTxt.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
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
}