package com.uptodd.uptoddapp.utilities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.LayoutAddedDialogBinding

class AddedPopUpDialog(val title: String, val content: String):DialogFragment() {
    lateinit var binding:LayoutAddedDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutAddedDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.titleTxt.text = title
        binding.contentTxt.text = content
        binding.crossBtn.setOnClickListener{
            dismiss()
        }
        binding.checkBtn.setOnClickListener {
            dismiss()
        }
    }

    companion object
    {
        fun showInfo(info: String, content: String,fragmentManager: FragmentManager)
        {
            val infoDialog=AddedPopUpDialog(info,content)
            infoDialog.show(fragmentManager,AddedPopUpDialog::class.java.name)
        }
    }
    override fun show(manager: FragmentManager, tag: String?) {
        try{
            val ft = manager.beginTransaction()
            ft.add(this, tag)
            ft.commitAllowingStateLoss()
        }catch(e: Exception){
            Log.e("DIALOG",e.toString())
        }
    }
}