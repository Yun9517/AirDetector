package com.microjet.airqi2.BroadReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.Definition.BroadcastIntents


/**
 * Created by B00175 on 2017/11/9.
 */

class PrimaryReceiver : BroadcastReceiver() {
    //private var nm: NotificationManager? = null

    private val NOTIFY_ID = 1
    private val TAG="Broadcast:"

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getStringExtra("status")) {
            "ACTION_GATT_DISCONNECTED",
            "ACTION_GATT_DISCONNECTING"
            -> {
                val bundle = Bundle()
                broadcastUpdate(context, BroadcastActions.ACTION_GATT_DISCONNECTED, bundle)
             //   var mainIntent = Intent("mainActivity")
             //   mainIntent.putExtra("status", "ACTION_GATT_DISCONNECTED")
             //   context.sendBroadcast(mainIntent)
            }
            "ACTION_GATT_CONNECTED",
            "ACTION_GATT_CONNECTING"
            -> {
                val bundle = Bundle()
                bundle.putString(BroadcastActions.INTENT_KEY_DEVICE_ADDR, intent.getStringExtra("macAddress"))
                broadcastUpdate(context,BroadcastActions.ACTION_GATT_CONNECTED, bundle)
            //    var mainIntent = Intent("mainActivity")
            //    mainIntent.putExtra("status", "ACTION_GATT_CONNECTED")
                // ***** 2017/12/11 Drawer連線 會秀出 Mac Address ************************ //
            //    mainIntent.putExtra("macAddress", intent.getStringExtra("macAddress"))
            //    context.sendBroadcast(mainIntent)
            }
            "disconnect" -> {
                var mainIntent = Intent(BroadcastIntents.UART_SERVICE)
                mainIntent.putExtra("status", "disconnect")
                context.sendBroadcast(mainIntent)
            }
            "connect" -> {
                //get Address
                var macAddress = intent.getStringExtra("mac")

                var intent = Intent(BroadcastIntents.UART_SERVICE)
                intent.putExtra("status", "connect")
                val bundle = Bundle()
                bundle.putString("mac",macAddress.toString())
                intent!!.putExtras(bundle)
                //intent.putExtra("mac",macAddress)
                context.sendBroadcast(intent)
                Log.d("MAINRECEIVER","CONNECT: $macAddress")
            }
            "close" -> {
                var mainIntent = Intent(BroadcastIntents.UART_SERVICE)
                mainIntent.putExtra("status", "close")
                context.sendBroadcast(mainIntent)
            }
            "B6"->{
//                var mainIntent = Intent(BroadcastIntents.MAIN_ACTIVITY)
//                mainIntent.putExtra("status","B6")
//
//                mainIntent.putExtra("TEMPValue",intent.getStringExtra("TEMPValue"))
//                mainIntent.putExtra("HUMIValue",intent.getStringExtra("HUMIValue"))
//                mainIntent.putExtra("TVOCValue",intent.getStringExtra("TVOCValue"))
//                mainIntent.putExtra("eCO2Value",intent.getStringExtra("eCO2Value"))
//
//                mainIntent.putExtra("BatteryLife",intent.getStringExtra("BatteryLife"))
//                mainIntent.putExtra("flag",intent.getStringExtra("flag"))
//                context.sendBroadcast(mainIntent)

                val bundle = Bundle()
//                bundle.putString(BroadcastActions.INTENT_KEY_TEMP_VALUE, intent.getStringExtra("TEMPValue"))
//                bundle.putString(BroadcastActions.INTENT_KEY_HUMI_VALUE, intent.getStringExtra("HUMIValue"))
//                bundle.putString(BroadcastActions.INTENT_KEY_TVOC_VALUE, intent.getStringExtra("TVOCValue"))
//                bundle.putString(BroadcastActions.INTENT_KEY_CO2_VALUE, intent.getStringExtra("eCO2Value"))
//                bundle.putLong(BroadcastActions.INTENT_KEY_CREATED_TIME, intent.getLongExtra(BroadcastActions.INTENT_KEY_CREATED_TIME,0))
//                //   Bundle[{status=MAXPROGRESSITEM, MAXPROGRESSITEM=1440}]
                broadcastUpdate(context,BroadcastActions.ACTION_SAVE_INSTANT_DATA, bundle)

            }
            "B0"->{
                val bundle = Bundle()
                bundle.putString(BroadcastActions.INTENT_KEY_TEMP_VALUE, intent.getStringExtra("TEMPValue"))
                bundle.putString(BroadcastActions.INTENT_KEY_HUMI_VALUE, intent.getStringExtra("HUMIValue"))
                bundle.putString(BroadcastActions.INTENT_KEY_TVOC_VALUE, intent.getStringExtra("TVOCValue"))
                bundle.putString(BroadcastActions.INTENT_KEY_CO2_VALUE,intent.getStringExtra("eCO2Value"))
                //bundle.putString(BroadcastActions.INTENT_KEY_PM25_VALUE, data.get(4));
                bundle.putString(BroadcastActions.INTENT_KEY_BATTERY_LIFE, intent.getStringExtra("BatteryLife"))
                bundle.putString(BroadcastActions.INTENT_KEY_PREHEAT_COUNT, intent.getStringExtra("PreheatCountDown"))
                broadcastUpdate(context,BroadcastActions.ACTION_GET_NEW_DATA, bundle)
                /*
                var mainIntent = Intent("mainActivity")
                mainIntent.putExtra("status","B0")
                mainIntent.putExtra("TEMPValue",intent.getStringExtra("TEMPValue"))
                mainIntent.putExtra("HUMIValue",intent.getStringExtra("HUMIValue"))
                mainIntent.putExtra("TVOCValue",intent.getStringExtra("TVOCValue"))
                mainIntent.putExtra("eCO2Value",intent.getStringExtra("eCO2Value"))
                mainIntent.putExtra("BatteryLife",intent.getStringExtra("BatteryLife"))
                mainIntent.putExtra("PreheatCountDown",intent.getStringExtra("PreheatCountDown"))
                context.sendBroadcast(mainIntent)
                */
            }
            "B5"->{
                var mainIntent = Intent(BroadcastIntents.MAIN_ACTIVITY)
                mainIntent.putExtra("status","B5")
                mainIntent.putExtras(intent)
                context.sendBroadcast(mainIntent)
               // var bundle= mainIntent.putExtra("TVOCValue",intent.getBundleExtra("result"))
              //  var mydata=bundle.getParcelableArrayExtra("resultSet")
              //  mainIntent.putExtra("TVOCValue",intent.getStringExtra("TVOCValue"))
              //  mainIntent.putExtra("BatteryLife",intent.getStringExtra("BatteryLife"))
            }
            "message" -> {
                var mainIntent = Intent(BroadcastIntents.UART_SERVICE)
                mainIntent.putExtra("status", "message")
                context.sendBroadcast(mainIntent)
                Log.d("message","messageMAIN")
            }
            "callItems" -> {
                var mainIntent = Intent(BroadcastIntents.UART_SERVICE)
                mainIntent.putExtra("status", "callItems")
                context.sendBroadcast(mainIntent)
            }
            "checkItems" -> {
                var mainIntent = Intent(BroadcastIntents.UART_SERVICE)
                mainIntent.putExtra("status", "checkItems")
                context.sendBroadcast(mainIntent)
            }
            "setSampleRate" -> {
                var SampleTime= intent.getIntExtra("SampleTime",2)
                var mainIntent = Intent(BroadcastIntents.UART_SERVICE)
                mainIntent.putExtra("status", "setSampleRate")
                mainIntent.putExtra("SampleTime",SampleTime)
                context.sendBroadcast(mainIntent)
            }
            "getSampleRate" -> {
                var mainIntent = Intent(BroadcastIntents.UART_SERVICE)
                mainIntent.putExtra("status", "getSampleRate")
                when( intent.getStringExtra("callFromConnect"))
                {
                    "yes"-> mainIntent.putExtra("callFromConnect", "yes")
                    else -> mainIntent.putExtra("callFromConnect", "no")
                }
                context.sendBroadcast(mainIntent)
            }
            "callDeviceStartSample" -> {
                var mainIntent = Intent(BroadcastIntents.UART_SERVICE)
                mainIntent.putExtra("status", "callDeviceStartSample")
                context.sendBroadcast(mainIntent)
            }
            "ACTION_GATT_SERVICES_DISCOVERED" -> {
                var mainIntent = Intent(BroadcastIntents.UART_SERVICE)
                mainIntent.putExtra("status", "ACTION_GATT_SERVICES_DISCOVERED")
                context.sendBroadcast(mainIntent)
            }
            "EXTRA_DATA" -> {
                var mainIntent = Intent(BroadcastIntents.UART_SERVICE)
                mainIntent.putExtra("status", "getSampleRate")
                context.sendBroadcast(mainIntent)
            }
            "ACTION_DATA_AVAILABLE" -> {
                var mainIntent = Intent(BroadcastIntents.UART_SERVICE)
                var char = intent.getByteArrayExtra("txValue")
                mainIntent.putExtra("status", "ACTION_DATA_AVAILABLE")
                mainIntent.putExtra("EXTRA_DATA",char)
                context.sendBroadcast(mainIntent)
            }
            "DEVICE_DOES_NOT_SUPPORT_UART" -> {
                var mainIntent = Intent(BroadcastIntents.UART_SERVICE)
                mainIntent.putExtra("status", "DEVICE_DOES_NOT_SUPPORT_UART")
                context.sendBroadcast(mainIntent)
            }
            "NOWPROGRESSITEM"->{
                val bundle = Bundle()
                bundle.putString(BroadcastActions.INTENT_KEY_LOADING_DATA, intent.getStringExtra("NOWPROGRESSITEM"))
                broadcastUpdate(context,BroadcastActions.ACTION_LOADING_DATA, bundle)
            }
            "MAXPROGRESSITEM"->{
                val bundle = Bundle()
                //   Bundle[{status=MAXPROGRESSITEM, MAXPROGRESSITEM=1440}]
                bundle.putString(BroadcastActions.INTENT_KEY_GET_HISTORY_COUNT, intent.getStringExtra("MAXPROGRESSITEM"))
                broadcastUpdate(context,BroadcastActions.ACTION_GET_HISTORY_COUNT, bundle)
            }/*
            "NOWPROGRESSITEM"->{
            var mainIntent = Intent("mainActivity")
            mainIntent.putExtra("status", "NOWPROGRESSITEM")
            mainIntent.putExtra("NOWPROGRESSITEM",intent.getIntExtra("NOWPROGRESSITEM",0))
            context.sendBroadcast(mainIntent)
            }
            "MAXPROGRESSITEM"->{
                var mainIntent = Intent("mainActivity")
                mainIntent.putExtra("status", "MAXPROGRESSITEM")
                mainIntent.putExtra("MAXPROGRESSITEM",intent.getIntExtra("MAXPROGRESSITEM",0))
                context.sendBroadcast(mainIntent)
            }*/
            else -> {
                Log.d("MAINRECIVER","ERROR")
            }
        }
    }
    private fun broadcastUpdate(context:Context,action: String, bundle: Bundle) {
        val intent = Intent(action)
        intent.putExtras(bundle)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        Log.v(TAG, "Send broadcast: " + action)
    }
    companion object {
        val ACTION = "Main"
    }

}
