package com.microjet.airqi2

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.PowerManager
import android.os.Vibrator
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.microjet.airqi2.Definition.SavePreferences

@Suppress("INTEGER_OVERFLOW")
/**
 * Created by B00170 on 2018/4/11.
 */
class WarningClass {
    //20180122
    private val REQUEST_TVOC_CODE = 0x01
    private val REQUEST_PM25_CODE = 0x02

    private var notificationManager: NotificationManager? = null
    private var notificationManager2: NotificationManager? = null
    private var m_context: Context? = null
    private var mVibrator: Vibrator? = null
    private var mPreference: SharedPreferences? = null

    //Test
    private var soundsMap: HashMap<Int, Int> =HashMap<Int,Int>()
    private var soundPool: SoundPool? =null
    constructor (MustInputContext: Context) {
        m_context = MustInputContext
        mPreference = m_context!!.getSharedPreferences(SavePreferences.SETTING_KEY, 0)
        mVibrator = m_context!!.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        notificationManager = m_context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager2 = m_context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //Test
        soundPool = SoundPool(1, AudioManager.STREAM_MUSIC, 0)
        soundPool!!.setOnLoadCompleteListener(soundPoolOnLoadCompleteListener)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun judgeValue(tvocValue: Int,pm25Value: Int) {
        //20180403
        //TVOC
        when (tvocValue) {
            in 660..2200 -> {
                warningFunction(REQUEST_TVOC_CODE,
                        R.raw.tvoc_over660,
                        2000L,
                        R.drawable.history_face_icon_03,
                        R.string.warning_title_Orange,//+tvocValue,
                        R.string.text_message_air_Medium_Orange,
                        tvocValue)  //中度汙染
            }
            in 2201..5500 -> {
                warningFunction(REQUEST_TVOC_CODE,
                        R.raw.tvoc_over2200,
                        3000L,
                        R.drawable.history_face_icon_04,
                        R.string.warning_title_Red,//+tvocValue,
                        R.string.text_message_air_bad,
                        tvocValue)  //重度汙染
            }
            in 5501..20000 -> {
                warningFunction(REQUEST_TVOC_CODE,
                        R.raw.tvoc_over5500,
                        4000L,
                        R.drawable.history_face_icon_05,
                        R.string.warning_title_Purple,//+tvocValue,
                        R.string.text_message_air_Serious_Purple,
                        tvocValue) //嚴重汙染
            }
            in 20001..60000 -> {
                warningFunction(REQUEST_TVOC_CODE,
                        R.raw.tvoc_over20000,
                        5000L,
                        R.drawable.history_face_icon_06,
                        R.string.warning_title_Brown,//+tvocValue,
                        R.string.text_message_air_Extreme_Dark_Purple,
                        tvocValue)  //非常嚴重汙染
            }
        }

        //20180409
        //PM2.5
        when (pm25Value) {
            in 0..15 -> {
//                warningFunction(REQUEST_PM25_CODE,
//                        R.raw.low_power,
//                        0L,
//                        R.drawable.history_face_icon_01,
//                        R.string.label_pm25_Green,
//                        R.string.message_pm25_Green,
//                        pm25Value)  //5
            }
            in 16..34 -> {
//                warningFunction(REQUEST_PM25_CODE,
//                        R.raw.low_power,
//                        1000L,
//                        R.drawable.history_face_icon_02,
//                        R.string.label_pm25_Yellow,//+pm25Value,
//                        R.string.message_pm25_Yellow,
//                        pm25Value)  //輕度汙染
            }
            in 35..54 -> {
                warningFunction(REQUEST_PM25_CODE,
                        R.raw.tvoc_over660,
                        2000L,
                        R.drawable.history_face_icon_03,
                        R.string.label_pm25_Orange,//+pm25Value,
                        R.string.message_pm25_Orange,
                        pm25Value)  //中度汙染
            }
            in 55..150 -> {
                warningFunction(REQUEST_PM25_CODE,
                        R.raw.tvoc_over2200,
                        3000L,
                        R.drawable.history_face_icon_04,
                        R.string.label_pm25_Red,//+pm25Value,
                        R.string.message_pm25_Red,
                        pm25Value)  //重度汙染
            }
            in 151..250 -> {
                warningFunction(REQUEST_PM25_CODE,
                        R.raw.tvoc_over5500,
                        4000L,
                        R.drawable.history_face_icon_05,
                        R.string.label_pm25_Purple,//+pm25Value,
                        R.string.message_pm25_Purple,
                        pm25Value) //嚴重汙染
            }
            else -> {
                warningFunction(REQUEST_PM25_CODE,
                        R.raw.tvoc_over20000,
                        5000L,
                        R.drawable.history_face_icon_06,
                        R.string.label_pm25_Brown,//+pm25Value,
                        R.string.message_pm25_Brown,
                        pm25Value)  //非常嚴重汙染
            }
        }
    }

    //20180402   Andy
    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun warningFunction(showDatetype:Int,soundResNo: Int,
                                vibratorSecond: Long,
                                iconSelect: Int, titleSelect: Int, messageSelect: Int, tvoc: Int) {
        soundPool!!.load(m_context, soundResNo, 1)
        sendVibrator(vibratorSecond)
        sendNotification(showDatetype,iconSelect,titleSelect,messageSelect,tvoc)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            val runningProcesses = am.runningAppProcesses
            for (processInfo in runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == context.packageName) {
                            isInBackground = false
                        }
                    }
                }
            }
        } else {
            val taskInfo = am.getRunningTasks(1)
            val componentInfo = taskInfo[0].topActivity
            if (componentInfo.packageName == context.packageName) {
                isInBackground = false
            }
        }
        return isInBackground
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun makeNotificationShow(dT:Int,iconID: Bitmap, title: String, text: String?, value: Int) {
        val bigStyle = NotificationCompat.BigTextStyle()
        bigStyle.bigText(text)//m_context!!.getString(R.string.text_message_air_Extreme_Dark_Purple))
        @SuppressLint("ResourceAsColor")
        val notification = NotificationCompat.Builder(m_context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(iconID)
                .setContentTitle(title)
                .setStyle(bigStyle)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setAutoCancel(true) // 點擊完notification自動消失
                .build()

        //20180109   Andy
        val intent = Intent(m_context!!, MainActivity::class.java)
        //當使用者點擊通知Bar時，切換回MainActivity
        val pi = PendingIntent.getActivity(m_context!!, dT,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        notification.contentIntent = pi


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationHelper = NotificationHelper(m_context!!)
            notificationHelper!!.set_TCOC_Value(value)
            val NB = notificationHelper!!.getNotification1(title, text.toString())
            notificationHelper!!.notify(dT, NB)
        } else {
            try {
                //送到手機的通知欄
                notificationManager!!.notify(dT, notification)
                //20180209
                val powerManager = m_context!!.getSystemService(Context.POWER_SERVICE) as PowerManager
                //獲取電源管理器對象
                val wl = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_DIM_WAKE_LOCK, "bright")
                //獲取PowerManager.WakeLock對象,後面的參數|表示同時傳入兩個值,最後的是LogCat裡用的Tag
                wl.acquire(2 * 1000L)
                //點亮屏幕
                wl.release()
                Log.e("休眠狀態下", "喚醒螢幕")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendVibrator(vicSec: Long) {
        if (mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_VIBERATION, false) && mVibrator != null) {
            mVibrator!!.vibrate(vicSec)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(dataType: Int,icon: Int, title: Int, message: Int, tvoc: Int) {
        if (mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_NOTIFY, false)) {
            if (isAppIsInBackground(m_context!!)) {
                try {
                    var titletype = ""
                    when (dataType) {
                        REQUEST_TVOC_CODE -> {
                            titletype = m_context!!.getString(R.string.title_tvoc)
                        }
                        REQUEST_PM25_CODE -> {
                            titletype = m_context!!.getString(R.string.title_pm25)
                        }
                    }
                    makeNotificationShow(
                            dataType,
                            BitmapFactory.decodeResource(m_context!!.resources, icon),
                            titletype + ":" + m_context!!.getString(title),
                            m_context!!.getString(message),
                            tvoc)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    private var soundPoolOnLoadCompleteListener: SoundPool.OnLoadCompleteListener = SoundPool.OnLoadCompleteListener { soundPool, sampleId, status ->
        if (mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_SOUND, false)) {
            if(status == 0) {
                soundPool.play(sampleId, 1.0f, 1.0f, 0, 0, 1f)
            }
            else{
                Log.e("SoundPoolErroCode",status.toString())
            }
        }
    }
}