package com.uptodd.uptoddapp.utilities

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.*

class ChangeLanguage(val context: Context) {
    var preferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    fun setLanguage() {
        preferences = context.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        editor= preferences!!.edit()

        var language="English"
        var languageCode="en"
        var currentLanguage="English"
        if(preferences!!.contains("language"))
        {
            currentLanguage=preferences!!.getString("language", "English")!!
            if(currentLanguage=="English")
            {}
            else
            {language="Hindi";languageCode="hi"}
        }

        editor?.putString("language", language)
        editor?.commit()

        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getLanguage():String
    {
        val preferences: SharedPreferences=context.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        var language="english"
        if(preferences.contains("language"))
            language=preferences.getString("language","English")!!.toLowerCase()
        return language
    }
}