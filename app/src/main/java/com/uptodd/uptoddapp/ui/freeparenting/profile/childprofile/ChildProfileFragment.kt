package com.uptodd.uptoddapp.ui.freeparenting.profile.childprofile

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.FreeParentingDemoActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ChildProfileFragmentLayoutBinding
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse
import com.uptodd.uptoddapp.utils.getDate
import com.uptodd.uptoddapp.utils.getEmojiByUnicode

class ChildProfileFragment : Fragment(R.layout.child_profile_fragment_layout) {
    private lateinit var binding: ChildProfileFragmentLayoutBinding

    private val yrs = getDate("yyyy")
    private val profileInfo by lazy {
        LoginSingletonResponse.getInstance()
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ChildProfileFragmentLayoutBinding.bind(view)
        binding.childProfileTxt.text =
            profileInfo.getLoginResponse()?.data?.kidsName?.first()?.uppercaseChar().toString()
        val gender = profileInfo.getLoginResponse()?.data?.kidsGender
        binding.ageTitle.text = "${getEmojiByUnicode(0x1F476)} Age"
        binding.genderValue.text = if (gender?.equals("male", true)!!) {
            binding.genderTitle.text = "${getEmojiByUnicode(0x2642)} Gender"
            "boy"
        } else if (gender.equals("female", true)) {
            binding.genderTitle.text = "${getEmojiByUnicode(0x2640)} Gender"
            "girl"
        } else {
            "N/A"
        }
        val age = profileInfo.getLoginResponse()?.data?.kidsDob?.split("-")?.get(0)
        val babyAge = yrs.toString().toLong() - age.toString().toLong()
        val bybTxt = "<br><font color='#2ba0c4'><b>${babyAge} yrs old<b></font>"
        val name =
            profileInfo.getLoginResponse()?.data?.kidsName?.split("\\s".toRegex())?.get(0) + "\n"

        binding.profileTxt.text = name
        binding.profileTxt.append(Html.fromHtml(bybTxt))
        binding.ageValue.text = "$babyAge yrs old"
        binding.editBtn.setOnClickListener {
            val action =
                ChildProfileFragmentDirections.actionChildProfileFragmentToEditProfileFragment()
            findNavController().navigate(action)
        }
        binding.toolbarNav.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        binding.toolbarNav.topAppBar.setNavigationIcon(R.drawable.arrow)
        binding.toolbarNav.titleTxt.text = "Child Profile"
        binding.toolbarNav.titleTxt.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        (activity as FreeParentingDemoActivity?)?.hideBottomNavBar()
    }
}