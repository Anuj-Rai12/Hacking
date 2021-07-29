package com.example.hackingwork.utils

import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

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