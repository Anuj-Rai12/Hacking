package com.uptodd.uptoddapp.ui.freeparenting.profile.childprofile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.FreeParentingDemoActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FreeParentingBabyEditProfileFragmentBinding
import com.uptodd.uptoddapp.datamodel.changeprofie.ChangeProfileRequest
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.FreeParentingResponse
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse
import com.uptodd.uptoddapp.ui.freeparenting.profile.repo.ProfileRepository
import com.uptodd.uptoddapp.ui.freeparenting.profile.viewmodel.ProfileViewModel
import com.uptodd.uptoddapp.utils.*
import com.uptodd.uptoddapp.utils.dialog.showDialogBox
import java.util.*

class EditProfileFragment : Fragment(R.layout.free_parenting_baby_edit_profile_fragment) {

    private lateinit var binding: FreeParentingBabyEditProfileFragmentBinding
    private val viewModel: ProfileViewModel by viewModels()
    private val profileDetail by lazy {
        LoginSingletonResponse.getInstance()
    }
    private var isCalenderClick = false
    private var genderSelection = ""

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FreeParentingBabyEditProfileFragmentBinding.bind(view)

        viewModel.event.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { err ->
                showErrorDialogBox(err)
            }
        }

        binding.genderGrpBtn.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.femaleGenderRadioBtn.id) {
                genderSelection = "Female"
                return@setOnCheckedChangeListener
            }
            if (checkedId == binding.maleGenderRadioBtn.id) {
                genderSelection = "Male"
                return@setOnCheckedChangeListener
            }
        }

        binding.saveBtn.setOnClickListener {
            val name = binding.nameEd.text.toString()
            val kidDob = binding.kidDobEd.text.toString()
            val kidName = binding.kidNameEd.text.toString()
            val phoneNum = binding.userPhoneEd.text.toString()

            if (checkUserInput(genderSelection)) {
                setToastMsg("Select Gender!!")
                return@setOnClickListener
            }

            if (checkUserInput(name)) {
                setToastMsg("Enter the Parent Name!!")
                return@setOnClickListener
            }

            if (checkUserInput(kidName)) {
                setToastMsg("Enter the Kid Name!!")
                return@setOnClickListener
            }

            if (checkUserInput(kidDob)) {
                setToastMsg("Select Kid's Date of Birth!!")
                return@setOnClickListener
            }

            if (checkUserInput(phoneNum) || !isValidPhone(phoneNum)) {
                setToastMsg("Invalid Phone number!!")
                return@setOnClickListener
            }
            val id =
                profileDetail.getUserId()?.toInt() ?: profileDetail.getLoginResponse()?.data?.id!!
            viewModel.updateProfileDetail(
                ChangeProfileRequest(
                    id = id,
                    kidsName = kidName,
                    kidsGender = genderSelection,
                    kidsDob = kidDob,
                    phone = phoneNum,
                    name = name
                )
            )


        }

        /*binding.updateInfoBtn.setOnClickListener {

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
        }*/


        binding.kidDobEd.setOnClickListener {
            if (!isCalenderClick) {
                isCalenderClick = true
                activity?.calenderPicker(childFragmentManager, cancel = {
                    isCalenderClick = false
                }, dateListener = {
                    binding.kidDobEd.setText(it)
                    isCalenderClick = false
                })
            } else {
                activity?.toastMsg("Opening Date Picker ${getEmojiByUnicode(0x1F4C5)}")
            }
        }

        profileDetail.getLoginResponse()?.let {
            setUI(it)
        } ?: run {
            setToastMsg("Cannot load User Detail")
        }

        getProfileResponse()
        binding.backIconImage.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUI(response: FreeParentingResponse) {


        binding.userProfileTxt.text = response.data.name.first().uppercaseChar().toString()

        binding.nameEd.setText(response.data.name)
        binding.kidNameEd.setText(response.data.kidsName)
        binding.kidDobEd.setText(response.data.kidsDob)
        binding.userEmailId.setText(response.data.email)
        binding.userPhoneEd.setText(response.data.phone)
        val gender = response.data.kidsGender.uppercase(Locale.ROOT)
        when (ProfileRepository.Companion.GENDER.valueOf(gender)) {
            ProfileRepository.Companion.GENDER.FEMALE -> {
                binding.femaleGenderRadioBtn.isChecked = true
            }
            ProfileRepository.Companion.GENDER.MALE -> {
                binding.maleGenderRadioBtn.isChecked = true
            }
        }

    }


    private fun getProfileResponse() {
        viewModel.profileResponse.observe(viewLifecycleOwner) { res ->
            res?.let {
                when (it) {
                    is ApiResponseWrapper.Error -> {
                        showPb()
                        if (it.data == null) {
                            it.exception?.localizedMessage?.let { err ->
                                showErrorDialogBox(err)
                            }
                        } else {
                            showErrorDialogBox("${it.data}")
                        }
                    }
                    is ApiResponseWrapper.Loading -> {
                        hidePb()
                    }
                    is ApiResponseWrapper.Success -> {
                        showPb()
                        val data = it.data as FreeParentingResponse?
                        data?.let { res ->
                            setUI(res)
                            setToastMsg("Successfully updated")
                        } ?: showErrorDialogBox("Failed to show user response")
                    }
                }
            }
        }
    }


    private fun setToastMsg(msg: String) {
        binding.root.showSnackbar(msg)
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        binding.userEmailId.isEnabled = false
        (activity as FreeParentingDemoActivity?)?.hideBottomNavBar()
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
        binding.saveBtn.invisible()
        binding.pbBtn.isVisible = true

    }

    private fun showPb() {
        binding.saveBtn.show()
        binding.pbBtn.isVisible = false
    }
}