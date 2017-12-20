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
        return if (getEntryForXIndex(index).`val` < 220)
        // less than 95 green
            mColors[0]
        else if (getEntryForXIndex(index).`val` < 660)
        // less than 100 orange
            mColors[1]
        else
        // greater or equal than 100 red
            mColors[2]
    }

}
