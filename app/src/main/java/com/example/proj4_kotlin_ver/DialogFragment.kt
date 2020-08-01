package com.example.proj4_kotlin_ver

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment

class DialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.notify_title)
            .setMessage(R.string.notify_description)
            .setPositiveButton("Done"){ dialog, id ->

            }
            .setNegativeButton("Cancel") { dialog, id ->

            }
        return builder.create()
    }
}