package com.uptodd.uptoddapp.utilities

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.uptodd.uptoddapp.R

class CustomToast(
    val context: Context,
    val text: String,
    val layoutInflater: LayoutInflater,
    val findViewById: ViewGroup,)
{
    fun show()
    {
        val layout=layoutInflater.inflate(R.layout.custom_toast,findViewById)
        val toast=Toast(context)
        toast.view=layout
        toast.duration=Toast.LENGTH_LONG
        toast.setText(text)
        toast.show()
    }
}