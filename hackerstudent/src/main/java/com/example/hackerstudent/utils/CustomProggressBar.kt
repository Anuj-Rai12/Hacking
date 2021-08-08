package com.example.hackerstudent.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import com.example.hackerstudent.databinding.CustomProgressBarLayoutBinding
import javax.inject.Inject

class CustomProgressBar @Inject constructor() {
    private var alertDialog: AlertDialog? = null
    @SuppressLint("SourceLockedOrientationActivity")
    fun show(context: Context, title: CharSequence?, flag:Boolean=true) {
        val con = (context as Activity)
        val alertDialog = AlertDialog.Builder(con)
        val inflater = (con).layoutInflater
        val binding = CustomProgressBarLayoutBinding.inflate(inflater)
        title?.let {
            binding.textView.text = it
        }
        alertDialog.setView(binding.root)
        alertDialog.setCancelable(flag)
        this.alertDialog = alertDialog.create()
        this.alertDialog?.show()
    }
    fun dismiss()= alertDialog?.dismiss()
}