package com.microjet.airqi2.warringClass

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.PowerManager
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.microjet.airqi2.BroadReceiver.NotificationButtonReceiver
import com.microjet.airqi2.Definition.SavePreferences
import com.microjet.airqi2.MainActivity
import com.microjet.airqi2.NotificationHelper
import com.microjet.airqi2.R

/**
 * Created by B00055 on 2018/5/21.
 */
class WarringNotification(context:Context,RequestCode:Int){
    private var notificationManager: NotificationManager? = null
    private val REQUEST_CODE = RequestCode
    val mContext=context
    val IconSet=ArrayList<Int>()
    init {
        IconSet.add(R.drawable.history_face_icon_02)//可移除一個，不過暫時先以andy原裝來try
        IconSet.add(R.drawable.history_face_icon_02)
        IconSet.add(R.drawable.history_face_icon_03)
        IconSet.add(R.drawable.history_face_icon_04)
        IconSet.add(R.drawable.history_face_icon_05)
        IconSet.add(R.drawable.history_face_icon_06)
    }
    fun showNotification(inputValue:Int){

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(DateTypeId: Int, icon: Int, title: Int, message: Int, value: Int) {
         try {
             var titleShowType = ""
             when (DateTypeId) {
                 REQUEST_TVOC_CODE -> {
                     titleShowType = mContext.getString(title) + " " + mContext.getString(R.string.title_tvoc) + ":" + value + " ppb "
                 }
                 REQUEST_PM25_CODE -> {
                     titleShowType = mContext.getString(title) + " " + mContext.getString(R.string.title_pm25) + ":" + value + " μg/m³ "
                 }
                 REQUEST_BOTH->{

                 }
             }
            // mPreferenceNotification = m_context!!.getSharedPreferences("NotificationAction", Context.MODE_PRIVATE)
           // mPreferenceNotification!!.edit().clear().apply()

             makeNotificationShow(
                     DateTypeId,
                     BitmapFactory.decodeResource(mContext.resources, icon),
                     titleShowType,
                     mContext.getString(message),
                     value)
         } catch (e: Exception) {
             e.printStackTrace()
         }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun makeNotificationShow(DateType: Int, iconID: Bitmap, title: String, text: String?, dataValue: Int) {
        val bigStyle = NotificationCompat.BigTextStyle()
        bigStyle.bigText(text)//m_context!!.getString(R.string.text_message_air_Extreme_Dark_Purple))
        //20180109   Andy
        val intent = Intent(mContext, MainActivity::class.java)
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        val stackBuilder = TaskStackBuilder.create(mContext)
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity::class.java)
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent)
        //val pi = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)


        //This is the intent of PendingIntent
        val intentAction = Intent(mContext, NotificationButtonReceiver::class.java)
        val intentAction2 = Intent(mContext, NotificationButtonReceiver::class.java)
        //This is optional if you have more than one buttons and want to differentiate between two
        intentAction.action = "action1"
        intentAction2.action = "action2"
        //當使用者點擊通知Bar時，切換回MainActivity
        val pi0 = PendingIntent.getActivity(mContext, DateType, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val pi1 = PendingIntent.getBroadcast(mContext, DateType, intentAction,0 )//PendingIntent.FLAG_CANCEL_CURRENT
        val pi2 = PendingIntent.getBroadcast(mContext, DateType, intentAction2, 0)

        @SuppressLint("ResourceAsColor")
        val notification = NotificationCompat.Builder(mContext)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(iconID)
                .setContentTitle(title)
                //.setStyle(bigStyle)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(title))
                //.setPriority(Notification.PRIORITY_DEFAULT)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .addAction (0, mContext.getString(R.string.remindDismiss), pi1)
                .addAction (0, mContext.getString(R.string.remindAfter_5_Mins), pi2)
                .setAutoCancel(true) // 點擊完notification自動消失
                .build()
        notification.contentIntent = pi0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationHelper = NotificationHelper(mContext)
            notificationHelper.set_TCOC_Value(dataValue)
            val action1 = Notification.Action(0, mContext.getString(R.string.remindDismiss), pi1)
            val action2 = Notification.Action(0, mContext.getString(R.string.remindAfter_5_Mins), pi2)
            val NB = notificationHelper.getNotification1(title, text.toString())
                    .addAction(action1)
                    .addAction(action2)
                    .setAutoCancel(true)
            notificationHelper.notify(DateType, NB)
        } else {
            try {
                //送到手機的通知欄
                notificationManager!!.notify(DateType, notification)
                //20180209
                val powerManager = mContext.getSystemService(Context.POWER_SERVICE) as PowerManager
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
}