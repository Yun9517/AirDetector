package com.microjet.airqi2.warringClass

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.util.Log
/**
 * Created by B00055 on 2018/5/21.
 */
class WarringSound(context:Context,inputResID: Int){
    private val m_context=context
    var warringValue=221
    private val ResID=inputResID
    private val soundPool = SoundPool(1, AudioManager.STREAM_MUSIC, 0)
    private var soundPoolOnLoadCompleteListener: SoundPool.OnLoadCompleteListener = SoundPool.OnLoadCompleteListener { soundPool, sampleId, status ->
        if (status == 0) {
            soundPool.play(sampleId, 1.0f, 1.0f, 0, 0, 1f)
        } else {
            Log.e(this.javaClass.simpleName, "status :$status")
        }
    }
    init{
        soundPool.setOnLoadCompleteListener(soundPoolOnLoadCompleteListener)
    }
    fun soundPlay(inpuValue:Int){
        if (inpuValue>=warringValue)
            soundPool.load(m_context, ResID, 1)
    }
}