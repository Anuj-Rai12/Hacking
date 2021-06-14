package com.uptodd.uptoddapp.ui.login.nonpremiumform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FragmentNonpremiumMinutesBinding
import com.uptodd.uptoddapp.databinding.FragmentNonpremiumNameBinding


class MinutesFragment : Fragment() {

    lateinit var binding: FragmentNonpremiumMinutesBinding
    var viewModel:BirthViewModel?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentNonpremiumMinutesBinding.inflate(inflater,container,false)
        viewModel= ViewModelProvider(this)[BirthViewModel::class.java]
        binding.buttonNext.setOnClickListener {

            if(binding.editTextMinutes.text.toString().isNotEmpty()) {
                viewModel?.putMinutes(binding.editTextMinutes.text.toString())
                view?.findNavController()?.navigate(R.id.action_minutesFragment_to_specialFragment)
            }
            else
                binding.editTextMinutes.error="Required"
        }
        return binding.root
    }
}