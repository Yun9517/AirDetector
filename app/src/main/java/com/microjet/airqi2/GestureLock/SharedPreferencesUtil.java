package com.microjet.airqi2.GestureLock;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.microjet.airqi2.MyApplication;

public class SharedPreferencesUtil {
    private static SharedPreferencesUtil instance;

    private SharedPreferences.Editor editor;
    private SharedPreferences prefer;

    public SharedPreferencesUtil() {
        this.prefer = PreferenceManager.getDefaultSharedPreferences(MyApplication.Companion.applicationContext());
        this.editor = this.prefer.edit();
    }

    public static SharedPreferencesUtil getInstance() {
        if (instance == null) {
            synchronized (SharedPreferencesUtil.class) {
                if (instance == null) {
                    instance = new SharedPreferencesUtil();
                }
            }
        }

        return instance;
    }

    public void saveString(String name, String data) {
        this.editor.putString(name, data);
        this.editor.commit();
    }

    public String getString(String name) {
        return this.prefer.getString(name, null);
    }
}
