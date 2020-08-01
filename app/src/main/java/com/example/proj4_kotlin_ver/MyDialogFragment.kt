package com.example.proj4_kotlin_ver

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class MyDialogFragment : DialogFragment() {

//    internal lateinit var listener: MyDialogFragment
//
//    // イベント ボタンが選択されたときに親へイベントを配信する
//    interface MyDialogFragment {
//        fun onDialogPositiveClick(dialog: DialogFragment)
//        fun onDialogNegativeClick(dialog: DialogFragment)
//    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val prefecturesList: Array<String> = arguments?.getStringArray("arrays") as Array<String>
        val from: Int = arguments?.getInt("from") as Int
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.notify_title)
            .setItems(prefecturesList) { _, which ->

                // activity as MapsActivity この書き方をしないとNullエラーが出るので注意
                val act = activity as MapsActivity
                act.onReturnValue(prefecturesList[which], from)
            }
            .setNegativeButton("Cancel") { _, _ ->
                dismiss()
            }
        return builder.create()
    }
}