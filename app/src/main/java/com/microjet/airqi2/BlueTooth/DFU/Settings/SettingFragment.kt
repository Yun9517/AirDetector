package com.microjet.airqi2.BlueTooth.DFU.Settings

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import com.microjet.airqi2.R
import no.nordicsemi.android.dfu.DfuSettingsConstants

/**
 * Created by B00055 on 2018/3/26.
 */

class SettingFragment : PreferenceFragment(), DfuSettingsConstants, SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.settings_dfu)

        // set initial values
        updateNumberOfPacketsSummary()
        updateMBRSize()
    }

    override fun onResume() {
        super.onResume()

        // attach the preference change listener. It will update the summary below interval preference
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()

        // unregister listener
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val preferences = preferenceManager.sharedPreferences

        if (DfuSettingsConstants.SETTINGS_PACKET_RECEIPT_NOTIFICATION_ENABLED == key) {
            val disabled = !preferences.getBoolean(DfuSettingsConstants.SETTINGS_PACKET_RECEIPT_NOTIFICATION_ENABLED, true)
            if (disabled && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                AlertDialog.Builder(activity).setMessage(R.string.dfu_settings_dfu_number_of_packets_info).setTitle(R.string.dfu_settings_dfu_information)
                        .setPositiveButton(R.string.ok, null).show()
            }
        } else if (DfuSettingsConstants.SETTINGS_NUMBER_OF_PACKETS == key) {
            updateNumberOfPacketsSummary()
        } else if (DfuSettingsConstants.SETTINGS_MBR_SIZE == key) {
            updateMBRSize()
        } else if (DfuSettingsConstants.SETTINGS_ASSUME_DFU_NODE == key && sharedPreferences.getBoolean(key, false)) {
            AlertDialog.Builder(activity).setMessage(R.string.dfu_settings_dfu_assume_dfu_mode_info).setTitle(R.string.dfu_settings_dfu_information)
                    .setPositiveButton(R.string.ok, null)
                    .show()
        }
    }

    private fun updateNumberOfPacketsSummary() {
        val screen = preferenceScreen
        val preferences = preferenceManager.sharedPreferences

        var value = preferences.getString(DfuSettingsConstants.SETTINGS_NUMBER_OF_PACKETS, DfuSettingsConstants.SETTINGS_NUMBER_OF_PACKETS_DEFAULT.toString())
        // Security check
        if (TextUtils.isEmpty(value)) {
            value = DfuSettingsConstants.SETTINGS_NUMBER_OF_PACKETS_DEFAULT.toString()
            preferences.edit().putString(DfuSettingsConstants.SETTINGS_NUMBER_OF_PACKETS, value).apply()
        }
        screen.findPreference(DfuSettingsConstants.SETTINGS_NUMBER_OF_PACKETS).summary = value

        val valueInt = Integer.parseInt(value)
        if (valueInt > 200 && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            AlertDialog.Builder(activity).setMessage(R.string.dfu_settings_dfu_number_of_packets_info).setTitle(R.string.dfu_settings_dfu_information)
                    .setPositiveButton(R.string.ok, null)
                    .show()
        }
    }

    private fun updateMBRSize() {
        val screen = preferenceScreen
        val preferences = preferenceManager.sharedPreferences

        val value = preferences.getString(DfuSettingsConstants.SETTINGS_MBR_SIZE, DfuSettingsConstants.SETTINGS_DEFAULT_MBR_SIZE.toString())
        screen.findPreference(DfuSettingsConstants.SETTINGS_MBR_SIZE).summary = value
    }

    companion object {
        val SETTINGS_KEEP_BOND = "settings_keep_bond"
    }
}
