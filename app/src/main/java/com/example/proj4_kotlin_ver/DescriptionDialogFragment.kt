package com.example.proj4_kotlin_ver

import androidx.appcompat.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.lang.IllegalStateException

class DescriptionDialogFragment: DialogFragment() {

    private lateinit var listener: DescriptionDialogListener
    private lateinit var permissionDescription: String
    private lateinit var permissionDescriptionQ: String

    interface DescriptionDialogListener {
        fun onDescriptionDialogClick()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Attachされていない段階でgetStringすると落ちる
        permissionDescription = resources.getString(R.string.permission_description)
        permissionDescriptionQ = resources.getString(R.string.permission_description_Q)

        try {
            listener = context as DescriptionDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            var description = ""

            when(arguments?.getInt("descriptionNumber")) {
                0 -> description = permissionDescription
                1 -> description = permissionDescriptionQ
            }
            builder.setMessage(description)
                .setPositiveButton(getString(R.string.dialog_close)) { _, _ ->
                    listener.onDescriptionDialogClick()
                }
            builder.create()
        } ?: throw IllegalStateException()
    }

    override fun onCancel(dialog: DialogInterface) {
        listener.onDescriptionDialogClick()
        super.onCancel(dialog)
    }
}