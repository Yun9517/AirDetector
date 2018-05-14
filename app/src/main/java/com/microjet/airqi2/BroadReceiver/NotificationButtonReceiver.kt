package com.microjet.airqi2.BroadReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Created by B00174 on 2018/5/10.
 */
class NotificationButtonReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        when(intent.action)
        {
            "action1"->{performAction1()}
            "action2"->{performAction2()}
            "action3"->{performAction3()}
        }
        //This is used to close the notification tray
        //   val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        //   context.sendBroadcast(it)
    }

    fun performAction1() {//設定幾分鐘提醒
        Log.d("NotificationButton","action 1")
    }

    fun performAction2() {//設定明日再提醒
        Log.d("NotificationButton","action 2")
    }
    fun performAction3() {
        Log.d("NotificationButton","action 3")
    }
}