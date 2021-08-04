package com.uptodd.uptoddapp.ui.other

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.BuildConfig
import com.uptodd.uptoddapp.databinding.FragmentUpdateAppBinding

class FragmentUpdateApp:Fragment() {

    var binding:FragmentUpdateAppBinding?=null

    companion object
    {
        var isOutDated=false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        isOutDated=true

        binding= FragmentUpdateAppBinding.inflate(inflater,container,false)


        val actionBar=requireActivity()as AppCompatActivity
        actionBar?.supportActionBar?.hide()

        binding?.updateButton?.setOnClickListener {
            val appId=BuildConfig.APPLICATION_ID

            try {
                requireActivity()?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appId")))
            }
            catch (e:Exception)
            {
                requireActivity()?.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?id=$appId")))

            }

        }

        return binding?.root
    }


}