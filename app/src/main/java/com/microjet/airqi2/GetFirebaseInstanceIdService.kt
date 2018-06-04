package com.microjet.airqi2

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Created by B00190 on 2018/6/1.
 */
class GetFirebaseInstanceIdService: FirebaseInstanceIdService() {
    override fun onTokenRefresh() {
        super.onTokenRefresh()
        val refreshedToken = FirebaseInstanceId.getInstance().token
        FirebaseMessaging.getInstance().isAutoInitEnabled

    }
}