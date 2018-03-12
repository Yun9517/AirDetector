package com.microjet.airqi2.CustomAPI

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast

object Utils {
    private var lastClickTime: Long = 0

    private var toast: Toast? = null

    // 防止瘋狂連點
    val isFastDoubleClick: Boolean
        get() {
            val time = System.currentTimeMillis()
            val timeD = time - lastClickTime
            if (0 < timeD && timeD < 1000) {
                return true
            }
            lastClickTime = time
            return false
        }

    // 氣泡訊息
    @SuppressLint("ShowToast")
    fun toastMakeTextAndShow(context: Context, text: String, duration: Int) {
        if (toast == null) {
            //如果還沒有用過makeText方法，才使用
            toast = android.widget.Toast.makeText(context, text, duration)
        } else {
            toast!!.setText(text)
            toast!!.duration = duration
        }
        toast!!.show()
    }
}
