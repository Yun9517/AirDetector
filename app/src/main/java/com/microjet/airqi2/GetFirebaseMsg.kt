package com.microjet.airqi2

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
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
            sendnotfication(getMessage!!.notification!!.body!!)
        }
    }
    private fun sendnotfication(body: String){
        val intent = Intent()
        intent.setClass(this,MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pend_intent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT)
        val Not_sound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("")//等Firebase設定Title
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(Not_sound)//由Firebase設定鈴聲
                .setContentIntent(pend_intent)

        val notMangger = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notMangger.notify(0,notBuilder.build())
    }
}