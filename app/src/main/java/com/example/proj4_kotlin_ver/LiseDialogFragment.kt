package com.example.proj4_kotlin_ver

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment


class LiseDialogFragment : DialogFragment() {

    private lateinit var listener: MyDialogFragmentListener

    // イベント アイテムが選択されたときに親フラグメントへイベントを配信する
    interface MyDialogFragmentListener {
        fun onDialogItemClick(num: Int, from: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when {
            context is MyDialogFragmentListener -> listener = context
            parentFragment is MyDialogFragmentListener -> listener = parentFragment as MyDialogFragmentListener
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val prefecturesList: Array<String> = arguments?.getStringArray("arrays") as Array<String>
        val from: Int = arguments?.getInt("from") as Int
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.notify_name)
            .setItems(prefecturesList) { _, which ->
                listener.onDialogItemClick(which, from)
            }
            .setNegativeButton("Cancel") { _, _ ->
                dismiss()
            }
        return builder.create()
    }
}