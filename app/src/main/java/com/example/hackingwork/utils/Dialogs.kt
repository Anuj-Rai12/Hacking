package com.example.hackingwork.utils

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.navigation.fragment.navArgs
import com.example.hackingwork.R
import com.google.firebase.auth.FirebaseAuth

class PasswordDialog : androidx.fragment.app.DialogFragment() {
    private val args: PasswordDialogArgs by navArgs()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alterDialog = AlertDialog.Builder(requireActivity()).setTitle(args.title)
        alterDialog.setMessage(args.message).setIcon(R.drawable.hacking_main_icon)
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

class ExtraDialog(
    private val title: String? = null,
    private val Msg: String? = null,
    private val flag: Boolean? = null,
    private val function: ((Boolean) -> Unit)? =null
) :
    androidx.fragment.app.DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alterDialog = AlertDialog.Builder(requireActivity()).setTitle(title ?: "Empty")
        alterDialog.setMessage(Msg ?: "Empty Message").setIcon(R.drawable.hacking_main_icon)
        if (flag == null || flag == false) {
            alterDialog.setPositiveButton("ok") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
        }
        else if (flag==true && title==GetConstStringObj.Create_Course_title){
            alterDialog.setPositiveButton("Yes") { _, _ ->
                function?.let {
                    it(flag)
                }
            }.setNeutralButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        }
        else {
            val cancel = if (flag == true) "Exit" else "Cancel"
            alterDialog.setPositiveButton("LogOut") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                activity?.finish()
            }.setNeutralButton(cancel) { _, _ ->
                activity?.finish()
            }
        }
        return alterDialog.create()
    }
    //8.At End of Password you may use $ symbol or Any Special Symbol
}