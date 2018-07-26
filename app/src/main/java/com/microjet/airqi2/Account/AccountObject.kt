package com.microjet.airqi2.Account

import android.app.Activity
import android.app.DialogFragment

/**
 * Created by B00190 on 2018/7/26.
 */
object AccountObject {
    var accountLoginStrResult: String = ""

    fun closeWaitDialog(activity: Activity){
        //獲得結果後，關閉所有dialog視窗
        val previousDialog = activity.fragmentManager.findFragmentByTag("dialog")
        if (previousDialog != null) {
            val dialog = previousDialog as DialogFragment
            dialog.dismissAllowingStateLoss()//處理縮小APP出現的沒回應事件
        }
    }
}