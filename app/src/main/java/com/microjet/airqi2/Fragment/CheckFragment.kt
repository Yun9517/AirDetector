package com.microjet.airqi2.Fragment

import android.app.Activity
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.microjet.airqi2.Account.AccountActiveActivity
import com.microjet.airqi2.Account.AccountManagementActivity
import com.microjet.airqi2.R


/**
 * Created by B00190 on 2018/5/29.
 */
class CheckFragment : DialogFragment() {
    fun newInstance(title: Int, message: Int, activity: Activity?, howMany: Int, clickMethodName: String): CheckFragment {
        val frag = CheckFragment()
        val args = Bundle()
        args.putInt("title", title) //傳入title參數
        args.putInt("message", message)
        args.putInt("howMany", howMany)
        args.putString("clickMethodNameKey", clickMethodName)
        frag.setArguments(args)
        return frag
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreate(savedInstanceState)
        val title = arguments.getInt("title") //取得參數title
        val message = arguments.getInt("message")
        val howMany = arguments.getInt("howMany")
        val strClickMethod = arguments.getString("clickMethodNameKey")
        val whichActivity = AlertDialog.Builder(activity)

        when (howMany) {
        //zero Button
            0 -> {
                when (strClickMethod) {
                    "wait" -> {
                        whichActivity.setView(R.layout.wait_progress_bar)
                                .setCancelable(false) // disable click back button
                    }
                }
            }
        //one Button
            1 -> whichActivity.setPositiveButton("確定", { dialog, which ->
                when (strClickMethod) {
                    "dismiss" -> {
                            dismiss()
                    }
                }
            })

        //two Button
            2 -> whichActivity.setPositiveButton("確定", { dialog, which ->
                when (strClickMethod) {
                    "doPositiveClick" -> (activity as? AccountActiveActivity)?.doPositiveClick()
                    "showEnableCloudUploadStat" -> (activity as? AccountManagementActivity)?.showEnableCloudUploadStat()
                    "showEnable3G_Network" -> (activity as? AccountManagementActivity)?.showEnable3G_Network()
                }
            })
                    .setNegativeButton("取消", { dialog, which ->
                        when(strClickMethod){
                            "showEnableCloudUploadStat"->(activity as? AccountManagementActivity)?.AccountActivityShow()
                            "showEnable3G_Network"->(activity as? AccountManagementActivity)?.AccountActivityShow()
                            else -> dismiss()
                        }
                    })
        }
        return whichActivity.setTitle(title).setMessage(message).create()
    }

}
