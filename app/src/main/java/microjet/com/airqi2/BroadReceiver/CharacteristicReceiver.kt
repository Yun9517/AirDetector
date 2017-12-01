package microjet.com.airqi2.BroadReceiver

import android.annotation.TargetApi
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import microjet.com.airqi2.MainActivity

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
