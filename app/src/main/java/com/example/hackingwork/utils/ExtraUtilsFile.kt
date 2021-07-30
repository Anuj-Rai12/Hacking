package com.example.hackingwork.utils

import android.accounts.AccountManager
import android.content.Intent
import android.os.Build
import com.google.android.gms.auth.GoogleAuthUtil

fun getIntent(): Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    AccountManager.newChooseAccountIntent(
        null,
        null,
        arrayOf(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE),
        null,
        null,
        null,
        null
    )
} else {
    AccountManager.newChooseAccountIntent(
        null,
        null,
        arrayOf(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE),
        false,
        null,
        null,
        null,
        null
    )
}