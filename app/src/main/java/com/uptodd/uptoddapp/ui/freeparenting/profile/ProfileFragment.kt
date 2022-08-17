package com.uptodd.uptoddapp.ui.freeparenting.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.FreeParentingDemoActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ProfileLayoutFragmentBinding
import com.uptodd.uptoddapp.utils.hide

class ProfileFragment : Fragment(R.layout.profile_layout_fragment) {

    private lateinit var binding: ProfileLayoutFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ProfileLayoutFragmentBinding.bind(view)

    }

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