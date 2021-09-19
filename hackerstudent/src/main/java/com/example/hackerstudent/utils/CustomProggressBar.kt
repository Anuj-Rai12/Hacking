package com.example.hackerstudent.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import com.example.hackerstudent.R
import com.example.hackerstudent.databinding.CustomProgressBarLayoutBinding
import javax.inject.Inject

class CustomProgressBar @Inject constructor() {
    private var alertDialog: AlertDialog? = null

    @SuppressLint("SourceLockedOrientationActivity")
    fun show(context: Context, title: CharSequence?, flag: Boolean = true) {
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

    fun dismiss() = alertDialog?.dismiss()
}


class SuccessOrFailedPayment @Inject constructor() {
    fun showPaymentDialog(text: String, file: Int = R.raw.payment_failed, context: Context) {
        val binding = CustomProgressBarLayoutBinding.inflate(LayoutInflater.from(context))
        val infoDialog: AlertDialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .create()
        binding.apply {
            progressBar.setAnimation(file)
            progressBar.scaleType = ImageView.ScaleType.FIT_XY
            progressBar.repeatCount = 1
            if (file != R.raw.payment_failed)//Success
                textView.text = text
            else {
                textView.hide()
                errorTxt.show()
                errorTxtTitle.show()

                errorTxt.text = text
                errorTxt.append(GetConstStringObj.Payment_ERROR)
            }
        }
        infoDialog.show()
    }
}