package com.example.proj4_kotlin_ver

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.lang.IllegalStateException

/*
 DescriptionDialogFragmentで渡す値で表示するtextを変更しようとすると
 java.lang.IllegalStateException: Fragment already addedのエラーが出る(2回以上setArgumentすると出るらしい)
 ので、パーミッションが許可されていないときのダイアログはこちらにする
 */
class PermissionDENIEDDialogFragment: DialogFragment() {

    private lateinit var listener: PermissionDENIEDDialogListener
    private lateinit var permissionDENIED: String

    interface PermissionDENIEDDialogListener {
        fun onDENIEDDialogClick()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        permissionDENIED = resources.getString(R.string.permission_DENIED_description)

        try {
            listener = context as PermissionDENIEDDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            builder.setMessage(permissionDENIED)
                .setPositiveButton(getString(R.string.dialog_close)) { _, _ ->
                    listener.onDENIEDDialogClick()
                }
            builder.create()
        } ?: throw IllegalStateException()
    }

    override fun onCancel(dialog: DialogInterface) {
        listener.onDENIEDDialogClick()
        super.onCancel(dialog)
    }
}