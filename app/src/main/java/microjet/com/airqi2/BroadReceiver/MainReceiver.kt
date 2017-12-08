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
                var mainIntent = Intent("UartService")
                var macAddress = intent.getStringExtra("mac")
                mainIntent.putExtra("status", "connect")
                mainIntent.putExtra("mac",macAddress)
                context.sendBroadcast(mainIntent)
            }
            "close" -> {
                var mainIntent = Intent("UartService")
                mainIntent.putExtra("status", "close")
                context.sendBroadcast(mainIntent)
            }

            "B6"->{
                var mainIntent = Intent("mainActivity")
                mainIntent.putExtra("status","B6")

                mainIntent.putExtra("TEMPValue",intent.getStringExtra("TEMPValue"))
                mainIntent.putExtra("HUMIValue",intent.getStringExtra("HUMIValue"))
                mainIntent.putExtra("TVOCValue",intent.getStringExtra("TVOCValue"))
                mainIntent.putExtra("eCO2Value",intent.getStringExtra("eCO2Value"))

                mainIntent.putExtra("BatteryLife",intent.getStringExtra("BatteryLife"))
                context.sendBroadcast(mainIntent)

            }
            "B5"->{
                var mainIntent = Intent("mainActivity")
                mainIntent.putExtra("status","B5")
                mainIntent.putExtras(intent)
                context.sendBroadcast(mainIntent)
               // var bundle= mainIntent.putExtra("TVOCValue",intent.getBundleExtra("result"))
              //  var mydata=bundle.getParcelableArrayExtra("resultSet")
              //  mainIntent.putExtra("TVOCValue",intent.getStringExtra("TVOCValue"))
              //  mainIntent.putExtra("BatteryLife",intent.getStringExtra("BatteryLife"))
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
                var SampleTime= intent.getIntExtra("SampleTime",2)
                var mainIntent = Intent("UartService")
                mainIntent.putExtra("status", "setSampleRate")
                mainIntent.putExtra("SampleTime",SampleTime)
                context.sendBroadcast(mainIntent)
            }
            "getSampleRate" -> {
                var mainIntent = Intent("UartService")
                mainIntent.putExtra("status", "getSampleRate")
                when( intent.getStringExtra("callFromConnect"))
                {
                    "yes"-> mainIntent.putExtra("callFromConnect", "yes")
                    else -> mainIntent.putExtra("callFromConnect", "no")
                }
                context.sendBroadcast(mainIntent)
            }
            "callDeviceStartSample" -> {
                var mainIntent = Intent("UartService")
                mainIntent.putExtra("status", "callDeviceStartSample")
                context.sendBroadcast(mainIntent)
            }
            "ACTION_GATT_SERVICES_DISCOVERED" -> {
                var mainIntent = Intent("UartService")
                mainIntent.putExtra("status", "ACTION_GATT_SERVICES_DISCOVERED")
                context.sendBroadcast(mainIntent)
            }
            "EXTRA_DATA" -> {
                var mainIntent = Intent("UartService")
                mainIntent.putExtra("status", "getSampleRate")
                context.sendBroadcast(mainIntent)
            }
            "ACTION_DATA_AVAILABLE" -> {
                var mainIntent = Intent("UartService")
                var char = intent.getByteArrayExtra("txValue")
                mainIntent.putExtra("status", "ACTION_DATA_AVAILABLE")
                mainIntent.putExtra("EXTRA_DATA",char)
                context.sendBroadcast(mainIntent)
            }
            "DEVICE_DOES_NOT_SUPPORT_UART" -> {
                var mainIntent = Intent("UartService")
                mainIntent.putExtra("status", "DEVICE_DOES_NOT_SUPPORT_UART")
                context.sendBroadcast(mainIntent)
            }
            else -> {
                Log.d("MAINRECIVER","ERROR")
            }
        }

    }

    companion object {
        val ACTION = "Main"
    }

}
