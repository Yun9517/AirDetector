package com.microjet.airqi2.CustomAPI

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarEntry
import com.microjet.airqi2.R
import kotlinx.android.synthetic.main.frg_chart.*
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringWriter



class FixBarChartK: BarChart {
//  private val DEFINE_FRAGMENT_TVOC = 1
//  private val DEFINE_FRAGMENT_ECO2 = 2
//  private val DEFINE_FRAGMENT_TEMPERATURE = 3
//  private val DEFINE_FRAGMENT_HUMIDITY = 4
//  private val DEFINE_FRAGMENT_PM25 = 5

//  private var define = 0
//  private var chartIntervalStep = 0
//  private var chartMin :Double= 0.0
//  private var chartMax :Double= 0.0
//  private var chartIntervalStart = 20
//  private var chartIntervalEnd = 20
//  private var chartLabelYCount = 6
//  private var chartIsShowMinTextView = false
//  private var chartLabelUnit = ""
//  private var chartLabel: String = ""

    internal var downPoint = PointF()

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(evt: MotionEvent): Boolean {
        when (evt.action) {
            MotionEvent.ACTION_DOWN -> {
                downPoint.x = evt.x
                downPoint.y = evt.y
            }
            MotionEvent.ACTION_MOVE -> {
                Log.i("Gesture getScrollX ", scrollX.toString() + "")
                if (scaleX > 1 && Math.abs(evt.x - downPoint.x) > 5) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
            }
        }
        return super.onTouchEvent(evt)
    }
/*
    fun configBar(input:Int) {
        val myJsonFile=GetJson()
        val jsonArray = JSONArray(myJsonFile)
        for (i in 0..(jsonArray.length() - 1)) {
            val item = jsonArray.getJSONObject(i)
            define = item.getInt("define")
            if (define == input)
            {
                chartLabel = item.getString("chartLabel")
                chartMin = item.getDouble("chartMin")
                chartMax = item.getDouble("chartMax")
                chartIntervalStep = item.getInt("chartIntervalStep")
                chartIntervalStart = item.getInt("chartIntervalStart")
                chartIntervalEnd = item.getInt("chartIntervalEnd")
                chartLabelYCount = item.getInt("chartLabelYCount")
                chartIsShowMinTextView = item.getBoolean("chartIsShowMinTextView")
                chartLabelUnit = item.getString("chartLabelUnit")
            }
        }
        configChartView()
    }
    private fun configChartView(){
        isScaleXEnabled = false
        isScaleYEnabled = false
        axisLeft.setLabelCount(chartLabelYCount, true)
        axisLeft.setAxisMaxValue(chartMax.toFloat())  // the axis maximum is 1500
        axisLeft.setAxisMinValue(chartMin.toFloat()) // start at zero
        axisLeft.setDrawLabels(false) // no axis labels
        axisLeft.setDrawAxisLine(false) // no axis line
        axisLeft.setDrawGridLines(true) // no grid lines
        axisLeft.gridColor = Color.WHITE
        xAxis.setDrawGridLines(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        val nums = ArrayList<Float>()
        for (i in chartIntervalStart..chartIntervalEnd step chartIntervalStep) {
            nums.add(i.toFloat())
        }
        legend.isEnabled = false
        yChartInterval = nums
        setDrawValueAboveBar(false)
        axisRight.isEnabled = false
        setDescription("")
    }
    private fun GetJson():String{

        val `is` = resources.openRawResource(R.raw.range_standard)
        val writer = StringWriter()
        val buffer = CharArray(1024)
        try {
            val reader = BufferedReader(InputStreamReader(`is`, "UTF-8"))
            var n:Int
            n = reader.read(buffer)
            while (n != -1) {
                writer.write(buffer, 0, n)
                n = reader.read(buffer)
            }
        } finally {
            `is`.close()
        }
            return writer.toString()
        //val jsonString = writer.toString()
    }
    fun getLabelRectLocation():ArrayList<RectF>{
        val lineRectFArray = ArrayList<RectF>()
        var j = 1
        for (i in chartMin.toInt()..chartMax.toInt() step chartIntervalStep) {//取得有標籤的數值位置，從最小值放至最大值
            lineRectFArray.add(getBarBounds(BarEntry(i.toFloat(), j)))
            j++
        }
        return lineRectFArray
    }
    */
}