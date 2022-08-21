package com.uptodd.uptoddapp.ui.freeparenting.profile.childprofile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FreeParentingBabyEditProfileFragmentBinding

class EditProfileFragment : Fragment(R.layout.free_parenting_baby_edit_profile_fragment) {

    private lateinit var binding: FreeParentingBabyEditProfileFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FreeParentingBabyEditProfileFragmentBinding.bind(view)

    }

}