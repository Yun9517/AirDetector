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

/**
 * Created by B00170 on 2018/4/11.
 */
class WarningClass {
    //20180122
    private val soundPool = SoundPool(5, AudioManager.STREAM_MUSIC, 100)
    //private MediaPlayer mp = null;
    //private MediaPlayer mp = null;
//    private val soundsMap=HashMap<Int, Int>()
//    //20180402 Icon的HashMap
//    private val iconMap= HashMap<Int, Int>()
//    //20180402 title的HashMap
//    private val titleMap= HashMap<Int, String>()
//    //20180402 vibrator的HashMap
//    private val vibratorMap= HashMap<Int, Long>()
//    //20180402 message的HashMap
//    private val messageMap= HashMap<Int, String>()
    private val REQUEST_CODE = 0xb01


    private var notificationManager: NotificationManager? = null
    private var m_context: Context? = null
    private var mVibrator: Vibrator? = null
    private var mPreference: SharedPreferences? = null

    constructor(MustInputContext: Context) {
        m_context = MustInputContext
        mPreference = m_context!!.getSharedPreferences(SavePreferences.SETTING_KEY, 0)
        mVibrator = m_context!!.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        notificationManager = m_context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //warningSetingArea()
    }

    //    private  fun warningSetingArea(){
//        //20180122   Andy_soundsMap
//        this.soundsMap!!.put(5, soundPool!!.load(m_context, R.raw.tvoc_over20000, 1))
//        this.soundsMap!!.put(4, soundPool!!.load(m_context, R.raw.tvoc_over5500, 1))
//        this.soundsMap!!.put(3, soundPool!!.load(m_context,R.raw.tvoc_over2200, 1))
//        this.soundsMap!!.put(2, soundPool!!.load(m_context, R.raw.tvoc_over660, 1))
//        this.soundsMap!!.put(1, soundPool!!.load(m_context, R.raw.tvoc_over220, 1))
//        //titleMap的選項Maps
//        this.titleMap!!.put(1,m_context!!.getString(R.string.warning_title_Yellow))
//        this.titleMap!!.put(2, m_context!!.getString(R.string.warning_title_Orange))
//        this.titleMap!!.put(3, m_context!!.getString(R.string.warning_title_Red))
//        this.titleMap!!.put(4, m_context!!.getString(R.string.warning_title_Purple))
//        this.titleMap!!.put(5, m_context!!.getString(R.string.warning_title_Brown))
//        //message的選項Maps
//        this.messageMap!!.put(1, m_context!!.getString(R.string.text_message_air_mid))
//        this.messageMap!!.put(2, m_context!!.getString(R.string.text_message_air_Medium_Orange))
//        this.messageMap!!.put(3, m_context!!.getString(R.string.text_message_air_bad))
//        this.messageMap!!.put(4, m_context!!.getString(R.string.text_message_air_Serious_Purple))
//        this.messageMap!!.put(5, m_context!!.getString(R.string.text_message_air_Extreme_Dark_Purple))
//        //icon的圖片選擇Maps
//        this.iconMap!!.put(1,R.drawable.history_face_icon_02)
//        this.iconMap!!.put(2,R.drawable.history_face_icon_03)
//        this.iconMap!!.put(3,R.drawable.history_face_icon_04)
//        this.iconMap!!.put(4,R.drawable.history_face_icon_05)
//        this.iconMap!!.put(5,R.drawable.history_face_icon_06)
//        //震動秒數的Maps
//        this.vibratorMap!!.put(1, 1000L)
//        this.vibratorMap!!.put(2, 2000L)
//        this.vibratorMap!!.put(3, 3000L)
//        this.vibratorMap!!.put(4, 4000L)
//        this.vibratorMap!!.put(5, 5000L)
//    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    fun judgeTvoc(TVOCString: Int) {
        //20180403
        var bebe5RString = TVOCString
        when (bebe5RString) {
        //Integer.valueOf(bebe5RString.get(2)
//            in 220..660 -> {
//                warning_function(1, 1, 1, 1, 1, bebe5RString)  //輕度汙染
//            }
            in 660..2200 -> {
                warning_function(soundPool!!.load(m_context, R.raw.tvoc_over20000, 1),
                        2000L,
                        BitmapFactory.decodeResource(m_context!!.resources, R.drawable.history_face_icon_03), m_context!!.getString(R.string.warning_title_Orange),
                        m_context!!.getString(R.string.text_message_air_Medium_Orange), bebe5RString.toString())  //中度汙染
            }
            in 2201..5500 -> {
                warning_function(soundPool!!.load(m_context, R.raw.tvoc_over5500, 1),
                        3000L,
                        BitmapFactory.decodeResource(m_context!!.resources, R.drawable.history_face_icon_04), m_context!!.getString(R.string.text_message_air_bad),
                        m_context!!.getString(R.string.text_message_air_bad), bebe5RString.toString())  //重度汙染
            }
            in 5501..20000 -> {
                warning_function(soundPool!!.load(m_context, R.raw.tvoc_over2200, 1),
                        4000L,
                        BitmapFactory.decodeResource(m_context!!.resources, R.drawable.history_face_icon_05), m_context!!.getString(R.string.text_message_air_Serious_Purple),
                        m_context!!.getString(R.string.text_message_air_Serious_Purple), bebe5RString.toString()) //嚴重汙染
            }
            in 20001..60000 -> {
                warning_function(soundPool!!.load(m_context, R.raw.tvoc_over660, 1),
                        5000L,
                        BitmapFactory.decodeResource(m_context!!.resources, R.drawable.history_face_icon_06), m_context!!.getString(R.string.text_message_air_Extreme_Dark_Purple),
                        m_context!!.getString(R.string.text_message_air_Extreme_Dark_Purple), bebe5RString.toString())  //非常嚴重汙染
            }
        }
    }

    //20180402   Andy
    @RequiresApi(api = Build.VERSION_CODES.O)
    fun warning_function(soundNo: Int,
                         vibratorSecond: Long,
                         iconSelect: Bitmap, titleSelect: String, messageSelect: String, BEBERString: String) {
        //20180124
        if (mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_SOUND, false)) {
            //mp.start();
            //20171220   Andy
            try {
                playSound(soundNo, 1.0f)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_VIBERATION, false) && mVibrator != null) {
            // 震动 1s
            mVibrator!!.vibrate(vibratorSecond)
        }
        if (mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_NOTIFY, false)) {
            if (isAppIsInBackground(m_context!!)) {
                try {
                    makeNotificationShow(iconSelect,
                            titleSelect,
                            messageSelect,
                            BEBERString)
                } catch (e: Exception) {
                    e.printStackTrace()
    }

            }
        }
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
    //播放音樂的撥放器
    fun playSound(sound: Int, fSpeed: Float) {
        val mgr = (m_context!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager?)!!
        val streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
        val streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        val volume = streamVolumeCurrent / streamVolumeMax
        //var id:Int=sound!!
        var success=  this.soundPool!!.play(sound, 1f, 1f, 1, 0, fSpeed)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun makeNotificationShow(iconID: Bitmap, title: String, text: String?, value: String) {
        val bigStyle = NotificationCompat.BigTextStyle()
        bigStyle.bigText(m_context!!.getString(R.string.text_message_air_Extreme_Dark_Purple))
        @SuppressLint("ResourceAsColor") val notification = NotificationCompat.Builder(m_context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(iconID)
                .setContentTitle(title)
                .setStyle(bigStyle)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setAutoCancel(true) // 點擊完notification自動消失
                .build()
        val intent = Intent(m_context!!, MainActivity::class.java)
        //當使用者點擊通知Bar時，切換回MainActivity
        val pi = PendingIntent.getActivity(m_context!!, REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        notification.contentIntent = pi
        //20180109   Andy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationHelper = NotificationHelper(m_context!!)
            notificationHelper!!.set_TCOC_Value(Integer.parseInt(value))//RString.get(2)));
            val NB = notificationHelper!!.getNotification1(title, text.toString())
            notificationHelper!!.notify(REQUEST_CODE, NB)
        } else {
            try {
                //送到手機的通知欄
                notificationManager!!.notify(1, notification)
                //20180209
                val pm = m_context!!.getSystemService(Context.POWER_SERVICE) as PowerManager
                //獲取電源管理器對象
                val wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_DIM_WAKE_LOCK, "bright")
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
}