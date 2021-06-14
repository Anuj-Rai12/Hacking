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
import com.uptodd.uptoddapp.databinding.FragmentNonpremiumToysBinding


class ToysFragment : Fragment() {

    lateinit var binding: FragmentNonpremiumToysBinding
    var viewModel:BirthViewModel?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentNonpremiumToysBinding.inflate(inflater,container,false)
        viewModel= ViewModelProvider(this)[BirthViewModel::class.java]
        binding.buttonNext.setOnClickListener {

            if(binding.editTextToys.text.toString().isNotEmpty()) {
                viewModel?.putToys(binding.editTextToys.text.toString())
                view?.findNavController()?.navigate(R.id.action_toysFragment2_to_minutesFragment)
            }
            else
                binding.editTextToys.error="Required"
        }
        return binding.root
    }
}