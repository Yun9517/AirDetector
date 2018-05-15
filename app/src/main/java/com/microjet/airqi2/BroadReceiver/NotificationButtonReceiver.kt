package com.microjet.airqi2.BroadReceiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*
import android.app.NotificationManager



/**
 * Created by B00174 on 2018/5/10.
 */
class NotificationButtonReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val date = Date().time
        when(intent.action)
        {
            "action1"->{performAction1(context,date)

            }
            "action2"->{performAction2(context,date)}
            "action3"->{performAction3()}
        }
        //This is used to close the notification tray
        //   val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        //   context.sendBroadcast(it)
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(0x01)
        mNotificationManager.cancel(0x02)
    }

    fun performAction1(context: Context,nowTime:Long) {//設定明日再提醒
        val share = context.getSharedPreferences("NotificationAction", Activity.MODE_PRIVATE)
        share.edit().putString("nextNotification", "tomorrow").apply()
        share.edit().putString("now time", nowTime.toString()).apply()
        Log.d("NotificationButton","action 1")
    }

    fun performAction2(context: Context,nowTime:Long) {//設定五分鐘提醒
        val share = context.getSharedPreferences("NotificationAction", Activity.MODE_PRIVATE)
        share.edit().putString("nextNotification", "5min" ).apply()
        share.edit().putString("now time", nowTime.toString()).apply()
        Log.d("NotificationButton","action 2")
    }
    fun performAction3() {
        Log.d("NotificationButton","action 3")
    }
}