package com.microjet.airqi2.Fragment

import android.app.Activity
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.microjet.airqi2.Account.AccountActiveActivity
import com.microjet.airqi2.Account.AccountRetryActivity
import com.microjet.airqi2.MainActivity
import com.microjet.airqi2.R


/**
 * Created by B00190 on 2018/5/29.
 */
class CheckFragment : DialogFragment() {
    fun newInstance(title: Int, activity: Activity?, howMany: Int, chooseMethod: String): CheckFragment {
        val frag = CheckFragment()
        val args = Bundle()
        args.putInt("title", title) //傳入title參數
        args.putInt("howMany", howMany)
        frag.setArguments(args)
        return frag
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreate(savedInstanceState)
        val title = arguments.getInt("title") //取得參數title
        val howMany = arguments.getInt("howMany")
        val chooseMethod = arguments.getString("chooseMethod")
        val whichActivity = AlertDialog.Builder(activity)
        when (howMany) {
        //zero Button
            0 -> {
                whichActivity.setView(R.layout.wait_progress_bar)
                        .setCancelable(false) // disable click back button
            }
        //one Button
            1 -> whichActivity.setPositiveButton("確定", { dialog, which ->
                when (activity::class.java.simpleName) {
                    "AccountManagementActivity" -> {
                        dismiss()
                    }
                    "AccountRetryActivity" -> {
                        if(chooseMethod == "Login"){
                            (activity as? AccountRetryActivity)?.Login()
                        }
                        if(chooseMethod == "dismiss") {
                            dismiss()
                        }
                    }
                    "MainActivity" -> {
                        if(chooseMethod == "Login"){
                            (activity as? MainActivity)?.Login()
                        }
                    }
                }

            }) 

        //two Button
            2 -> whichActivity.setPositiveButton("確定", { dialog, which ->
                when (activity::class.java.simpleName) {
                    "AccountActiveActivity" -> {
                        (activity as? AccountActiveActivity)?.doPositiveClick()
                    }
                }
            })
                    .setNegativeButton("取消", { dialog, which ->
                        dismiss()
                    })
        }
        return whichActivity.setTitle(title).create()
    }

}
