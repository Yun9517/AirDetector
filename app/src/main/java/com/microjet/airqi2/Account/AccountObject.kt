package com.microjet.airqi2.Account

import android.app.Activity
import android.app.DialogFragment
import android.util.Log
import com.microjet.airqi2.Fragment.CheckFragment
import com.microjet.airqi2.R

/**
 * Created by B00190 on 2018/7/26.
 */
object AccountObject {
    var accountLoginStrResult: String = ""
    var accountForgetStrResult: String = ""
    var accountRegisterStrResult: String = ""

    fun openWatiDialog(activity: Activity) {
        Log.e("activity.packageName", activity.localClassName)
        var messageString: Int = 0
        when (activity.localClassName) {
            "Account.AccountForgetPasswordActivity" -> messageString = R.string.dialog_forgetPassword_emailSend
            "Account.AccountRegisterActivity" -> messageString = R.string.dialog_forgetPassword_emailSend
            else -> messageString = R.string.wait_Login
        }
        val newFrage = CheckFragment().newInstance(R.string.remind, messageString, activity, 0, "wait")
        newFrage.setCancelable(false)
        newFrage.show(activity.fragmentManager, "dialog")
    }

    fun closeWaitDialog(activity: Activity) {
        //獲得結果後，關閉所有dialog視窗
        val previousDialog = activity.fragmentManager.findFragmentByTag("dialog")
        if (previousDialog != null) {
            val dialog = previousDialog as DialogFragment
            dialog.dismissAllowingStateLoss()//處理縮小APP出現的沒回應事件
        }
    }
}