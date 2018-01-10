package com.microjet.airqi2;

/**
 * Created by B00170 on 2018/1/9.
 */

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.content.Context;
import android.content.ContextWrapper;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.microjet.airqi2.BlueTooth.UartService;
import com.microjet.airqi2.R;

public class NotificationHelper extends ContextWrapper {
    private NotificationManager notifManager;
    public static final String CHANNEL_ONE_ID = "com.jessicathornsby.myapplication.ONE";
    public static final String CHANNEL_ONE_NAME = "Channel One";
    public static final String CHANNEL_TWO_ID = "com.jessicathornsby.myapplication.TWO";
    public static final String CHANNEL_TWO_NAME = "Channel Two";

//Create your notification channels//

    public NotificationHelper(Context base) {
        super(base);
        createChannels();
    }

    @SuppressLint("NewApi")
    public void createChannels() {

        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                CHANNEL_ONE_NAME, notifManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.setShowBadge(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        getManager().createNotificationChannel(notificationChannel);

        NotificationChannel notificationChannel2 = new NotificationChannel(CHANNEL_TWO_ID,
                CHANNEL_TWO_NAME, notifManager.IMPORTANCE_DEFAULT);
        notificationChannel2.enableLights(false);
        notificationChannel2.enableVibration(true);
        notificationChannel2.setLightColor(Color.RED);
        notificationChannel2.setShowBadge(false);
        getManager().createNotificationChannel(notificationChannel2);

    }

//Create the notification that’ll be posted to Channel One//

    @RequiresApi(api = Build.VERSION_CODES.O)

    public Notification.Builder getNotification1(String title, String body) {
//        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
//        bigStyle.bigText(getString(R.string.text_message_air_bad));
//        // 需要注意的是，作为選項，此處可以设置MainActivity的啟動模式為singleTop，避免APP從開與重新產生onCreate()
        Intent intent = new Intent(this, MainActivity.class);
//        //當使用者點擊通知Bar時，切換回MainActivity
        PendingIntent pi = PendingIntent.getActivity(this, 1,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);


        return new Notification.Builder(getApplicationContext(), CHANNEL_ONE_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.color.progressBarMidColor)
                .setAutoCancel(true)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.app_android_icon_light))
                .setColor(Color.BLUE)
                //.setBadgeIconType( R.drawable.app_android_icon_logo)
                //.setSubText(bigStyle)
                //.setTicker("通知首次出现在通知栏，带上升动画效果的")
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(pi); // 點擊完notification自動消失
    }

////Create the notification that’ll be posted to Channel Two//
//
//    public Notification.Builder getNotification2(String title, String body) {
//        return new Notification.Builder(getApplicationContext(), CHANNEL_TWO_ID)
//                .setContentTitle(title)
//                .setContentText(body)
//                .setSmallIcon(R.color.progressBarMidColor)
//                .setAutoCancel(true);
//    }


    public void notify(int id, Notification.Builder notification) {

        getManager().notify(id, notification.build());
    }

//Send your notifications to the NotificationManager system service//

    private NotificationManager getManager() {
        if (notifManager == null) {
            notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        }
        return notifManager;
    }
}