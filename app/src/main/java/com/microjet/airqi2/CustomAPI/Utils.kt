package com.microjet.airqi2.CustomAPI

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.SubscriptSpan
import android.widget.Toast
import com.microjet.airqi2.PrefObjects


object Utils {
    private var lastClickTime: Long = 0

    private var toast: Toast? = null

    // 防止瘋狂連點
    val isFastDoubleClick: Boolean
        get() {
            val time = System.currentTimeMillis()
            val timeD = time - lastClickTime
            if (timeD in 1..999) {
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

    fun checkCheckSum(input: ByteArray): Boolean {
        var checkSum = 0x00
        val max = 0xFF.toByte()
        for (i in 0 until input.size) {
            checkSum += input[i]
        }
        val checkSumByte = checkSum.toByte()
        return checkSumByte == max

    }

    fun setTextSubscript(inputText: String, index: Int): SpannableString {
        val msp = SpannableString(inputText)
        msp.setSpan(RelativeSizeSpan(0.5f), index, inputText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)  //0.5f表示默認字體大小的一半
        msp.setSpan(SubscriptSpan(), index, inputText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)     //下標
        return msp
    }

    fun setTextSubscript(inputText: String): SpannableString {
        val msp = SpannableString(inputText)
        msp.setSpan(RelativeSizeSpan(0.75f), inputText.indexOf(" ") + 1, inputText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)  //0.5f表示默認字體大小的一半
        return msp
    }

    fun convertTemperature(context: Context, celsiusVal: Float): String {
        val myPref = PrefObjects(context)
        val isFahrenheit = myPref.getSharePreferenceTempUnitFahrenheit()

        return if(isFahrenheit) {
            val fahrenheitVal = ((celsiusVal + 40) * 1.8) - 40
            String.format("%.1f", fahrenheitVal) + " ℉"
        } else {
            String.format("%.1f", celsiusVal) + " ℃"
        }
    }

    fun convertTemperatureNoUnit(context: Context, celsiusVal: Int): Int {
        val myPref = PrefObjects(context)
        val isFahrenheit = myPref.getSharePreferenceTempUnitFahrenheit()

        return if(isFahrenheit) {
            (((celsiusVal.toFloat() + 40) * 1.8) - 40).toInt()
        } else {
            celsiusVal
        }
    }

}
