package com.uptodd.uptoddapp.ui.login.selectparent

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
import com.uptodd.uptoddapp.databinding.FragmentSelectParentBinding

class SelectParentFragment : Fragment() {

    lateinit var binding: FragmentSelectParentBinding
    var parent:String="mother"

    var preferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_select_parent,container,false)
        binding.lifecycleOwner = this

        preferences = activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        editor= preferences!!.edit()

        binding.imageButtonMother.setOnClickListener{
            parent="mother"
            navigateToNext()
        }
        binding.imageButtonFather.setOnClickListener{
            parent="father"
            navigateToNext()
        }
        binding.imageButtonGuardian.setOnClickListener{
            parent="guardian"
            navigateToNext()
        }


        return binding.root
    }

    private fun navigateToNext()
    {
        editor?.putString("parentType",parent)
        editor?.commit()
        view?.findNavController()?.navigate(R.id.action_selectParentFragment_to_stageFragment)
    }

}