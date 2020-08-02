package com.example.proj4_kotlin_ver

import android.text.TextWatcher

interface CustomTextWatcher: TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
}