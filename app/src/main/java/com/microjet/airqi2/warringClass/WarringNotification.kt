package com.microjet.airqi2.warringClass

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.PowerManager
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat.getColor
import android.util.Log
import com.microjet.airqi2.BroadReceiver.NotificationButtonReceiver
import com.microjet.airqi2.MainActivity
import com.microjet.airqi2.R

/**
 * Created by B00055 on 2018/5/21.
 */
class WarringNotification(context:Context,RequestCode:Int,initValue:Int,channelID:String,channelName:CharSequence){
    private val mContext=context
    private val notfiMangger = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val REQUEST_CODE = RequestCode
    private val REQUEST_TVOC_CODE=0x01
    private val REQUEST_PM25_CODE=0x02
    private val mChannelID=channelID
    private val mChannelName=channelName
    var warringValue=initValue
    private val iconSet=ArrayList<Int>()
    private var points=ArrayList<Int>()
    private var titleSet=ArrayList<Int>()
    init {
        iconSet.add(R.drawable.history_face_icon_02)//可移除一個，不過暫時先以andy原裝來try
        iconSet.add(R.drawable.history_face_icon_02)
        iconSet.add(R.drawable.history_face_icon_03)
        iconSet.add(R.drawable.history_face_icon_04)
        iconSet.add(R.drawable.history_face_icon_05)
        iconSet.add(R.drawable.history_face_icon_06)
    }
    fun setArrayPoint(input:ArrayList<Int>){
        points=input
    }
    fun setArrayTitle(input:ArrayList<Int>)
    {
        titleSet=input
    }

    fun showNotification(inputValue:Int){
        if (inputValue>=warringValue) {
            when (inputValue){
                in points [0]+1..points[1]->{ sendNotification(iconSet[0],titleSet[0],R.string.text_message_air_mid,inputValue)}
                in points [1]+1..points[2]->{ sendNotification(iconSet[1],titleSet[1],R.string.text_message_air_mid,inputValue)}
                in points [2]+1..points[3]->{ sendNotification(iconSet[2],titleSet[2],R.string.text_message_air_Medium_Orange,inputValue)}
                in points [3]+1..points[4]->{ sendNotification(iconSet[3],titleSet[3],R.string.text_message_air_bad,inputValue)}
                in points [4]+1..points[5]->{ sendNotification(iconSet[4],titleSet[4],R.string.text_message_air_Serious_Purple,inputValue)}
                in points [5]+1..points[6]->{ sendNotification(iconSet[5],titleSet[5],R.string.text_message_air_Extreme_Dark_Purple,inputValue)}
                else->{}
            }
        }
    }

    private fun sendNotification( icon: Int, title: Int, message: Int, value: Int) {
         try {
             var titleShowType = ""
             when (REQUEST_CODE) {
                 REQUEST_TVOC_CODE -> {
                     titleShowType = mContext.getString(title) + " " + mContext.getString(R.string.title_tvoc) + ":" + value + " ppb "
                 }
                 REQUEST_PM25_CODE -> {
                     titleShowType = mContext.getString(title) + " " + mContext.getString(R.string.title_pm25) + ":" + value + " μg/m³ "
                 }
             }
             makeNotificationShow( BitmapFactory.decodeResource(mContext.resources, icon), titleShowType, mContext.getString(message))
         } catch (e: Exception) {
             e.printStackTrace()
         }
    }

    @SuppressLint("NewApi")
    private fun makeNotificationShow(iconID: Bitmap, title: String, text: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        //This is the intent of PendingIntent
        val intent = Intent(mContext, MainActivity::class.java)
        val intentAction = Intent(mContext, NotificationButtonReceiver::class.java)
        val intentAction2 = Intent(mContext, NotificationButtonReceiver::class.java)
        //This is optional if you have more than one buttons and want to differentiate between two
        intentAction.action = "action1"
        intentAction2.action = "action2"
        //當使用者點擊通知Bar時，切換回MainActivity
        val pi0 = PendingIntent.getActivity(mContext, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val pi1 = PendingIntent.getBroadcast(mContext, REQUEST_CODE, intentAction,0 )//PendingIntent.FLAG_CANCEL_CURRENT
        val pi2 = PendingIntent.getBroadcast(mContext, REQUEST_CODE, intentAction2, 0)

        @SuppressLint("ResourceAsColor")
        val notification = NotificationCompat.Builder(mContext)
                .setChannelId(mChannelID)
                .setSmallIcon(R.mipmap.icon_leaf)
                .setColor(ContextCompat.getColor(mContext, R.color.iconColor))
                .setLargeIcon(iconID)
                .setContentTitle(title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(title))
                //.setPriority(Notification.PRIORITY_DEFAULT)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .addAction (0, mContext.getString(R.string.remindDismiss), pi1)
                .addAction (0, mContext.getString(R.string.remindAfter_5_Mins), pi2)
                .setAutoCancel(true) // 點擊完notification自動消失
                .build()
        notification.contentIntent = pi0
        try {
            //送到手機的通知欄
            notfiMangger.notify(REQUEST_CODE, notification)
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
    @RequiresApi(Build.VERSION_CODES.O)
   private fun createNotificationChannel(){
        val newNotBuilder =  NotificationChannel(mChannelID, mChannelName, NotificationManager.IMPORTANCE_HIGH)
        newNotBuilder.enableLights(true)
        newNotBuilder.setShowBadge(true)
        newNotBuilder.lockscreenVisibility=Notification.VISIBILITY_PUBLIC
        notfiMangger.createNotificationChannel(newNotBuilder)
    }
}