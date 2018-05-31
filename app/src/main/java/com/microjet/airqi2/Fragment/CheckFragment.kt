package com.microjet.airqi2.Fragment

import android.app.Activity
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.microjet.airqi2.Account.AccountActiveActivity




/**
 * Created by B00190 on 2018/5/29.
 */
class CheckFragment: DialogFragment() {
    fun newInstance(title: Int, activity: Activity?): CheckFragment {
        val frag = CheckFragment()
        val args = Bundle()
        args.putInt("title", title) //傳入title參數
        frag.setArguments(args)
        return frag
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreate(savedInstanceState)
        val title = arguments.getInt("title") //取得參數title

        //回傳AlertDialog
        return AlertDialog.Builder(activity)
                .setTitle(title)
                .setPositiveButton("確定",{ dialog,which ->
                    when (activity::class.java.simpleName) {
                        "AccountActiveActivity" -> {
                            (activity as? AccountActiveActivity)?.doPositiveClick()
                        }
                    }
                })
                .setNegativeButton("取消",{ dialog,which ->
                    dismiss()
                })
                .create()
    }

}
