package com.microjet.airqi2.CustomAPI

import android.graphics.Color
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

/**
 * Created by ray650128 on 2017/11/28.
 */

class MyBarDataSet(yVals: List<BarEntry>, label: String) : BarDataSet(yVals, label) {
    override fun getEntryIndex(e: BarEntry?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getColor(index: Int): Int {
        when (label) {
            "TVOC" -> {
                return when (getEntryForXIndex(index).`val`) {
                    in 0..220 -> mColors[0]         //G
                    in 221..660 -> mColors[1]       //Y
                    in 661..2200 -> mColors[2]      //O
                    in 2201..5500 -> mColors[3]     //R
                    in 5501..20000 -> mColors[4]    //P
                //20180227未偵測值
                    in 65537..65538 -> Color.TRANSPARENT
                    else -> mColors[5]              //B
                }
            }
            "ECO2" -> {
                return when (getEntryForXIndex(index).`val`) {
                    in 0..700 -> mColors[0]         //G
                    in 701..1000 -> mColors[1]       //Y
                    in 1001..1500 -> mColors[2]      //O
                    in 1501..2500 -> mColors[3]     //R
                    in 2501..5000 -> mColors[4]     //P
                    in 65537..65538 -> Color.TRANSPARENT
                    else -> mColors[5]              //B
                }
            }
            "Temp" -> {
                return when (getEntryForXIndex(index).`val`) {
                    in 28..34 -> mColors[1]         //Green
                    in 35..200 -> mColors[2]        //Red
                    in 65537..65538 -> Color.TRANSPARENT
                    else -> mColors[0]              //Blue
                }
            }
            "Humi" -> {
                return when (getEntryForXIndex(index).`val`) {
                    in 0..44 -> mColors[0]         //Blue
                    in 45..65 -> mColors[1]       //Green
                    in 65537..65538 -> Color.TRANSPARENT
                    else -> mColors[2]             //Red
                }
            }
            "PM2.5" -> {
                return when (getEntryForXIndex(index).`val`) {
                    in 0..15 -> mColors[0]         //G
                    in 16..34 -> mColors[1]       //Y
                    in 35..54 -> mColors[2]      //O
                    in 55..150 -> mColors[3]     //R
                    in 151..250 -> mColors[4]    //P
                //20180227未偵測值
                    in 65537..65538 -> Color.TRANSPARENT
                    else -> mColors[5]              //B
                }
            }
            "PM10" -> {
                return when (getEntryForXIndex(index).`val`) {
                    in 0..15 -> mColors[0]         //G
                    in 16..34 -> mColors[1]       //Y
                    in 35..54 -> mColors[2]      //O
                    in 55..150 -> mColors[3]     //R
                    in 151..250 -> mColors[4]    //P
                    in 65537..65538 -> Color.TRANSPARENT
                    else -> mColors[5]              //B
                }
            }
            else -> {
                return 0
            }
        }
    }
}
