package com.microjet.airqi2.BroadReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Created by B00175 on 2017/11/9.
 */

class CharacteristicReceiver : BroadcastReceiver() {
    //private var nm: NotificationManager? = null
    val ACTION = "Characteristic"
    private val NOTIFY_ID = 1

    override fun onReceive(context: Context, intent: Intent) {
    }


}
