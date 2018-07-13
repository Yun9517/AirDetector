package com.microjet.airqi2.warringClass

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.widget.RemoteViews
import com.microjet.airqi2.BlueTooth.UartService
import com.microjet.airqi2.MainActivity
import com.microjet.airqi2.R
import io.realm.internal.SyncObjectServerFacade.getApplicationContext

/**
 * Created by ray650128 on 2018/6/13.
 *
 */
class MainNotification(context: Context) {
    private val NOTIF_ID = "com.microjet.airqi2.notify"
    private val NOTIF_NAME = "com.microjet.airqi2.mainNotify"
    private val NOTIF_DESC = "Main Notification for foreground service"

    val mContext = context

    @SuppressLint("NewApi")
    fun makeNotificion(): Notification {

        // 建立觸碰通知範圍時的PendingIntent
        val actionIntent = Intent(getApplicationContext(), MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel(NOTIF_ID, NOTIF_NAME, NotificationManager.IMPORTANCE_LOW)
            } else {
                null
            }

            notificationChannel!!.description = NOTIF_DESC

            notificationChannel.enableLights(false)
            notificationChannel.enableVibration(false)
            notificationChannel.setSound(null, null)

            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(mContext, NOTIF_ID)
        } else {
            NotificationCompat.Builder(mContext)
        }

        val contentView = RemoteViews(mContext.packageName, R.layout.main_notification_layout)
        contentView.setImageViewResource(R.id.image, R.mipmap.icon_leaf)
        contentView.setTextViewText(R.id.contentTitle, "ADDWII")
        contentView.setTextViewText(R.id.contentText, mContext.resources.getString(R.string.text_service_live_in_foreground))

        //contentView.setOnClickPendingIntent(R.id.btnClose, createPendingIntent("STOP_FOREGROUND"))
        contentView.setOnClickPendingIntent(R.id.btnClose, createPendingIntent("MANUAL_DISCONNECT"))

        notificationBuilder.setOngoing(true)

        //notificationBuilder.setContentTitle("ADDWII")
        //notificationBuilder.setContentText(mContext.resources.getString(R.string.text_service_live_in_foreground))
        notificationBuilder.setSmallIcon(R.mipmap.icon_leaf).color = mContext.getColor(R.color.iconColor)
        notificationBuilder.setCustomContentView(contentView)
        notificationBuilder.setContentIntent(mainPendingIntent)

        return notificationBuilder.build()
    }

    private fun createPendingIntent(action: String): PendingIntent {
        // 為了儲存對應Action的Service的Intent，建立PendingIntent
        val service = Intent(mContext, UartService::class.java)
        service.action = action

        return PendingIntent.getService(mContext, 0, service, 0)
    }
}