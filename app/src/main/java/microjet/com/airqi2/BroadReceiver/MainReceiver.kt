package microjet.com.airqi2.BroadReceiver

import android.annotation.TargetApi
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.design.widget.NavigationView
import android.support.v4.app.NotificationCompat
import android.util.Log
import microjet.com.airqi2.MainActivity
import microjet.com.airqi2.R


/**
 * Created by B00175 on 2017/11/9.
 */

class MainReceiver : BroadcastReceiver() {
    //private var nm: NotificationManager? = null

    private val NOTIFY_ID = 1


    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getStringExtra("status")) {
            "ACTION_GATT_DISCONNECTED",
            "ACTION_GATT_DISCONNECTING"
            -> {
                var mainIntent = Intent("mainActivity")
                mainIntent.putExtra("status", "ACTION_GATT_DISCONNECTED")
                context.sendBroadcast(mainIntent)
            }
            "ACTION_GATT_CONNECTED",
            "ACTION_GATT_CONNECTING"
            -> {
                var mainIntent = Intent("mainActivity")
                mainIntent.putExtra("status", "ACTION_GATT_CONNECTED")
                context.sendBroadcast(mainIntent)
            }
            "disconnect" -> {
                var mainIntent = Intent("UartService")
                mainIntent.putExtra("status", "disconnect")
                context.sendBroadcast(mainIntent)
            }
            "connect" -> {
                var macAddress = intent.getStringExtra("mac")
                var mainIntent = Intent("UartService")
                mainIntent.putExtra("status", "connect")
                mainIntent.putExtra("mac",macAddress)
                context.sendBroadcast(mainIntent)
            }

            "B6"->{
                var mainIntent = Intent("mainActivity")
                mainIntent.putExtra("status","B6")
                mainIntent.putExtra("TVOCValue",intent.getStringExtra("TVOCValue"))
                mainIntent.putExtra("BatteryLife",intent.getStringExtra("BatteryLife"))
                context.sendBroadcast(mainIntent)

            }
            "message" -> {
                var mainIntent = Intent("UartService")
                mainIntent.putExtra("status", "message")
                context.sendBroadcast(mainIntent)
                Log.d("message","messageMAIN")
            }
            "callItems" -> {
                var mainIntent = Intent("UartService")
                mainIntent.putExtra("status", "callItems")
                context.sendBroadcast(mainIntent)
            }
            "checkItems" -> {
                var mainIntent = Intent("UartService")
                mainIntent.putExtra("status", "checkItems")
                context.sendBroadcast(mainIntent)
            }
            "setSampleRate" -> {
                var mainIntent = Intent("UartService")
                mainIntent.putExtra("status", "setSampleRate")
                context.sendBroadcast(mainIntent)
            }
            "callDeviceStartSample" -> {
                var mainIntent = Intent("UartService")
                mainIntent.putExtra("status", "callDeviceStartSample")
                context.sendBroadcast(mainIntent)
            }
        }

    }

    companion object {
        val ACTION = "Main"
    }

}
