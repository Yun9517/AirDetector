package com.microjet.airqi2;

/**
 * Created by B00170 on 2018/1/9.
 */
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Context;
import android.content.ContextWrapper;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.content.res.AppCompatResources;

import javax.annotation.meta.When;

import static java.lang.System.in;

public class NotificationHelper extends ContextWrapper {
    private NotificationManager notifManager;
    public static final String CHANNEL_ONE_ID = "com.jessicathornsby.myapplication.ONE";
    public static final String CHANNEL_ONE_NAME = "Channel One";
    private Bitmap bitmap=null;
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
        //notificationChannel.setLightColor(Color.RED);
        notificationChannel.setShowBadge(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        getManager().createNotificationChannel(notificationChannel);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)

    public Notification.Builder getNotification1(String title, String body) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 1,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //創建一個BigPictureStyle物件，並設定要傳送的圖片
        return new Notification.Builder(getApplicationContext(), CHANNEL_ONE_ID)
                 .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setColorized(true)
                .setLargeIcon(bitmap)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(pi)
                .setStyle(new Notification.BigTextStyle().bigText(body));
    }
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
    public void set_TCOC_Value (Integer value) {
        Integer Value=value;
       if(Value >= 220 && Value < 660) {
           bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.history_face_icon_02);
       }else if (Value >= 660 && Value < 2200){
           bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.history_face_icon_03);
       }else if (Value >= 2200 && Value < 5500) {
           bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.history_face_icon_04);
       }else if (Value >= 5500 && Value < 20000) {
           bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.history_face_icon_05);
       }else {
           bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.history_face_icon_06);
       }
    }
}