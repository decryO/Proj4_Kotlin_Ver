package com.example.proj4_kotlin_ver

import androidx.appcompat.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.lang.IllegalStateException


class ListDialogFragment : DialogFragment() {

    private lateinit var listener: MyDialogFragmentListener

    // イベント アイテムが選択されたときに親フラグメントへイベントを配信する
    interface MyDialogFragmentListener {
        fun onDialogItemClick(value: Int, from: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when {
            context is MyDialogFragmentListener -> listener = context
            parentFragment is MyDialogFragmentListener -> listener = parentFragment as MyDialogFragmentListener
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val prefecturesList: Array<String> = arguments?.getStringArray("arrays") as Array<String>
            val from: Int = arguments?.getInt("from") as Int
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.notify_name)
                .setItems(prefecturesList) { _, which ->
                    listener.onDialogItemClick(which, from)
                }
                .setNegativeButton(R.string.dialog_close) { _, _ ->
                    listener.onDialogItemClick(0, 0)
                    dismiss()
                }
            builder.create()
        } ?: throw IllegalStateException()
    }
}