package com.microjet.airqi2.CustomAPI

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
        return when(getEntryForXIndex(index).`val`) {
            in 0..220 -> mColors[0]         //G
            in 221..660 -> mColors[1]       //Y
            in 661..2200 -> mColors[2]      //O
            in 2201..5500 -> mColors[3]     //R
            else -> mColors[4]              //P
        }
    }

}
