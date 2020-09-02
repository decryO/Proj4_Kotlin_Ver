package com.example.proj4_kotlin_ver.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.proj4_kotlin_ver.R
import java.lang.IllegalStateException

class ProgressDialogFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val df = this as DialogFragment
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = it.layoutInflater
            val view = inflater.inflate(R.layout.dialog_progress, null)
            // layoutを適用する
            builder.setView(view)
            // Dialogの外をタップしてもキャンセルできなくする
            // DialogFragmentに対してsetCancelableをセットする
            this.isCancelable = false
            builder.create()
        } ?: throw IllegalStateException()
    }
}