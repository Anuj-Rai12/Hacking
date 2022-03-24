package com.uptodd.uptoddapp.utilities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.LayoutFullScreenDialogBinding

class ShowInfoDialog(val info:String) :DialogFragment() {

    lateinit var binding:LayoutFullScreenDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= LayoutFullScreenDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textInfo.text = info
    binding.okButton.setOnClickListener {
        dismiss()
    }
    }

    companion object
    {
        fun showInfo(info: String,fragmentManager:FragmentManager)
        {
            val infoDialog=ShowInfoDialog(info)
            infoDialog.show(fragmentManager,ShowInfoDialog::class.java.name)
        }

    }


}