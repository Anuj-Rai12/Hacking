package com.uptodd.uptoddapp.utils

import com.google.android.gms.auth.api.credentials.HintRequest

object CaptureDeviceInformation{
    const val RequestCodeForPhone=121
    const val RequestCodeForEmail=111
}


fun hintRequest(): HintRequest = HintRequest.Builder()
    .setPhoneNumberIdentifierSupported(true)
    .build()