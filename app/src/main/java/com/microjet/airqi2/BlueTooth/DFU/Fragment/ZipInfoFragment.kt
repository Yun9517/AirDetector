package com.microjet.airqi2.BlueTooth.DFU.Fragment

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import com.microjet.airqi2.R


/**
 * Created by B00055 on 2018/3/26.
 */

class ZipInfoFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_zip_info, null)
        return AlertDialog.Builder(activity!!).setView(view).setTitle(R.string.dfu_file_info).setPositiveButton(R.string.ok, null).create()
    }
}