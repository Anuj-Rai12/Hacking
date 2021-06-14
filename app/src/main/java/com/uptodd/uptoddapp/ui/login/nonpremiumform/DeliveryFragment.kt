package com.uptodd.uptoddapp.ui.login.nonpremiumform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FragmentNonpremiumAnythingBinding
import com.uptodd.uptoddapp.databinding.FragmentNonpremiumDeliveryBinding


class DeliveryFragment: Fragment() {

    lateinit var binding: FragmentNonpremiumDeliveryBinding
    var viewModel:BirthViewModel?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentNonpremiumDeliveryBinding.inflate(inflater,container,false)
        viewModel= ViewModelProvider(this)[BirthViewModel::class.java]
        binding.buttonNext.setOnClickListener {

            if(binding.editTextToys.text.toString().isNotEmpty())
            {
                viewModel?.putDelivery(binding.editTextToys.text.toString())
                view?.findNavController()?.navigate(R.id.action_deliveryFragment_to_anyThingFragment)
            }
            else
                binding.editTextToys.error="Required"
        }
        return binding.root
    }
}