package com.example.hackingwork.utils

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.example.hackingwork.R
import com.example.hackingwork.databinding.CustomDialogForUserBinding
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
    private val function: ((Boolean) -> Unit)? = null
) :
    androidx.fragment.app.DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alterDialog = AlertDialog.Builder(requireActivity()).setTitle(title ?: "Empty")
        alterDialog.setMessage(Msg ?: "Empty Message").setIcon(R.drawable.hacking_main_icon)
        if (flag == null || flag == false) {
            alterDialog.setPositiveButton("ok") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
        } else if (flag == true && title == GetConstStringObj.Create_Course_title) {
            alterDialog.setPositiveButton("Yes") { dialog, _ ->
                function?.let {
                    it(flag)
                }
                dialog.dismiss()
            }.setNeutralButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        } else {
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

class DialogsForUser(
    private val phone: String? = null,
    private val udi: String? = null,
    private val token: String? = null,
    private val returnValue: ((String) -> Unit)? = null
) : DialogFragment() {
    private lateinit var alertDialogBinding: CustomDialogForUserBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        alertDialogBinding = CustomDialogForUserBinding.inflate(layoutInflater)
        val alertDialog =
            AlertDialog.Builder(requireActivity()).setView(alertDialogBinding.root)
                .setTitle("User Info :)")

        alertDialogBinding.copyPhone.setOnClickListener {
            returnValue?.invoke(phone!!)
        }

        alertDialogBinding.copyUdi.setOnClickListener {
            returnValue?.invoke(udi!!)
        }
        alertDialogBinding.copyToken.setOnClickListener {
            returnValue?.invoke(token!!)
        }
        return alertDialog.create()
    }
}