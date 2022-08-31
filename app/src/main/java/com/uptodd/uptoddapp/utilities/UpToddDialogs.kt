package com.uptodd.uptoddapp.utilities

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.navigation.NavController
import com.uptodd.uptoddapp.R
import pl.droidsonroids.gif.GifImageView

class UpToddDialogs(val context: Context) {

    private var upToddDialogListener: UpToddDialogListener? = null
    private val dialog = Dialog(context)

    init {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        ChangeLanguage(context).setLanguage()
    }

    fun showInfoDialog(text: String,buttonText: String,upToddClickListener: UpToddDialogListener):UpToddDialogs
    {
        this.upToddDialogListener = upToddClickListener
        dialog.setContentView(R.layout.msg_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val dialogButton: Button = dialog.findViewById(R.id.msg_dialog_button)
        val dialogText = dialog.findViewById<TextView>(R.id.msg_dialog_text)
        dialogText.text = text
        dialogButton.text = buttonText
        dialogButton.setOnClickListener {
            upToddDialogListener!!.onDialogButtonClicked(dialog)
        }
        dialog.setCancelable(false)
        upToddDialogListener!!.onDialogReady(dialog, dialogText, dialogButton)
        dialog.setOnCancelListener {
            upToddClickListener.onDialogCancelled(dialog)
        }
        dialog.setOnDismissListener {
            upToddClickListener.onDialogDismiss()
        }
        dialog.show()
        return this
    }

    fun showDialog(
        gif: Int,
        text: String,
        buttonText: String,
        upToddClickListener: UpToddDialogListener
    ): UpToddDialogs {
        this.upToddDialogListener = upToddClickListener
        dialog.setContentView(R.layout.gif_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val dialogGIF = dialog.findViewById<GifImageView>(R.id.gif_dialog_gif)
        val dialogButton: Button = dialog.findViewById(R.id.gif_dialog_button)
        val dialogText = dialog.findViewById<TextView>(R.id.gif_dialog_text)
        dialogText.text = text
        dialogGIF.setImageResource(gif)
        dialogButton.text = buttonText
        dialogButton.setOnClickListener {
            upToddDialogListener!!.onDialogButtonClicked(dialog)
        }
        dialog.setCancelable(false)
        upToddDialogListener!!.onDialogReady(dialog, dialogText, dialogButton, dialogGIF)
        dialog.setOnCancelListener {
            upToddClickListener.onDialogCancelled(dialog)
        }
        dialog.show()
        if (gif == R.drawable.gif_done) {
            val mPlayer: MediaPlayer = MediaPlayer.create(context, R.raw.ting)
            mPlayer.start()
        }
        return this
    }


    fun showSetUpsDialog(
        gif: Int,
        text: String,
        buttonText: String,
        upToddClickListener: UpToddDialogListener
    ): UpToddDialogs {
        this.upToddDialogListener = upToddClickListener
        dialog.setContentView(R.layout.gif_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val dialogGIF = dialog.findViewById<GifImageView>(R.id.gif_dialog_gif)
        val dialogButton: Button = dialog.findViewById(R.id.gif_dialog_button)
        val dialogText = dialog.findViewById<TextView>(R.id.gif_dialog_text)
        dialogText.text = text
        dialogGIF.setImageResource(gif)
        dialogButton.text = buttonText
        dialogButton.visibility=View.INVISIBLE
        val timerCounter  =object : CountDownTimer((1000*15).toLong(), (1000).toLong()){
            override fun onFinish()
            {
                dialog.dismiss()
            }

            override fun onTick(milliFinished: Long) {
                dialogText.text=context.getString(R.string.setupLoading)+"\n"+"${milliFinished/1000}"

            }
        }
        dialog.setOnDismissListener {
            upToddDialogListener?.onDialogDismiss()
        }

        timerCounter.start()
        upToddDialogListener!!.onDialogReady(dialog, dialogText, dialogButton, dialogGIF)
        dialog.setOnCancelListener {
            upToddClickListener.onDialogCancelled(dialog)
        }
        dialog.show()
        if (gif == R.drawable.gif_done) {
            val mPlayer: MediaPlayer = MediaPlayer.create(context, R.raw.ting)
            mPlayer.start()
        }
        return this
    }

    fun showLoadingDialog(navController: NavController, goBack: Boolean = true) {
        this.showDialog(
            R.drawable.gif_loading,
            context.getString(R.string.loading),
            context.getString(R.string.cancel),
            object : UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {}
                override fun onDialogReady(
                    dialog: Dialog,
                    dialogText: TextView,
                    dialogButton: Button,
                    dialogGIF: GifImageView
                ) {
//                    dialogButton.visibility = View.INVISIBLE
                    dialog.setCancelable(true)
                    dialogButton.setOnClickListener {
                        dialog.cancel()
                    }
                }

                override fun onDialogCancelled(dialog: Dialog) {
                    if (goBack)
                        navController.navigateUp()
                }
            })
    }

    fun showOnlyLoadingDialog(msg:String?=null) {
        this.showDialog(
            R.drawable.gif_loading,
            msg?:context.getString(R.string.loading),
            context.getString(R.string.cancel),
            object : UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {}
                override fun onDialogReady(
                    dialog: Dialog,
                    dialogText: TextView,
                    dialogButton: Button,
                    dialogGIF: GifImageView
                ) {
                   dialogButton.visibility = View.INVISIBLE
                    dialog.setCancelable(false)

                }

                override fun onDialogCancelled(dialog: Dialog) {

                }
            })
    }

    fun showSetupDialog(navController: NavController, goBack: Boolean = false) {
        this.showDialog(
            R.drawable.gif_loading,
           context.getString(R.string.setupLoading),
            context.getString(R.string.cancel),
            object : UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {}
                override fun onDialogReady(
                    dialog: Dialog,
                    dialogText: TextView,
                    dialogButton: Button,
                    dialogGIF: GifImageView
                ) {

                    val timerCounter  =object : CountDownTimer((1000*15).toLong(), (1000).toLong()){
                        override fun onFinish()
                        {
                            dialog.dismiss()
                        }

                        override fun onTick(milliFinished: Long) {
                            dialogText.text=context.getString(R.string.setupLoading)+"${milliFinished/1000}"

                        }
                    }
                    timerCounter.start()
                    dialogButton.visibility = View.GONE
                }

                override fun onDialogCancelled(dialog: Dialog) {
                    if (goBack)
                        navController.navigateUp()
                }
            })


    }


    fun showUploadDialog() {
        this.showDialog(
            R.drawable.gif_upload,
            context.getString(R.string.sending_data),
            context.getString(R.string.cancel),
            object : UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {}
                override fun onDialogReady(
                    dialog: Dialog,
                    dialogText: TextView,
                    dialogButton: Button,
                    dialogGIF: GifImageView
                ) {
                    dialogButton.visibility = View.INVISIBLE
                }
            })
    }

    fun dismissDialog() {
        if (dialog.isShowing)
            dialog.dismiss()
    }

    fun showLoginDialog() {
        this.showDialog(
            R.drawable.gif_loading,
            context.getString(R.string.loading_please_wait),
            context.getString(R.string.cancel),
            object : UpToddDialogListener {
                override fun onDialogButtonClicked(dialog: Dialog) {}
                override fun onDialogReady(
                    dialog: Dialog,
                    dialogText: TextView,
                    dialogButton: Button,
                    dialogGIF: GifImageView
                ) {
                    dialogButton.visibility = View.INVISIBLE
                }
            })
    }

    interface UpToddDialogListener {
        fun onDialogButtonClicked(dialog: Dialog)
        fun onDialogReady(
            dialog: Dialog,
            dialogText: TextView,
            dialogButton: Button,
            dialogGIF: GifImageView
        ) {
        }
        fun onDialogReady(
            dialog: Dialog,
            dialogText: TextView,
            dialogButton: Button
        ) {
        }

        fun onDialogCancelled(dialog: Dialog) {}
        fun onDialogDismiss()
        {}
    }

}