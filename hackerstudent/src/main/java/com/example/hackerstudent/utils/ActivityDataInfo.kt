package com.example.hackerstudent.utils

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import javax.inject.Inject

class ActivityDataInfo : ActivityResultContract<InputDataBitch, OutPutDataBitch>() {
    override fun createIntent(context: Context, input: InputDataBitch?): Intent {
        return Intent(input?.intent)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): OutPutDataBitch {
        return OutPutDataBitch(
            requestCode = resultCode == Activity.RESULT_OK,
            email = intent?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        )
    }
}

class InputDataBitch(
    val intent: Intent
)

class OutPutDataBitch(
    val requestCode: Boolean,
    val email: String?
)

class CustomProgress @Inject constructor(private val customProgressBar: CustomProgressBar) {
    fun hideLoading() {
        customProgressBar.dismiss()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    fun showLoading(context: Context, string: String?, boolean: Boolean = false) {
        val con = context as Activity
        customProgressBar.show(con, string, boolean)
    }
}