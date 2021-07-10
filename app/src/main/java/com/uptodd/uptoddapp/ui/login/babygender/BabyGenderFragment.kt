package com.uptodd.uptoddapp.ui.login.babygender

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FragmentBabyGenderBinding

class BabyGenderFragment : Fragment() {

    lateinit var binding:FragmentBabyGenderBinding

    var preferences:SharedPreferences?=null
    var editor:SharedPreferences.Editor?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_baby_gender,container,false)
        binding.lifecycleOwner=this

        preferences = activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        editor= preferences!!.edit()

        binding.imageButtonBoy.setOnClickListener{
            editor?.putString("gender","boy")?.apply()
            editor?.commit()
            view?.findNavController()?.navigate(BabyGenderFragmentDirections.actionBabyGenderFragmentToBabyNameFragment())
        }

        binding.imageButtonGirl.setOnClickListener{
            editor?.putString("gender","girl")?.apply()
            editor?.commit()
            view?.findNavController()?.navigate(BabyGenderFragmentDirections.actionBabyGenderFragmentToBabyNameFragment())
        }

        binding.buttonNotDisclose.setOnClickListener{
            editor?.putString("gender","notDisclose")?.apply()
            editor?.commit()
            view?.findNavController()?.navigate(BabyGenderFragmentDirections.actionBabyGenderFragmentToBabyNameFragment())
        }

        return binding.root
    }
}