package com.microjet.airqi2.BlueTooth.DFU

import android.app.Activity
import android.util.Log

import com.microjet.airqi2.AboutActivity
import no.nordicsemi.android.dfu.DfuBaseService

/**
 * Created by B00055 on 2018/3/26.
 */
class DFUService : DfuBaseService() {

       override fun getNotificationTarget(): Class<out Activity> {
          /*
            * As a target activity the NotificationActivity is returned, not the MainActivity. This is because the notification must create a new task:
            *
            * intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            *
            * when user press it. Using NotificationActivity we can check whether the new activity is a root activity (that means no other activity was open before)
            * or that there is other activity already open. In the later case the notificationActivity will just be closed. System will restore the previous activity.
            * However if the application has been closed during upload and user click the notification a NotificationActivity will be launched as a root activity.
            * It will create and start the main activity and terminate itself.
            *
            * This method may be used to restore the target activity in case the application was closed or is open. It may also be used to recreate an activity
            * history (see NotificationActivity).
            */
       //    return NotificationActivity::class.java
           return AboutActivity::class.java
    }

    override fun isDebug(): Boolean {
        // return BuildConfig.DEBUG;
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("DFU", "DFU service destroyed.")
    }
}