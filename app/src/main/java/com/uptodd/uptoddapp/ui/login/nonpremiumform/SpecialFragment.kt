package com.uptodd.uptoddapp.ui.login.nonpremiumform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FragmentNonpremiumNameBinding
import com.uptodd.uptoddapp.databinding.FragmentNonpremiumSpecialBinding


class SpecialFragment : Fragment() {

    lateinit var binding: FragmentNonpremiumSpecialBinding
    var viewModel:BirthViewModel?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentNonpremiumSpecialBinding.inflate(inflater,container,false)
        viewModel= ViewModelProvider(this)[BirthViewModel::class.java]
        binding.buttonNext.setOnClickListener {

                viewModel?.putSpecial(binding.editTextSpecial.text.toString())
                view?.findNavController()?.navigate(R.id.action_specialFragment_to_majorFragment)
            }
        return binding.root
    }
}