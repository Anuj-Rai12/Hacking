package com.uptodd.uptoddapp.ui.freeparenting.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.uptodd.uptoddapp.FreeParentingDemoActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ProfileLayoutFragmentBinding
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.FreeParentingResponse
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse
import com.uptodd.uptoddapp.ui.freeparenting.profile.repo.ProfileRepository
import com.uptodd.uptoddapp.ui.freeparenting.profile.viewmodel.ProfileViewModel
import com.uptodd.uptoddapp.utils.*
import com.uptodd.uptoddapp.utils.dialog.showDialogBox
import java.util.*

class ProfileFragment : Fragment(R.layout.profile_layout_fragment) {

    private lateinit var binding: ProfileLayoutFragmentBinding

    private val viewModel: ProfileViewModel by viewModels()

    private var isEditIconDisplayed = false

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

        binding.genderGrpBtn.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.femaleGenderRadioBtn.id) {
                setToastMsg("Female")
                return@setOnCheckedChangeListener
            }
            if (checkedId == binding.maleGenderRadioBtn.id) {
                setToastMsg("Male")
                return@setOnCheckedChangeListener
            }
        }

        /*      binding.updateInfoBtn.setOnClickListener {
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
                          loginSingletonResponse.getLoginResponse()?.data?.id
                              ?: loginSingletonResponse.getUserId()!!.toInt(),
                          name = name,
                          phone = phone,
                      )
                  )

              }*/
        binding.toolbarNav.accountIcon.setOnClickListener {
            if (!isEditIconDisplayed) {
                binding.editBtn.show()
            } else {
                binding.editBtn.hide()
            }
            isEditIconDisplayed = !isEditIconDisplayed
            /*val action = ProfileFragmentDirections.actionProfileFragmentToChildProfileFragment()
            findNavController().navigate(action)*/
        }
        getProfileResponse()

        binding.profileLayout.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, _, _, _ ->
            binding.editBtn.hide()
        })


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

                        if (it.data == null) {
                            it.exception?.localizedMessage?.let { err ->
                                showErrorDialogBox(err)
                            }
                        } else {
                            showErrorDialogBox("${it.data}")
                        }
                    }
                    is ApiResponseWrapper.Loading -> {
                        binding.userTitle.text = "${it.data}"
                    }
                    is ApiResponseWrapper.Success -> {
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
        binding.toolbarNav.accountIcon.setImageResource(R.drawable.ic_more)
        binding.toolbarNav.accountIcon.show()
        (activity as FreeParentingDemoActivity?)?.showBottomNavBar()
        binding.kidNameEd.isEnabled=false
        binding.userEmailId.isEnabled=false
        binding.userPhoneEd.isEnabled=false
        binding.kidDobEd.isEnabled=false
        binding.genderGrpBtn.getChildAt(0).isEnabled=false
        binding.genderGrpBtn.getChildAt(1).isEnabled=false
        viewModel.getProfile(
            loginSingletonResponse.getLoginResponse()?.data?.id?.toLong()
                ?: loginSingletonResponse.getUserId()!!
        )
    }

    @SuppressLint("SetTextI18n")
    private fun setUpUI(data: FreeParentingResponse) {
        binding.userTypeDesc.text = "Enroll For Parenting Program"
        binding.userProfileTxt.text = data.data.name.first().uppercaseChar().toString()
        binding.userTitle.text = data.data.name.split("\\s".toRegex())[0]
        binding.kidNameEd.setText(data.data.kidsName)
        binding.userEmailId.setText(data.data.email)
        binding.userPhoneEd.setText(data.data.phone)
        binding.kidDobEd.setText(data.data.kidsDob)
        val gender = data.data.kidsGender.uppercase(Locale.ROOT)
        when (ProfileRepository.Companion.GENDER.valueOf(gender)) {
            ProfileRepository.Companion.GENDER.FEMALE -> {
                binding.femaleGenderRadioBtn.isChecked = true
            }
            ProfileRepository.Companion.GENDER.MALE -> {
                binding.maleGenderRadioBtn.isChecked = true
            }
        }
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