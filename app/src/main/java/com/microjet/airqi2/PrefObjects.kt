package com.microjet.airqi2

import android.content.Context
import com.microjet.airqi2.Definition.SavePreferences

/**
 * Created by ray650128 on 2018/6/1.
 *
 */
class PrefObjects(context: Context) {

    private val share = context.getSharedPreferences(SavePreferences.SETTING_KEY, Context.MODE_PRIVATE)

    // ****** 2018/04/17 Identify the App is first time initial or not ************//
    fun setSharePreferenceNotShowTour(value: Boolean) {
        share.edit().putBoolean(SavePreferences.SETTING_IS_FIRST_USED, value).apply()
    }

    fun getSharePreferenceNotShowTour(): Boolean {
        return share.getBoolean(SavePreferences.SETTING_IS_FIRST_USED, false)
    }

    // 通用
    fun getSharePreferenceMAC(): String {
        return share.getString(SavePreferences.SETTING_SAVED_BT_ADDR, "noValue")
    }

    fun setSharePreferenceMAC(value: String) {
        share.edit().putString(SavePreferences.SETTING_SAVED_BT_ADDR, value).apply()
    }

    fun getSharePreferenceName(): String {
        return share.getString(SavePreferences.SETTING_SAVED_BT_NAME, "Unknown")
    }

    fun setSharePreferenceName(value: String) {
        share.edit().putString(SavePreferences.SETTING_SAVED_BT_NAME, value).apply()
    }

    fun getSharePreferenceManualDisconn(): Boolean {
        return share.getBoolean(SavePreferences.SETTING_MANUAL_DISCONNECT, false)
    }

    fun setSharePreferenceManualDisconn(value: Boolean) {
        share.edit().putBoolean(SavePreferences.SETTING_MANUAL_DISCONNECT, value).apply()
    }

    // 個人軌跡
    fun getSharePreferenceMapPanelStat(): Boolean {
        return share.getBoolean(SavePreferences.SETTING_MAP_PANEL_STATUS, true)
    }

    fun setSharePreferenceMapPanelStat(value: Boolean) {
        share.edit().putBoolean(SavePreferences.SETTING_MAP_PANEL_STATUS, value).apply()
    }

    // 設定
    fun getSharePreferenceAllowNotify(): Boolean {
        return share.getBoolean(SavePreferences.SETTING_ALLOW_NOTIFY, false)
    }

    fun setSharePreferenceAllowNotify(value: Boolean) {
        share.edit().putBoolean(SavePreferences.SETTING_ALLOW_NOTIFY, value).apply()
    }

    fun getSharePreferenceAllowNotifyMessage(): Boolean {
        return share.getBoolean(SavePreferences.SETTING_ALLOW_MESSAGE, false)
    }

    fun setSharePreferenceAllowNotifyMessage(value: Boolean) {
        share.edit().putBoolean(SavePreferences.SETTING_ALLOW_MESSAGE, value).apply()
    }

    fun getSharePreferenceAllowNotifyVibrate(): Boolean {
        return share.getBoolean(SavePreferences.SETTING_ALLOW_VIBRATION, false)
    }

    fun setSharePreferenceAllowNotifyVibrate(value: Boolean) {
        share.edit().putBoolean(SavePreferences.SETTING_ALLOW_VIBRATION, value).apply()
    }

    fun getSharePreferenceAllowNotifySound(): Boolean {
        return share.getBoolean(SavePreferences.SETTING_ALLOW_SOUND, false)
    }

    fun setSharePreferenceAllowNotifySound(value: Boolean) {
        share.edit().putBoolean(SavePreferences.SETTING_ALLOW_SOUND, value).apply()
    }

    fun getSharePreferenceAllowNotifyTvocValue(): Int {
        return share.getInt(SavePreferences.SETTING_TVOC_NOTIFY_VALUE, 660)
    }

    fun setSharePreferenceAllowNotifyTvocValue(value: Int) {
        share.edit().putInt(SavePreferences.SETTING_TVOC_NOTIFY_VALUE, value).apply()
    }

    fun getSharePreferenceAllowNotifyPM25Value(): Int {
        return share.getInt(SavePreferences.SETTING_PM25_NOTIFY_VALUE, 25)
    }

    fun setSharePreferenceAllowNotifyPM25Value(value: Int) {
        share.edit().putInt(SavePreferences.SETTING_PM25_NOTIFY_VALUE, value).apply()
    }

    fun getSharePreferenceAllowNotifyLowBattery(): Boolean {
        return share.getBoolean(SavePreferences.SETTING_BATTERY_SOUND, false)
    }

    fun setSharePreferenceAllowNotifyLowBattery(value: Boolean) {
        share.edit().putBoolean(SavePreferences.SETTING_BATTERY_SOUND, value).apply()
    }

    fun getSharePreferenceCloudUploadStat(): Boolean {
        return share.getBoolean(SavePreferences.SETTING_CLOUD_FUN, false)
    }

    fun setSharePreferenceCloudUploadStat(value: Boolean) {
        share.edit().putBoolean(SavePreferences.SETTING_CLOUD_FUN, value).apply()
    }

    fun getSharePreferenceCloudUpload3GStat(): Boolean {
        return share.getBoolean(SavePreferences.SETTING_CLOUD_ALLOW_3G, false)
    }

    fun setSharePreferenceCloudUpload3GStat(value: Boolean) {
        share.edit().putBoolean(SavePreferences.SETTING_CLOUD_ALLOW_3G, value).apply()
    }

    fun getSharePreferenceLedOn(): Boolean {
        return share.getBoolean(SavePreferences.SETTING_LED_SWITCH, true)
    }

    fun setSharePreferenceLedOn(value: Boolean) {
        share.edit().putBoolean(SavePreferences.SETTING_LED_SWITCH, value).apply()
    }

    fun getSharePreferenceDisconnectLedOn(): Boolean {
        return share.getBoolean(SavePreferences.SETTING_LED_SWITCH_OFFLINE, true)
    }

    fun setSharePreferenceDisconnectLedOn(value: Boolean) {
        share.edit().putBoolean(SavePreferences.SETTING_LED_SWITCH_OFFLINE, value).apply()
    }

    fun getSharePreferenceFirebase(): Boolean {
        return share.getBoolean(SavePreferences.SETTING_FIREBASE, false)
    }

    fun setSharePreferenceFirebase(value: Boolean) {
        share.edit().putBoolean(SavePreferences.SETTING_FIREBASE, value).apply()
    }

    fun getSharePreferencePrivacy(): Boolean {
        return share.getBoolean(SavePreferences.SETTING_MAP_PRIVACY, false)
    }

    fun setSharePreferencePrivacy(value: Boolean) {
        share.edit().putBoolean(SavePreferences.SETTING_MAP_PRIVACY, value).apply()
    }


    fun getSharePreferenceServiceForeground(): Boolean {
        return share.getBoolean(SavePreferences.SETTING_IS_FOREGROUND, false)
    }

    fun setSharePreferenceServiceForeground(value: Boolean) {
        share.edit().putBoolean(SavePreferences.SETTING_IS_FOREGROUND, value).apply()
    }

    fun getSharePreferenceSaveImageCount(): Int {
        return share.getInt(SavePreferences.SETTING_PHOTO_COUNT, 0)
    }

    fun setSharePreferenceSaveImageCount(value: Int) {
        share.edit().putInt(SavePreferences.SETTING_PHOTO_COUNT, value).apply()
    }

    fun getSharePreferenceAllowBroadcastMessage(): Boolean {
        return share.getBoolean(SavePreferences.SETTING_ALLOW_BROADCAST_MESSAGE, false)
    }

    fun getSharePreferenceAllowBroadcastVibrate(): Boolean {
        return share.getBoolean(SavePreferences.SETTING_ALLOW_BROADCAST_VIBRATION, false)
    }

    fun setSharePreferenceAllowBroadcastVibrate(value: Boolean) {
        share.edit().putBoolean(SavePreferences.SETTING_ALLOW_BROADCAST_VIBRATION, value).apply()
    }

    fun getSharePreferenceAllowBroadcastSound(): Boolean {
        return share.getBoolean(SavePreferences.SETTING_ALLOW_BROADCAST_SOUND, false)
    }

    fun setSharePreferenceAllowBroadcastSound(value: Boolean) {
        share.edit().putBoolean(SavePreferences.SETTING_ALLOW_BROADCAST_SOUND, value).apply()
    }

    fun getSharePreferenceTempUnitFahrenheit(): Boolean {
        return share.getBoolean(SavePreferences.SETTING_IS_FAHRENHEIT, false)
    }

    fun setSharePreferenceTempUnitFahrenheit(value: Boolean) {
        share.edit().putBoolean(SavePreferences.SETTING_IS_FAHRENHEIT, value).apply()
    }

    fun getSharePreferencePullAllData(): Boolean {
        return share.getBoolean(SavePreferences.FLAG_PULL_ALL_DATA, false)
    }

    fun setSharePreferencePullAllData(value: Boolean) {
        share.edit().putBoolean(SavePreferences.FLAG_PULL_ALL_DATA, value).apply()
    }

    fun getSharePreferencePullAllDataMAC(): String {
        return share.getString(SavePreferences.FLAG_PULL_ALL_DATA_MAC, "no")
    }

    fun setSharePreferencePullAllDataMAC(value: String?) {
        share.edit().putString(SavePreferences.FLAG_PULL_ALL_DATA_MAC, value).apply()
    }

    fun getSharePreferenceCheckFWVersion(): Boolean {
        return share.getBoolean(SavePreferences.SETTING_IS_NEWFW_ARRIVAL, false)
    }

    fun setSharePreferenceCheckFWVersion(value: Boolean) {
        share.edit().putBoolean(SavePreferences.SETTING_IS_NEWFW_ARRIVAL, value).apply()
    }

}