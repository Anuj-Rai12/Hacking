package com.example.hackerstudent.utils

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.navigation.fragment.navArgs
import com.example.hackerstudent.R
import com.google.firebase.auth.FirebaseAuth

class PasswordDialog : androidx.fragment.app.DialogFragment() {
    private val args: PasswordDialogArgs by navArgs()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alterDialog = AlertDialog.Builder(requireActivity()).setTitle(args.title)
        alterDialog.setMessage(args.message).setIcon(R.drawable.hacking_main_icon)
        if (!args.flag && args.title != "LogOut!!") {
            alterDialog.setPositiveButton("ok") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
        } else {
            alterDialog.setPositiveButton("LogOut") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                activity?.finish()
            }.setNeutralButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
        }
        return alterDialog.create()
    }
    //8.At End of Password you may use $ symbol or Any Special Symbol
}

class ExtraDialog(
    private val title: String? = null,
    private val Msg: String? = null,
    private val flag: Boolean? = null,
    private val itemClicked: (() -> Unit?)? = null
) :
    androidx.fragment.app.DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alterDialog = AlertDialog.Builder(requireActivity()).setTitle(title ?: "Empty")
        alterDialog.setMessage(Msg ?: "Empty Message").setIcon(R.drawable.hacking_main_icon)
        if (flag == null || flag == false) {
            alterDialog.setPositiveButton("ok") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
        } else {
            val cancel = if (flag == true) "Exit" else "Cancel"
            val btn = if (title == "Update") "Update" else "LogOut"
            alterDialog.setPositiveButton(btn) { op, _ ->
                if (btn == "LogOut") {
                    FirebaseAuth.getInstance().signOut()
                    activity?.finish()
                } else {
                    itemClicked?.invoke()
                    op.dismiss()
                }
            }.setNeutralButton(cancel) { _, _ ->
                activity?.finish()
            }
        }
        return alterDialog.create()
    }
}