package com.uptodd.uptoddapp.utilities

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.erkutaras.showcaseview.ShowcaseManager
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.LayoutFullScreenDialogBinding
import com.uptodd.uptoddapp.utils.getColorValue

class ShowInfoDialog(val info: String) : DialogFragment() {

    lateinit var binding: LayoutFullScreenDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutFullScreenDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textInfo.text = info
        binding.okButton.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        fun showInfo(info: String, fragmentManager: FragmentManager) {
            val infoDialog = ShowInfoDialog(info)
            infoDialog.show(fragmentManager, ShowInfoDialog::class.java.name)
        }


        fun showHint(activity: Activity,view: View, title: String, desc: String,key:Int) {
            val builder = ShowcaseManager.Builder()
            builder.context(activity)
                .key("$key")
                //.developerMode(true)
                .view(view)
                .roundedRectangle()
                .descriptionImageRes(R.mipmap.ic_launcher_round)
                .descriptionTitle(title)
                .descriptionText(desc)
                .colorDescTitle(Color.WHITE)
                .colorDescText(Color.WHITE)
                .buttonText("Done")
                //.colorButtonBackground(activity.getColorValue(btnColor))
                .colorButtonText(Color.WHITE)
                //.colorBackground(activity.getColorValue(bgColor))
                .cancelButtonVisibility(false)
                .add()
                .build()
                .show()
        }

    }


}