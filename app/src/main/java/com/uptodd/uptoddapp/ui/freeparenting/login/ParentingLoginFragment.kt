package com.uptodd.uptoddapp.ui.freeparenting.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.LoginParentingFragmentBinding

class ParentingLoginFragment : Fragment(R.layout.login_parenting_fragment) {
    private lateinit var binding: LoginParentingFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LoginParentingFragmentBinding.bind(view)
        binding.goToDemoDashBoard.setOnClickListener {
            val action=ParentingLoginFragmentDirections
                .actionParentingLoginFragmentToFreeDemoBashBoardFragment()
            findNavController().navigate(action)
//            binding.goToDemoDashBoard.invisible()
//            binding.pbBtn.isVisible = true
        }
    }


}
