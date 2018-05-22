package com.microjet.airqi2.warringClass

import android.content.Context
import android.os.Vibrator

/**
 * Created by B00055 on 2018/5/21.
 */
class WarringVibrator(context: Context,initValue:Int){
    private val vibrator= context.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    var warringValue=initValue
    private val vicSet=ArrayList<Long>()
    private var points=ArrayList<Int>()
    init{
        vicSet.add(500)
        vicSet.add(1000)
        vicSet.add(2000)
        vicSet.add(3000)
        vicSet.add(4000)
        vicSet.add(5000)
    }
//    private var callback: OnVibratorThreadHoldValueChangedListener? = null
/*
    fun setOnVibratorThreadHoldValueChangedListener(listener:OnVibratorThreadHoldValueChangedListener) {
        callback = listener
    }*/

    fun sendVibrator(vicSec: Int) {
        if (vicSec>warringValue) {
            when (vicSec) {
                in points[0]+1..points[1]-> {
                    vibrator.vibrate(vicSet[0])
                }
                in points[1]+1..points[2]-> {
                    vibrator.vibrate(vicSet[1])
                }
                in points[2]+1..points[3]-> {
                    vibrator.vibrate(vicSet[2])
                }
                in points[3]+1..points[4]-> {
                    vibrator.vibrate(vicSet[3])
                }
                in points[4]+1..points[5]-> {
                    vibrator.vibrate(vicSet[4])
                }
                in points[5]+1..points[6]-> {
                    vibrator.vibrate(vicSet[5])
                }
                else ->{//over points[6]
                    vibrator.vibrate(vicSet[5])
                }
            }
        }
    }
    fun setArrayPoint(input:ArrayList<Int>){
        points=input
    }
/*
     interface OnVibratorThreadHoldValueChangedListener{
        fun setThead(threadHoldValue:Int)
    }*/
}
