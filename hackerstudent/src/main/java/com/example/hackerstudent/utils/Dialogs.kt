package com.example.hackerstudent.utils

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.navArgs
import com.example.hackerstudent.ClientActivity
import com.example.hackerstudent.R
import com.example.hackerstudent.TAG
import com.google.firebase.auth.FirebaseAuth
import com.stepstone.apprating.AppRatingDialog
import java.util.*
import javax.inject.Inject

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


class RatingDialogs @Inject constructor() {

    fun getDialog(fragmentActivity: FragmentActivity, fragment: Fragment,title: String) {
        AppRatingDialog.Builder()
            .setPositiveButtonText("Submit")
            .setNegativeButtonText("Cancel")
            .setNeutralButtonText("Later")
            .setNoteDescriptions(
                listOf(
                    "Very Bad",
                    "Not good",
                    "Quite ok",
                    "Very Good",
                    "Excellent !!!"
                )
            )
            .setDefaultRating(3)
            .setTitle(title)
            .setDescription("Please select some stars and give your feedback")
            .setCommentInputEnabled(true)
            .setStarColor(R.color.cheery_red)
            .setNoteDescriptionTextColor(R.color.black)
            .setTitleTextColor(R.color.my_color)
            .setDescriptionTextColor(R.color.black)
            .setHint("Please write your comment here ...")
            .setHintTextColor(R.color.forget_text_color)
            .setCommentTextColor(R.color.forget_text_color)
            .setCommentBackgroundColor(R.color.white)
            .setWindowAnimation(R.style.MyDialogFadeAnimation)
            .setCancelable(false)
            .setCanceledOnTouchOutside(false)
            .create(fragmentActivity)
            .setTargetFragment(fragment, 23) // only if listener is implemented by fragment
            .show()
    }

}