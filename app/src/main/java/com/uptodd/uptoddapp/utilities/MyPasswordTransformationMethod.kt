package com.uptodd.uptoddapp.utilities

import android.text.method.PasswordTransformationMethod
import android.view.View

class MyPasswordTransformationMethod : PasswordTransformationMethod() {
    override fun getTransformation(source: CharSequence, view: View): CharSequence {
        return PasswordCharSequence(source)
    }

    private inner class PasswordCharSequence     // Store char sequence
        (private val mSource: CharSequence) :
        CharSequence {

        override val length: Int
            get() = mSource.length

        override fun get(index: Int): Char {
            return '⬤'
        }

        override fun subSequence(start: Int, end: Int): CharSequence {
            return mSource.subSequence(start, end) // Return default
        }
    }
}