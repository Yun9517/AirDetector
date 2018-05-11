package com.microjet.airqi2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Created by B00190 on 2018/5/2.
 */
class GetFirebaseMsg : FirebaseMessagingService(){
    private val TAG = "MyFirebaseMessaging"
    override fun onMessageReceived(getMessage: RemoteMessage?) {
        super.onMessageReceived(getMessage)
        if(getMessage!!.data!!.size > 0){
            Log.d(TAG,"Message data"+getMessage.data!!)
        }
        if(getMessage!!.notification != null){
            Log.d(TAG,"Medssage body"+getMessage!!.notification!!.body)
            sendnotfication(getMessage!!.notification!!.body!!,getMessage!!.notification!!.title!!)
        }
    }
    private fun sendnotfication(body: String, title: String){
        val notfiMangger = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId: String = "給程式辨認，使用者看不到"
        val channelName: String = "ADDWII"
        var notiFication_ID: Int = 8

        val intent = Intent()
        intent.setClass(this,MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pend_intent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT)
        val Not_sound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val newNotBuilder =  NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notfiMangger.createNotificationChannel(newNotBuilder)
        }

        val notBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)//等Firebase設定Title
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(Not_sound)//由Firebase設定鈴聲
                .setContentIntent(pend_intent)
                .setChannelId(channelId)

        notfiMangger.notify(1,notBuilder.build())

    }


}