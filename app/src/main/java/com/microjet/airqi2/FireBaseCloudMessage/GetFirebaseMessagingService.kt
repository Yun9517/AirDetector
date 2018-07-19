package com.microjet.airqi2.FireBaseCloudMessage

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
import com.microjet.airqi2.*
import com.microjet.airqi2.Definition.NotificationObj
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

/**
 * Created by B00190 on 2018/5/2.
 */
class GetFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "MyFirebaseMessaging"

    // 2018/07/04
    private var swMessageVal: Boolean = false
    private var swVibrateVal: Boolean = false
    private var swSoundVal: Boolean = false
    private lateinit var myPref: PrefObjects

    override fun onMessageReceived(getMessage: RemoteMessage?) {
        super.onMessageReceived(getMessage)

        myPref = PrefObjects(this)

        if (getMessage?.data!!.size > 0) {
            Log.d(TAG, "Message data" + getMessage.data)

        }

        if (getMessage?.data != null) {
            val firebaseScorllingText: String = getMessage?.data?.get("updateArticle").toString()
            Log.e(TAG, "Message Topic= " + firebaseScorllingText)
            firebaseScrollingTopic(firebaseScorllingText)
        } else if (getMessage?.notification != null) {
            Log.d(TAG, "Medssage body" + getMessage?.notification?.body)
            sendNotification(getMessage?.notification?.body, getMessage?.notification?.title)
        }
    }

    private fun sendNotification(body: String?, title: String?) {
        val notiManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId: String = "給程式辨認，使用者看不到"
        val channelName: String = "ADDWII"
        var notiFication_ID: Int = 8
        val GROUP_KEY_NEWS = "notification_NewsGronp"

        swMessageVal = myPref.getSharePreferenceAllowBroadcastMessage()
        swVibrateVal = myPref.getSharePreferenceAllowBroadcastVibrate()
        swSoundVal = myPref.getSharePreferenceAllowBroadcastSound()

        val Not_sound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val newNotBuilder = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notiManager.createNotificationChannel(newNotBuilder)
        }

        val intent = Intent()
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        when (body) {
            "Addwii最新資訊" -> {
                intent.setClass(this, OpenBrowserActivity::class.java)
                intent.putExtra("fromNotification", true)
            }
            else -> intent.setClass(this, MainActivity::class.java)
        }
        val pend_intent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val notBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.icon_leaf)
                .setColor(getColor(R.color.iconColor))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                //.setSound(Not_sound)//由Firebase設定鈴聲
                .setContentIntent(pend_intent)
                .setChannelId(channelId)
                .setSound(Not_sound)
        // 2018/07/05 Add Judgement for Sound & Vibrate
        if (swSoundVal) {
            notBuilder.setSound(Not_sound)
        }
        if (swVibrateVal) {
            notBuilder.setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
        }

        val notfiID = System.currentTimeMillis().toInt()
        Log.e("notfiID", notfiID.toString())

        when (body) {
            "Addwii最新資訊" -> {
                notBuilder.setGroup(GROUP_KEY_NEWS).setGroupSummary(true)//將相同訊息包在一起
                notiManager.notify(notfiID, notBuilder.build())
            }
            else -> notiManager.notify(NotificationObj.CLOUD_NOTIFICATION_ID, notBuilder.build())
        }

    }

    private fun firebaseScrollingTopic(firebaseScorllingText: String) {
        TvocNoseData.scrollingList.clear()
        val jsonObj = JSONObject(firebaseScorllingText)
        //取出posts內容
        val resultArray = jsonObj.getJSONArray("posts")
        for (i in 0 until resultArray.length()) {
            val jsonObjScrolling = resultArray.getJSONObject(i)
            val hashMap = HashMap<String, String>()
            hashMap["title"] = jsonObjScrolling["title"].toString()
            hashMap["url"] = jsonObjScrolling["url"].toString()
            TvocNoseData.scrollingList.add(hashMap)
            Log.e(TAG, "TvocNoseData.scrollingList=  " + TvocNoseData.scrollingList.toString())
        }
        val urlEvent = BleEvent("new Topic get")
        EventBus.getDefault().post(urlEvent)
        sendNotification("Addwii最新資訊", TvocNoseData.scrollingList[0]["title"])
    }
}