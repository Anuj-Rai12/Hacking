package com.example.hackingwork.utils

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.navigation.fragment.navArgs
import com.example.hackingwork.R
import com.google.firebase.auth.FirebaseAuth

class PasswordDialog(private val title: String? = null, private val Msg: String? = null) :
    androidx.fragment.app.DialogFragment() {
    private val args: PasswordDialogArgs by navArgs()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alterDialog = AlertDialog.Builder(requireActivity()).setTitle(title ?: args.title)
        alterDialog.setMessage(Msg ?: args.message).setIcon(R.drawable.hacking_main_icon)
        if (!args.flag && args.title != "Log Out!!") {
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