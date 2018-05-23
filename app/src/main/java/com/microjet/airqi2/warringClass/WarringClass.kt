package com.microjet.airqi2.warringClass

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.microjet.airqi2.Definition.SavePreferences
import com.microjet.airqi2.R

/**
 * Created by B00055 on 2018/5/21.
 */

class WarringClass (context:Context)
{
    private val REQUEST_TVOC_CODE=0x01
    private val REQUEST_PM25_CODE=0x02
    private var allowNotify = false
    private var allowVibrator = false
    private var allowSound = false
    private var allowNotification=false
    private var tvocAlertValue = 660
    private var pm25AlertValue = 16
    private val tvocChannel="tvocChannel"
    private val pm25Channel="pm25Channel"
    private val tvocChannelName="TVOC"
    private val pm25ChannelName="PM25"
    private val mContext=context
    private val mPreference= mContext.getSharedPreferences(SavePreferences.SETTING_KEY, 0)
    private var tvocSound=WarringSound(context, R.raw.tvoc_over)
    private var pm25Sound=WarringSound(context, R.raw.pm25_over)
    private var tvocNotification=WarringNotification(context,REQUEST_TVOC_CODE,tvocAlertValue,tvocChannel,tvocChannelName)
    private var pm25Notification=WarringNotification(context,REQUEST_PM25_CODE,pm25AlertValue,pm25Channel,pm25ChannelName)
    private var tvocVibrator=WarringVibrator(context,mPreference.getInt(SavePreferences.SETTING_TVOC_NOTIFY_VALUE, 660))
    private var pm25Vibrator=WarringVibrator(context,mPreference.getInt(SavePreferences.SETTING_PM25_NOTIFY_VALUE, 16))

    private val tvocArrayAlertValue=ArrayList<Int>()
    private val tvocArrayStringList=ArrayList<Int>()
    private val pm25ArrayAlertValue=ArrayList<Int>()
    private val pm25ArrayStringList=ArrayList<Int>()
    var callback:onChangeListener?=null
    init {
        tvocArrayAlertValue.add(0)
        tvocArrayAlertValue.add(219)
        tvocArrayAlertValue.add(659)
        tvocArrayAlertValue.add(2199)
        tvocArrayAlertValue.add(5499)
        tvocArrayAlertValue.add(19999)
        tvocArrayAlertValue.add(60000)
        tvocArrayStringList.add(R.string.warning_title_Yellow)
        tvocArrayStringList.add(R.string.warning_title_Yellow)
        tvocArrayStringList.add(R.string.warning_title_Orange)
        tvocArrayStringList.add(R.string.warning_title_Red)
        tvocArrayStringList.add(R.string.warning_title_Purple)
        tvocArrayStringList.add(R.string.warning_title_Brown)
        tvocVibrator.setArrayPoint(tvocArrayAlertValue)
        tvocNotification.setArrayPoint(tvocArrayAlertValue)
        tvocNotification.setArrayTitle(tvocArrayStringList)

        pm25ArrayAlertValue.add(0)
        pm25ArrayAlertValue.add(14)
        pm25ArrayAlertValue.add(34)
        pm25ArrayAlertValue.add(54)
        pm25ArrayAlertValue.add(149)
        pm25ArrayAlertValue.add(249)
        pm25ArrayAlertValue.add(500)
        pm25ArrayStringList.add(R.string.label_pm25_Green)
        pm25ArrayStringList.add(R.string.label_pm25_Yellow)
        pm25ArrayStringList.add(R.string.label_pm25_Orange)
        pm25ArrayStringList.add(R.string.label_pm25_Red)
        pm25ArrayStringList.add(R.string.label_pm25_Purple)
        pm25ArrayStringList.add(R.string.label_pm25_Brown)
        pm25Vibrator.setArrayPoint(pm25ArrayAlertValue)
        pm25Notification.setArrayPoint(pm25ArrayStringList)

    }
    fun judgeValue(tvocValue: Int, pm25Value: Int){
        allowNotify = mPreference.getBoolean(SavePreferences.SETTING_ALLOW_NOTIFY, false)
        allowVibrator = mPreference.getBoolean(SavePreferences.SETTING_ALLOW_VIBERATION,false)
        allowSound = mPreference.getBoolean(SavePreferences.SETTING_ALLOW_SOUND,false)
        allowNotification = mPreference.getBoolean(SavePreferences.SETTING_ALLOW_MESSAGE, false)
        tvocAlertValue = mPreference.getInt(SavePreferences.SETTING_TVOC_NOTIFY_VALUE, 660)
        pm25AlertValue = mPreference.getInt(SavePreferences.SETTING_PM25_NOTIFY_VALUE, 16)

        tvocVibrator.warringValue = tvocAlertValue
        pm25Vibrator.warringValue = pm25AlertValue
        tvocSound.warringValue = tvocAlertValue
        pm25Sound.warringValue = pm25AlertValue
        when (allowNotify){
            true->{
                checkVibrator(tvocValue,pm25Value)
                checkSound(tvocValue,pm25Value)
                if (isAppIsInBackground(mContext)) {
                    checkNotification(tvocValue,pm25Value)
                }
            }
            false->{
                Log.v(this.javaClass.simpleName,"allowNotify:$allowNotify")
            }
        }
    }
    private fun checkVibrator(tvocValue: Int, pm25Value: Int){
        when (allowVibrator){
            true->{
                tvocVibrator.sendVibrator(tvocValue)
                pm25Vibrator.sendVibrator(pm25Value)
            }
            false->{
                Log.v(this.javaClass.simpleName,"allowVibrator:$allowVibrator")
            }
        }
    }
    private fun checkSound(tvocValue: Int, pm25Value: Int){
        when (allowSound){
            true->{
                pm25Sound.soundPlay(pm25Value)
                tvocSound.soundPlay(tvocValue)
            }
            false->{
                Log.v(this.javaClass.simpleName,"allowSound:$allowSound")
            }
        }
    }
    private fun checkNotification(tvocValue:Int,pm25Value:Int){
        when (allowNotification){
            true ->{
                tvocNotification.showNotification(tvocValue)
                pm25Notification.showNotification(pm25Value)
            }
            false ->{
                Log.v(this.javaClass.simpleName,"allowNotification:$allowNotification")
            }
        }
    }
    @SuppressLint("ObsoleteSdkInt")
    private fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            val runningProcesses = am.runningAppProcesses
            for (processInfo in runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == context.packageName) {
                            isInBackground = false
                        }
                    }
                }
            }
        } else {
            val taskInfo = am.getRunningTasks(1)
            val componentInfo = taskInfo[0].topActivity
            if (componentInfo.packageName == context.packageName) {
                isInBackground = false
            }
        }
        return isInBackground
    }
    interface onChangeListener{
        fun allowNotifyChange()
        fun allowVibratorChange()
        fun allowSoundChange()
        fun allowNotification()

    }
}