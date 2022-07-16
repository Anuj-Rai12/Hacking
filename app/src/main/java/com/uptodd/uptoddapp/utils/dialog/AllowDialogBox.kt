package com.uptodd.uptoddapp.utils.dialog

import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.uptodd.uptoddapp.R


fun Fragment.showDialogBox(
    title: String,
    desc: String,
    btn: String = "Ok",
    icon: Int = R.drawable.ic_baseline_info_24,
    cancel: String? = null,
    isCancel: Boolean = true,
    listener: () -> Unit
) {
    val material = MaterialAlertDialogBuilder(
        requireActivity(),
        R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog_simple
    )

    val dialog = material.setTitle(title)
        .setMessage(desc)
        .setIcon(icon)
        .setCancelable(isCancel)
        .setPositiveButton(btn) { dialog, _ ->
            listener.invoke()
            dialog.dismiss()
        }
    cancel?.let {
        dialog.setNegativeButton(it) { dialog, _ ->
            dialog.dismiss()
        }
    }
    dialog.show()
}