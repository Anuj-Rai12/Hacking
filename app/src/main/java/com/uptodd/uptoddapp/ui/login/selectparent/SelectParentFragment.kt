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
import com.uptodd.uptoddapp.database.logindetails.UserInfo
import com.uptodd.uptoddapp.databinding.FragmentSelectParentBinding
import com.uptodd.uptoddapp.utils.toastMsg

class SelectParentFragment : Fragment() {

    private lateinit var binding: FragmentSelectParentBinding
    private var parent: String = "mother"

     private var preferences: SharedPreferences?=null
    //private var preferences: SharedPreferences? = null
    /*var preferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_parent, container, false)
        binding.lifecycleOwner = this


        preferences=activity?.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)


        binding.imageButtonMother.setOnClickListener {
            parent = "mother"
            navigateToNext()
        }
        binding.imageButtonFather.setOnClickListener {
            parent = "father"
            navigateToNext()
        }
        binding.imageButtonGuardian.setOnClickListener {
            parent = "guardian"
            navigateToNext()
        }


        return binding.root
    }

    private fun navigateToNext() {
        val edit=preferences?.edit()
        if (edit==null){
            activity?.toastMsg("Oops Something Went Wrong !!")
            return
        }
        val value=edit.apply {
            putString("parentType", parent)
            putString(UserInfo::parentType.name, parent)
        }.commit()

        if (value){
            view?.findNavController()?.navigate(R.id.action_selectParentFragment_to_stageFragment)
        }else{
            activity?.toastMsg("Cannot capture this changes")
        }
    }

}