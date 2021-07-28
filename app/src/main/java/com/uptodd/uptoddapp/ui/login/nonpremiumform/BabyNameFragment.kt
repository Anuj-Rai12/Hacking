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

class BabyNameFragment : Fragment() {

    lateinit var binding: FragmentBabyNameBinding
    var viewModel:BirthViewModel?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentBabyNameBinding.inflate(inflater,container,false)
        viewModel= ViewModelProvider(this)[BirthViewModel::class.java]
        binding.buttonNext.text="Next"
        binding.buttonNext.setOnClickListener {

            if (binding.editTextName.text.toString().isNotEmpty()) {
                viewModel?.putBabyName(binding.editTextName.text.toString())
                BirthViewModel.npAcc.kidsName=binding.editTextName.text.toString()
                view?.findNavController()?.navigate(R.id.action_babyNameFragment2_to_dobFragment)
            }
            else
                binding.editTextName.error="Required"
        }
        return binding.root
    }
}