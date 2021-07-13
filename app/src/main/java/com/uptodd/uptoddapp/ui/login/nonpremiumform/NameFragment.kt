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
import com.uptodd.uptoddapp.databinding.FragmentPreBirthQuestionsBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences


class NameFragment : Fragment() {

    lateinit var binding: FragmentNonpremiumNameBinding
    var viewModel:BirthViewModel?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentNonpremiumNameBinding.inflate(inflater,container,false)
        viewModel= ViewModelProvider(this)[BirthViewModel::class.java]
        binding.buttonNext.setOnClickListener {

            if(binding.editTextToys.text.toString().isNotEmpty()) {

                viewModel?.putName(binding.editTextToys.text.toString())
                val stage = UptoddSharedPreferences.getInstance(requireContext()).getStage()
                if (stage == "pre birth" || stage=="prenatal") {
                    it?.findNavController()
                        ?.navigate(R.id.action_nameFragment_to_deliveryFragment)
                } else {
                    it?.findNavController()
                        ?.navigate(R.id.action_nameFragment_to_babyNameFragment2)
                }

            }
            else
                binding.editTextToys.error="Required"
        }
        return binding.root
    }
}