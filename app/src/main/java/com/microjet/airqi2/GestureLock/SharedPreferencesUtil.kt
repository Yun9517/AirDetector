package com.microjet.airqi2.GestureLock

import android.content.SharedPreferences
import android.preference.PreferenceManager

import com.microjet.airqi2.MyApplication

class SharedPreferencesUtil private constructor() {

    private val editor: SharedPreferences.Editor
    private val prefer: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.applicationContext())

    init {
        this.editor = this.prefer.edit()
    }

    fun saveString(name: String, data: String) {
        this.editor.putString(name, data)
        this.editor.commit()
    }

    fun getString(name: String): String? {
        return this.prefer.getString(name, null)
    }

    companion object {
        private var instance: SharedPreferencesUtil? = null

        fun getInstance(): SharedPreferencesUtil? {
            if (instance == null) {
                synchronized(SharedPreferencesUtil::class.java) {
                    if (instance == null) {
                        instance = SharedPreferencesUtil()
                    }
                }
            }

            return instance
        }
    }
}
