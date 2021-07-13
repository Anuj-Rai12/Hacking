package com.uptodd.uptoddapp.ui.login.nonpremiumform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FragmentBabyNameBinding
import com.uptodd.uptoddapp.databinding.FragmentNonpremiumAnythingBinding


class AnyThingFragment: Fragment() {

    lateinit var binding: FragmentNonpremiumAnythingBinding
    var viewModel:BirthViewModel?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentNonpremiumAnythingBinding.inflate(inflater,container,false)
        viewModel= ViewModelProvider(this)[BirthViewModel::class.java]

        binding.buttonNext.setOnClickListener {

                viewModel?.putAnything(binding.editTextToys.text.toString())
                view?.findNavController()?.navigate(R.id.action_anyThingFragment_to_specialFragment)
        }
        return binding.root
    }
}