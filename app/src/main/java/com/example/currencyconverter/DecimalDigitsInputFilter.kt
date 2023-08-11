package com.example.currencyconverter

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Matcher
import java.util.regex.Pattern

class DecimalDigitsInputFilter(digitsBeforeZero: Int, digitsAfterZero: Int) : InputFilter {
    private var pattern : Pattern

    init {
        pattern = Pattern.compile("[0-9]{0," + (digitsBeforeZero-1) + "}+((\\.[0-9]{0," + (digitsAfterZero-1) + "})?)||(\\.)?")
    }

    override fun filter(
        p0: CharSequence?,
        p1: Int,
        p2: Int,
        p3: Spanned?,
        p4: Int,
        p5: Int
    ): CharSequence? {
        val matcher : Matcher = pattern.matcher(p3)
        if (!matcher.matches())
            return ""
        return null
    }

}