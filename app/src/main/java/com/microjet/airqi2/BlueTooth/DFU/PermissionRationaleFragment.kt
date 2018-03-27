package com.microjet.airqi2.BlueTooth.DFU

/**
 * Created by B00055 on 2018/3/26.
 */

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.microjet.airqi2.R

class PermissionRationaleFragment : DialogFragment() {

    private var mListener: PermissionDialogListener? = null

    interface PermissionDialogListener {
        fun onRequestPermission(permission: String?)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is PermissionDialogListener) {
            mListener = context
        } else {
            throw IllegalArgumentException("The parent activity must impelemnt PermissionDialogListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments
        val text = StringBuilder(getString(args!!.getInt(ARG_TEXT)))
        return AlertDialog.Builder(activity!!).setTitle(R.string.permission_title).setMessage(text)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, { dialog, which -> mListener!!.onRequestPermission(args.getString(ARG_PERMISSION)) }).create()
    }

    companion object {
        private val ARG_PERMISSION = "ARG_PERMISSION"
        private val ARG_TEXT = "ARG_TEXT"

        fun getInstance(aboutResId: Int, permission: String): PermissionRationaleFragment {
            val fragment = PermissionRationaleFragment()

            val args = Bundle()
            args.putInt(ARG_TEXT, aboutResId)
            args.putString(ARG_PERMISSION, permission)
            fragment.arguments = args

            return fragment
        }
    }
}
