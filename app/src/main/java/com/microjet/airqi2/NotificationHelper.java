package com.microjet.airqi2;

/**
 * Created by B00170 on 2018/1/9.
 */

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.Toast;

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

        //showTwo();

//        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
//        bigStyle.bigText(getString(R.string.text_message_air_bad));
//        // 需要注意的是，作为選項，此處可以设置MainActivity的啟動模式為singleTop，避免APP從開與重新產生onCreate()
        Intent intent = new Intent(this, MainActivity.class);
//        //當使用者點擊通知Bar時，切換回MainActivity
        PendingIntent pi = PendingIntent.getActivity(this, 1,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

//        /* Add Big View Specific Configuration */
//        NotificationCompat.InboxStyle inboxStyle =
//                new NotificationCompat.InboxStyle();
//
//        String[] events = new String[6];
//        events[0] = new String("This is first line....");
//        events[1] = new String("This is second line...");
//        events[2] = new String("This is third line...");
//        events[3] = new String("This is 4th line...");
//        events[4] = new String("This is 5th line...");
//        events[5] = new String("This is 6th line...");
//
//        // Sets a title for the Inbox style big view
//        inboxStyle.setBigContentTitle("Big Title Details:");
//        // Moves events into the big view
//        for (int i=0; i < events.length; i++) {
//
//            inboxStyle.addLine(events[i]);
//        }
        Bitmap bitmap = BitmapFactory.decodeResource(getResources() , R.mipmap.ic_launcher);
        //取得要發送的圖片

        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.bigPicture(bitmap);
        //創建一個BigPictureStyle物件，並設定要傳送的圖片
        return new Notification.Builder(getApplicationContext(), CHANNEL_ONE_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.app_android_icon_light))
                .setColor(Color.RED)
                .setColorized(true)
                .setLargeIcon(bitmap)
                //.setBadgeIconType( R.drawable.app_android_icon_logo)
                //.setTicker("通知首次出现在通知栏，带上升动画效果的")
                .setPriority(Notification.PRIORITY_DEFAULT)
                //.setBadgeIconType(R.drawable.background_chart) //your app icon
                .setContentIntent(pi)
                .setStyle(new Notification.BigTextStyle().bigText(body));
    }
    protected NotificationCompat.Builder builder;
    protected NotificationManager manager;
    public void showTwo() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Toast.makeText(this, "ur device is not support media style, use stand instead", Toast.LENGTH_SHORT).show();
        }
        //builder.setTicker("MessagingNotification");
        builder.setContentTitle("MessagingNotification");
        builder.setContentText("MessagingNotificationMessagingNotificationMessagingNotification");

        builder.setStyle(new NotificationCompat.MessagingStyle("DisplayName2")
                .addMessage("addMessageA", (int)System.currentTimeMillis(), "sender1")
                .addMessage("addMessageB", (int)System.currentTimeMillis(), "sender2")
                .addMessage("addMessageC", (int)System.currentTimeMillis(), "sender3")
                .addMessage("addMessageD", (int)System.currentTimeMillis(), "sender4")
                .setConversationTitle("ConversationTitle"));

        manager.notify((int)System.currentTimeMillis(), getNotification());
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

    protected Notification getNotification() {
        return builder.build();
    }
}