package com.uptodd.uptoddapp.ui.freeparenting.login

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.FreeParentingDemoActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.LoginParentingFragmentBinding
import com.uptodd.uptoddapp.utils.hide
import com.uptodd.uptoddapp.utils.invisible

class ParentingLoginFragment : Fragment(R.layout.login_parenting_fragment) {
    private lateinit var binding: LoginParentingFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LoginParentingFragmentBinding.bind(view)
        (activity as FreeParentingDemoActivity).setUpCustomActionBar("Login") {

        }

        binding.goToDemoDashBoard.setOnClickListener {
            binding.goToDemoDashBoard.invisible()
            binding.pbBtn.isVisible = true
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as FreeParentingDemoActivity).supportActionBar?.displayOptions =
            ActionBar.DISPLAY_SHOW_TITLE
        (activity as FreeParentingDemoActivity).supportActionBar?.setDisplayShowCustomEnabled(
            false
        )
        (activity as FreeParentingDemoActivity).supportActionBar?.title = "Login"
    }


}
