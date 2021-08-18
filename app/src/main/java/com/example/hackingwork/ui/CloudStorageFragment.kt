package com.example.hackingwork.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.hackingwork.R
import com.example.hackingwork.databinding.CloudStorageFragmentBinding
import com.example.hackingwork.utils.ExtraDialog
import com.example.hackingwork.utils.GetConstStringObj
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CloudStorageFragment : Fragment(R.layout.cloud_storage_fragment) {
    private lateinit var binding: CloudStorageFragmentBinding
    private var extraDialog: ExtraDialog? = null
    private var dialogFlag: Boolean? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CloudStorageFragmentBinding.bind(view)
        savedInstanceState?.let {
            dialogFlag = it.getBoolean(GetConstStringObj.Create_Course_title)
        }
        if (dialogFlag == true) {
            openDialog()
        }
        binding.CreateCourse.setOnClickListener {
            openDialog()
        }
    }

    private fun openDialog() {
        extraDialog = ExtraDialog(
            title = GetConstStringObj.Create_Course_title,
            Msg = GetConstStringObj.Create_Course_desc,
            flag = true
        ) {
            if (it)
                Toast.makeText(activity, "Successfully", Toast.LENGTH_SHORT).show()

        }
        extraDialog?.isCancelable=true
        extraDialog?.show(childFragmentManager,"create_course")
        dialogFlag = true
    }

    override fun onPause() {
        super.onPause()
        extraDialog?.dismiss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        dialogFlag?.let {
            outState.putBoolean(GetConstStringObj.Create_Course_title, it)
        }
    }
}