package com.microjet.airqi2.warringClass

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.SoundPool
import android.util.Log
import com.microjet.airqi2.Definition.SavePreferences

/**
 * Created by B00055 on 2018/5/21.
 */
class WarringSound(context: Context,Preference: SharedPreferences){
    val mContext=context
    val mPreference=Preference
    val soundPool = SoundPool(1, AudioManager.STREAM_MUSIC, 0)
    private var soundPoolOnLoadCompleteListener: SoundPool.OnLoadCompleteListener = SoundPool.OnLoadCompleteListener { soundPool, sampleId, status ->
        if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_SOUND, false)) {
            if (status == 0) {
                soundPool.play(sampleId, 1.0f, 1.0f, 0, 0, 1f)
            } else {
                Log.e("SoundPoolErroCode", status.toString())
            }
        }
    }
    init{
        soundPool.setOnLoadCompleteListener(soundPoolOnLoadCompleteListener)
    }
}