package com.uptodd.uptoddapp.ui.login.nonpremiumform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.databinding.FragmentPreBirthQuestionsBinding

class PreBirthQuestionsFragment :Fragment() {

    lateinit var binding:FragmentPreBirthQuestionsBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentPreBirthQuestionsBinding.inflate(inflater,container,false)
        return binding.root
    }
}