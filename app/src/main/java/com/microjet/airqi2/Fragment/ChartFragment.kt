package com.microjet.airqi2.Fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.microjet.airqi2.AsmDataModel
import com.microjet.airqi2.CustomAPI.CSVWriter
import com.microjet.airqi2.CustomAPI.MyBarDataSet
import com.microjet.airqi2.CustomAPI.Utils
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.Definition.BroadcastIntents
import com.microjet.airqi2.R
import com.microjet.airqi2.TvocNoseData
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.frg_chart.*
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringWriter
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by B00055 on 2018/2/9.
 *
 */

class ChartFragment : Fragment() {
    private val DEFINE_FRAGMENT_TVOC = 1
    private val DEFINE_FRAGMENT_PM25 = 2
    private val DEFINE_FRAGMENT_ECO2 = 3
    private val DEFINE_FRAGMENT_TEMPERATURE = 4
    private val DEFINE_FRAGMENT_HUMIDITY = 5
    
    private var mContext: Context? = null

    private var mDataCount: Int = 60

    private var mConnectStatus: Boolean = false

    //試Realm拉資料
    private var arrTime = ArrayList<String>()
    private var arrData = ArrayList<String>()
    //20180122

    private var animationCount = 0
    private var downloadingData = false

    private var preHeat = "0"
    private var getDataCycle = 15

    private val calObject = Calendar.getInstance()
    private var spinnerPositon = 0
    private var datepickerHandler = Handler()
    //private var chartHandler = Handler()
    private var downloadComplete = false


    var counter = 0
    var valueIntAVG = 0
    var valueFloatAVG = 0.0
    //Andy
    //private val arrayAvgData = ArrayList<String>()
    var useFor = 0

    private var chartIntervalStep = 0
    private var chartMin = 0f
    private var chartMax = 0f
    private var chartIntervalStart = 20
    private var chartIntervalEnd = 20
    private var chartLabelYCount = 6
    private var chartIsShowMinTextView = false
    private var chartLabelUnit = ""
    private var labelTextViewArray = ArrayList<TextView>()
    private var labelArray = ArrayList<String>()
    private var intArray: IntArray? = null
    private var chartLabel: String = ""


    private var errorTime = 0

    private fun setImageBarPosition() {
        chart_line.data = getBarData()
        chart_line.yChartInterval.size
        var j = 1
        val lineRectFArray = ArrayList<RectF>()
        for (i in chartMin.toInt()..chartMax.toInt() step chartIntervalStep) {//取得有標籤的數值位置，從最小值放至最大值
            lineRectFArray.add(chart_line.getBarBounds(BarEntry(i.toFloat(), j)))
            j++
        }
        for (i in lineRectFArray.indices) {//放置標籤
            labelTextViewArray[i].y = lineRectFArray[i].top - (labelTextViewArray[i].height / 2f)
            labelTextViewArray[i].x = chart_line.x - labelTextViewArray[i].width.toFloat()
        }
        //    labelTextViewArray[0].y=lineRectFArray[0].top - labelTextViewArray[0].height/2f  //放置最底層的標籤
        if (!chartIsShowMinTextView) {
            labelTextViewArray[0].visibility = View.INVISIBLE
        }

        //視Radio id畫圖 放置完textView後將原本的繪圖清空
        chart_line.clear()
    }

    fun configFragment(input: Int) {
        useFor = input
    }

    // 20171128 Added by Raymond
    private fun configChartView() {
        val xAxis: XAxis = chart_line.xAxis
        val leftAxis: YAxis = chart_line.axisLeft
        val rightAxis: YAxis = chart_line.axisRight

        chart_line.isScaleXEnabled = false
        chart_line.isScaleYEnabled = false
        leftAxis.setLabelCount(chartLabelYCount, true)
        leftAxis.setAxisMaxValue(chartMax)  // the axis maximum is 1500
        leftAxis.setAxisMinValue(chartMin) // start at zero
        leftAxis.setDrawLabels(false) // no axis labels
        leftAxis.setDrawAxisLine(false) // no axis line
        leftAxis.setDrawGridLines(true) // no grid lines
        leftAxis.gridColor = Color.WHITE

        xAxis.setDrawGridLines(false)

        xAxis.position = XAxis.XAxisPosition.BOTTOM
        val nums = ArrayList<Float>()

        for (i in chartIntervalStart..chartIntervalEnd step chartIntervalStep) {
            nums.add(i.toFloat())
        }
        chart_line.legend.isEnabled = false
        chart_line.yChartInterval = nums
        chart_line.setDrawValueAboveBar(false)
        rightAxis.isEnabled = false
        chart_line.setDescription("")// clear default string
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putInt("useFor", useFor)
//        outState?.putInt("chartIntervalStep", chartIntervalStep)
//        outState?.putFloat("chartMin", chartMin)
//        outState?.putFloat("chartMax", chartMax)
//        outState?.putInt("chartIntervalStart", chartIntervalStart)
//        outState?.putInt("chartIntervalEnd", chartIntervalEnd)
//        outState?.putInt("chartLabelYCount", chartLabelYCount)
//        outState?.putBoolean("chartIsShowMinTextView", chartIsShowMinTextView)
//        outState?.putString("chartLabelUnit", chartLabelUnit)
//        outState?.putString("chartLabel", chartLabel)
        super.onSaveInstanceState(outState!!)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            useFor = savedInstanceState.getInt("useFor")
//            chartIntervalStep = savedInstanceState.getInt("chartIntervalStep")
//            chartMin = savedInstanceState.getFloat("chartMin")
//            chartMax = savedInstanceState.getFloat("chartMax")
//            chartIntervalStart = savedInstanceState.getInt("chartIntervalStart")
//            chartIntervalEnd = savedInstanceState.getInt("chartIntervalEnd")
//            chartLabelYCount = savedInstanceState.getInt("chartLabelYCount")
//            chartIsShowMinTextView = savedInstanceState.getBoolean("chartIsShowMinTextView")
//            chartLabelUnit = savedInstanceState.getString("chartLabelUnit")
//            chartLabel = savedInstanceState.getString("chartLabel")

        } else {
            // Probably initialize members with default values for a new instance
        }
        val myJsonFile=GetJson()
        val jsonArray = JSONArray(myJsonFile)
        for (i in 0..(jsonArray.length() - 1)) {
            val item = jsonArray.getJSONObject(i)
            val define = item.getInt("define")
            if (useFor == define)
            {
                chartLabel = item.getString("chartLabel")
                chartMin = item.getDouble("chartMin").toFloat()
                chartMax = item.getDouble("chartMax").toFloat()
                chartIntervalStep = item.getInt("chartIntervalStep")
                chartIntervalStart = item.getInt("chartIntervalStart")
                chartIntervalEnd = item.getInt("chartIntervalEnd")
                chartLabelYCount = item.getInt("chartLabelYCount")
                chartIsShowMinTextView = item.getBoolean("chartIsShowMinTextView")
                chartLabelUnit = item.getString("chartLabelUnit")
            }
        }
        for ((j, i) in (chartMin.toInt()..chartMax.toInt() step chartIntervalStep).withIndex()) {
            val textView = TextView(this.context)
            textView.width = 200
            textView.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            labelTextViewArray.add(textView)
            when (i) {
                chartMax.toInt() -> {
                    textView.text = chartLabelUnit
                }
                else -> {
                    when (useFor) {
                        DEFINE_FRAGMENT_TEMPERATURE -> {
                            textView.text = (chartMin - 10 + (j) * chartIntervalStep).toInt().toString()
                        }
                        else -> {
                            textView.text = (chartMin + (j) * chartIntervalStep).toInt().toString()
                        }
                    }
                }
            }
            RelativeLayoutForLabelTextView.addView(textView)
        }

        chart_line.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {

            }

            @SuppressLint("SetTextI18n", "SimpleDateFormat")
            override fun onValueSelected(e: Entry?, dataSetIndex: Int, h: Highlight?) {
                ChartSelectDetectionTime.text = labelArray[h!!.xIndex]
                when (useFor) {
                    DEFINE_FRAGMENT_TVOC -> {
                        val temp = e?.`val`
                        if (temp == 65538f) {
                            ChartSelectDetectionValue.text = getString(R.string.not_yetDetected)
                        } else {
                            ChartSelectDetectionValue.text = temp?.toInt().toString() + " ppb"
                        }

                        changeBackground(temp!!.toInt())
                    }
                    DEFINE_FRAGMENT_ECO2 -> {
                        val temp = e?.`val`
                        if (temp == 65538f) {
                            ChartSelectDetectionValue.text = getString(R.string.not_yetDetected)
                        } else {
                            ChartSelectDetectionValue.text = temp?.toInt().toString() + " ppm"
                        }

                        changeBackground(temp!!.toInt())
                    }
                    DEFINE_FRAGMENT_TEMPERATURE -> {
                        val temp: Float? = e?.`val`
                        val temp1: Float? = (temp!! - 10.0f)
                        if (temp1!! == 65528f) {
                            ChartSelectDetectionValue.text = getString(R.string.not_yetDetected)
                        } else {
                            val newTemp = "%.1f".format(temp1)
                            ChartSelectDetectionValue.text = "$newTemp ℃"
                        }

                        changeBackground(temp.toInt())
                    }
                    DEFINE_FRAGMENT_HUMIDITY -> {
                        val temp = e?.`val`
                        if (temp == 65538f) {
                            ChartSelectDetectionValue.text = getString(R.string.not_yetDetected)
                        } else {
                            ChartSelectDetectionValue.text = temp?.toInt().toString() + " %"
                        }

                        changeBackground(temp!!.toInt())
                    }
                    DEFINE_FRAGMENT_PM25 -> {
                        val temp = e?.`val`
                        if (temp == 65538f) {
                            ChartSelectDetectionValue.text = getString(R.string.not_yetDetected)
                        } else {
                            ChartSelectDetectionValue.text = temp?.toInt().toString() + " μg/m³"
                        }

                        changeBackground(temp!!.toInt())
                    }
                }
            }
        })

        //修改上排Spinner及Button
        val cycleList = ArrayAdapter.createFromResource(context, R.array.SpinnerArray, android.R.layout.simple_spinner_dropdown_item)
        sprChart.adapter = cycleList
        sprChart.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                view?.textAlignment = View.TEXT_ALIGNMENT_CENTER
                spinnerPositon = position
                when (spinnerPositon) {
                    0 -> {
                        averageExposureByTime.text = getString(R.string.averageExposure_Daily)
                    }
                    1 -> {
                        averageExposureByTime.text = getString(R.string.averageExposure_Daily)
                    }
                    2 -> {
                        averageExposureByTime.text = getString(R.string.averageExposure_Daily)
                    }
                    3 -> {
                        averageExposureByTime.text = getString(R.string.averageExposure_Daily)
                    }
                }
                btnTextChanged(spinnerPositon)
                drawChart(spinnerPositon)

                val selectedItem = parent.getItemAtPosition(position).toString()
//                if (selectedItem == "Add new category") {
//                    // do your stuff
//                }
                Log.d("Humi", selectedItem)
            } // to close the onItemSelected

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        val dateFormat = SimpleDateFormat("yyyy/MM/dd")
        btnCallDatePicker.text = dateFormat.format(calObject.time)
        btnCallDatePicker.setOnClickListener {
            if (Utils.isFastDoubleClick) {
                Utils.toastMakeTextAndShow(context!!, "連點，母湯喔！！",
                        Toast.LENGTH_SHORT)
            } else {
                datepickerHandler.post {
                    val dpd = DatePickerDialog(context!!, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        calObject.set(year, month, dayOfMonth)
                        Log.d("ChartBtncall" + useFor.toString(), calObject.get(Calendar.DAY_OF_MONTH).toString())
                        btnTextChanged(spinnerPositon)
                        drawChart(spinnerPositon)
                        timePickerShow()
                    }, calObject.get(Calendar.YEAR), calObject.get(Calendar.MONTH), calObject.get(Calendar.DAY_OF_MONTH))
                    dpd.setMessage("請選擇日期")
                    dpd.show()
                }
            }
        }

        when (useFor) {
            DEFINE_FRAGMENT_TVOC -> {
                ChartLabel.text = getString(R.string.text_label_tvoc)
                faceBar.setImageResource(R.drawable.face_bar_tvoc)
                intArray = intArrayOf(ContextCompat.getColor(mContext!!, R.color.Main_textResult_Good),
                        ContextCompat.getColor(context!!, R.color.Main_textResult_Moderate),
                        ContextCompat.getColor(context!!, R.color.Main_textResult_Orange),
                        ContextCompat.getColor(context!!, R.color.Main_textResult_Bad),
                        ContextCompat.getColor(context!!, R.color.Main_textResult_Purple),
                        ContextCompat.getColor(context!!, R.color.Main_textResult_Unhealthy))
            }
            DEFINE_FRAGMENT_ECO2 -> {
                ChartLabel.text = getString(R.string.text_label_co2)
                faceBar.setImageResource(R.drawable.face_bar_eco2)
                intArray = intArrayOf(ContextCompat.getColor(mContext!!, R.color.Main_textResult_Good),
                        //ContextCompat.getColor(context!!, R.color.Main_textResult_Moderate),
                        //ContextCompat.getColor(context!!, R.color.Main_textResult_Orange),
                        ContextCompat.getColor(context!!, R.color.Main_textResult_Bad))
                        //ContextCompat.getColor(context!!, R.color.Main_textResult_Purple),
                        //ContextCompat.getColor(context!!, R.color.Main_textResult_Unhealthy))
            }
            DEFINE_FRAGMENT_TEMPERATURE -> {
                ChartLabel.text = getString(R.string.text_label_temperature_full)
                faceBar.setImageResource(R.drawable.face_bar_temp)
                intArray = intArrayOf(ContextCompat.getColor(mContext!!, R.color.Main_textResult_Blue),
                        ContextCompat.getColor(context!!, R.color.Main_textResult_Good),
                        ContextCompat.getColor(context!!, R.color.Main_textResult_Bad))
            }
            DEFINE_FRAGMENT_HUMIDITY -> {
                ChartLabel.text = getString(R.string.text_label_humidity_full)
                faceBar.setImageResource(R.drawable.face_bar_humidity)
                intArray = intArrayOf(ContextCompat.getColor(context!!, R.color.Main_textResult_Blue),
                        ContextCompat.getColor(context!!, R.color.Main_textResult_Good),
                        ContextCompat.getColor(context!!, R.color.Main_textResult_Bad))
            }
            DEFINE_FRAGMENT_PM25 -> {
                ChartLabel.text = getString(R.string.text_label_pm25)
                faceBar.setImageResource(R.drawable.face_bar_pm25)
                intArray = intArrayOf(ContextCompat.getColor(mContext!!, R.color.Main_textResult_Good),
                        ContextCompat.getColor(context!!, R.color.Main_textResult_Moderate),
                        ContextCompat.getColor(context!!, R.color.Main_textResult_Orange),
                        ContextCompat.getColor(context!!, R.color.Main_textResult_Bad),
                        ContextCompat.getColor(context!!, R.color.Main_textResult_Purple),
                        ContextCompat.getColor(context!!, R.color.Main_textResult_Unhealthy))
            }
        }

        configChartView()
        //chart_line.setOnChartValueSelectedListener(this)

        btnExport.setOnClickListener {
            checkPermissions()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater!!.inflate(R.layout.frg_chart, container, false)


    private fun changeBackground(input: Int) {
        when (useFor) {
            DEFINE_FRAGMENT_TVOC -> {
                when (input) {
                    in 0..220 -> {
                        ChartBackground.setBackgroundResource(R.drawable.app_bg_cloud_green)
                    }
                    in 220..2199 -> {
                        ChartBackground.setBackgroundResource(R.drawable.app_bg_cloud_orange)
                    }
                    in 65538..65540 -> {
                        ChartBackground.setBackgroundResource(R.drawable.app_bg_cloud_green)
                    }
                    else -> {
                        ChartBackground.setBackgroundResource(R.drawable.app_bg_cloud_red)
                    }
                }
            }

            DEFINE_FRAGMENT_ECO2 -> {
                when (input) {
                    in 0..1499 -> {
                        ChartBackground.setBackgroundResource(R.drawable.app_bg_cloud_green)
                    }
                    in 65538..65540 -> {
                        ChartBackground.setBackgroundResource(R.drawable.app_bg_cloud_green)
                    }
                    else -> {
                        ChartBackground.setBackgroundResource(R.drawable.app_bg_cloud_red)
                    }
                }
            }

            DEFINE_FRAGMENT_TEMPERATURE -> {
                when (input) {
                    in 28..34 -> {
                        ChartBackground.setBackgroundResource(R.drawable.bg_temp_green)
                    }
                    in 35..210 -> {
                        ChartBackground.setBackgroundResource(R.drawable.bg_temp_red)
                    }
                    in 65538..65540 -> {
                        ChartBackground.setBackgroundResource(R.drawable.bg_temp_green)
                    }
                    else -> {
                        ChartBackground.setBackgroundResource(R.drawable.bg_temp_blue)
                    }
                }
            }

            DEFINE_FRAGMENT_HUMIDITY -> {
                when (input) {
                    in 45..65 -> {
                        ChartBackground.setBackgroundResource(R.drawable.bg_rh_green)
                    }
                    in 66..100 -> {
                        ChartBackground.setBackgroundResource(R.drawable.bg_rh_red)
                    }
                    in 65538..65540 -> {
                        ChartBackground.setBackgroundResource(R.drawable.bg_rh_green)
                    }
                    else -> {
                        ChartBackground.setBackgroundResource(R.drawable.bg_rh_blue)
                    }
                }
            }
            DEFINE_FRAGMENT_PM25 -> {
                when (input) {
                    in 0..15 -> {
                        ChartBackground.setBackgroundResource(R.drawable.app_bg_cloud_green)
                    }
                    in 16..54 -> {
                        ChartBackground.setBackgroundResource(R.drawable.app_bg_cloud_orange)
                    }
                    in 65538..65540 -> {
                        ChartBackground.setBackgroundResource(R.drawable.app_bg_cloud_green)
                    }
                    else -> {
                        ChartBackground.setBackgroundResource(R.drawable.app_bg_cloud_red)
                    }
                }
            }
        }
    }

    private lateinit var mActivity: Activity

    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    override fun onAttach(activity: Activity?) {
        super.onAttach(activity!!)
        mContext = this.context!!.applicationContext
        mActivity = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(mContext!!).registerReceiver(mGattUpdateReceiver,
                makeMainFragmentUpdateIntentFilter())
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun btnTextChanged(position: Int?) {
        when (position) {
            0 -> {
                val dateFormat = SimpleDateFormat("yyyy/MM/dd")
                btnCallDatePicker.text = dateFormat.format(calObject.time)
            }
            1 -> {
                btnCallDatePicker.text = calObject.get(Calendar.YEAR).toString() + " " +
                        getString(R.string.week_First_Word) +
                        calObject.get(Calendar.WEEK_OF_YEAR).toString() +
                        getString(R.string.week_Last_Word)
            }
            2 -> {
                val dateFormat = SimpleDateFormat("yyyy/MM")
                btnCallDatePicker.text = dateFormat.format(calObject.time)
            }
            3 -> {
                val dateFormat = SimpleDateFormat("yyyy")
                btnCallDatePicker.text = dateFormat.format(calObject.time)
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun drawChart(position: Int?) {
        setImageBarPosition()

        when (position) {
            0 -> {
                val p = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60 * 60 +
                        Calendar.getInstance().get(Calendar.MINUTE) * 60 +
                        Calendar.getInstance().get(Calendar.SECOND)
                val l = p / 60
                if (l <= 2) {
                    calObject.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    Log.d("drawChart" + useFor.toString(), calObject.toString())
                }
                getRealmDay()
                chart_line.data = buildBarData(arrData, arrTime, position)
                chart_line.data?.setDrawValues(false)
                chart_line.setVisibleXRange(14.0f, 14.0f)
                //chart_line.setVisibleXRangeMinimum(20.0f)
                //chart_line.setVisibleXRangeMaximum(20.0f)//需要在设置数据源后生效
                //chart_line.centerViewToAnimated((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                //        + Calendar.getInstance().get(Calendar.MINUTE) / 60F) * 120F,0F, YAxis.AxisDependency.LEFT,1000)
                if (useFor == DEFINE_FRAGMENT_PM25) {
                    chart_line.centerViewToAnimated(l.toFloat() / 5, 0F,
                            YAxis.AxisDependency.LEFT, 1000)
                } else {
                    chart_line.centerViewToAnimated(l.toFloat(), 0F,
                            YAxis.AxisDependency.LEFT, 1000)
                }
                //chart_line.moveViewToX((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                //        + Calendar.getInstance().get(Calendar.MINUTE) / 60F) * 118.5F) //移動視圖by x index
                val y = chart_line.data!!.dataSetCount
                chart_line.highlightValue(l, y - 1)
                //Log.v("Highligh:",l.toString())
            }
            1 -> {
                getRealmWeek()
                chart_line.data = buildBarData(arrData, arrTime, position)
                chart_line.data?.setDrawValues(false)
                chart_line.animateY(3000, Easing.EasingOption.EaseOutBack)
                chart_line.setVisibleXRange(7.0f, 7.0f)
                chart_line.centerViewToAnimated(Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toFloat(),
                        0F, YAxis.AxisDependency.LEFT, 1000)
            }
            2 -> {
                getRealmMonth()
                chart_line.data = buildBarData(arrData, arrTime, position)
                chart_line.data?.setDrawValues(false)
                chart_line.animateY(3000, Easing.EasingOption.EaseOutBack)
                chart_line.setVisibleXRange(14.0f, 14.0f)
            }

            3 -> {
                getRealmYear()
                chart_line.data = buildBarData(arrData, arrTime, position)
                chart_line.data?.setDrawValues(false)
                chart_line.animateY(3000, Easing.EasingOption.EaseOutBack)
                chart_line.setVisibleXRange(12.0f, 12.0f)
            }
        }

    }

    override fun onStart() {
        super.onStart()
        checkUIState()

        // 將日期初始化成今天
        val calendar = Calendar.getInstance()

        calObject.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
        //spinnerPositon = 0
        //btnTextChanged(spinnerPositon)
        //drawChart(spinnerPositon)
    }

    override fun onResume() {
        super.onResume()
        //視Radio id畫圖
        //dependRadioIDDrawChart(radioButtonID)
        btnTextChanged(spinnerPositon)
        drawChart(spinnerPositon)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            LocalBroadcastManager.getInstance(mContext!!).unregisterReceiver(mGattUpdateReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    private fun checkUIState() {
        if (mConnectStatus) {
            if (animationCount > 1440) {
                downloadingData = false
                setProgressBarZero()
            }
        } else {
            downloadingData = false
            setProgressBarZero()
        }
    }

    private fun getDeviceData() {
        when (useFor) {
            DEFINE_FRAGMENT_TVOC -> {
                if (mConnectStatus && !downloadingData) {
                    val intent: Intent? = Intent(BroadcastIntents.PRIMARY)
                    intent!!.putExtra("status", BroadcastActions.ACTION_GET_SAMPLE_RATE)
                    context!!.sendBroadcast(intent)
                    Log.d("Fragment" + useFor.toString(), "getDeviceData")
                }
            }
            else -> {
            }
        }

    }

    private fun setProgressBarMax(input: Int) {
        chartDataLoading.progress = 0
        chartDataLoading.max = input
    }

    private fun setProgressBarNow(input: Int) {
        chartDataLoading.progress = input
    }

    private fun setProgressBarZero() {
        chartDataLoading.progress = 0
    }

    private fun cleanTextViewInTVOC() {
        ChartSelectDetectionValue.text = ""
        ChartSelectDetectionTime.text = ""
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun getRealmDay() {
        arrTime.clear()
        arrData.clear()
        //現在時間實體毫秒
        //var touchTime = Calendar.getInstance().timeInMillis
        val touchTime = if (calObject.get(Calendar.HOUR_OF_DAY) >= 8) calObject.timeInMillis else calObject.timeInMillis + calObject.timeZone.rawOffset
        Log.d("TVOCbtncallRealm" + useFor.toString(), calObject.get(Calendar.HOUR).toString())
        //將日期設為今天日子加一天減1秒
        val endDay = touchTime / (3600000 * 24) * (3600000 * 24) - calObject.timeZone.rawOffset
        val endDayLast = endDay + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
        val realm = Realm.getDefaultInstance()
        val query = realm.where(AsmDataModel::class.java)
        //一天共有2880筆
        val dataCount = (endDayLast - endDay) / (60 * 1000)
        Log.d("TimePeriod" + useFor.toString(), (dataCount.toString() + "thirtySecondsCount"))
        query.between("Created_time", endDay, endDayLast).sort("Created_time", Sort.ASCENDING)
        val result1 = query.findAll()
        Log.d("getRealmDay" + useFor.toString(), result1.size.toString())
        var avgValueFloat = 0.0f
        var avgValueInt = 0
        var sumValueFloat = 0.0f
        var sumValueInt = 0

        //先生出2880筆值為0的陣列
        if (useFor == DEFINE_FRAGMENT_PM25) {
            for (y in 0..dataCount step 5) {
                arrData.add("65538")
                arrTime.add((endDay + y * 60 * 1000).toString())
            }
        } else {
            for (y in 0..dataCount) {
                arrData.add("65538")
                arrTime.add((endDay + y * 60 * 1000).toString())
            }
        }

        //關鍵!!利用取出的資料減掉抬頭時間除以30秒算出index換掉TVOC的值
        if (result1.size != 0) {
            result1.forEachIndexed { _, asmDataModel ->
                val count = ((asmDataModel.created_time - endDay) / (60 * 1000)).toInt()
                when (useFor) {
                    DEFINE_FRAGMENT_TVOC -> {
                        arrData[count] = asmDataModel.tvocValue.toString()
                        sumValueInt += arrData[count].toInt()
                    }
                    DEFINE_FRAGMENT_ECO2 -> {
                        arrData[count] = asmDataModel.ecO2Value.toString()
                        sumValueInt += arrData[count].toInt()
                    }
                    DEFINE_FRAGMENT_TEMPERATURE -> {
                        arrData[count] = (asmDataModel.tempValue.toFloat() + 10.0F).toString()
                        sumValueFloat += arrData[count].toFloat()
                    }
                    DEFINE_FRAGMENT_HUMIDITY -> {
                        arrData[count] = asmDataModel.humiValue.toString()
                        sumValueInt += arrData[count].toInt()
                    }
                    DEFINE_FRAGMENT_PM25 -> {
                        val count = ((asmDataModel.created_time - endDay) / (60 * 1000 * 5)).toInt()
                        arrData[count] = asmDataModel.pM25Value.toString()
                        sumValueInt += arrData[count].toInt()
                    }
                }
                //Log.v("hilightCount:", count.toString())
            }
            Log.d("getRealmDay" + useFor.toString(), result1.last().toString())
            //20180122
            when (useFor) {
                DEFINE_FRAGMENT_TEMPERATURE -> {
                    avgValueFloat = (sumValueFloat / result1.size) - 10.0f
                }
                else -> {
                    avgValueInt = (sumValueInt / result1.size)
                }
            }
        }
        //前一天的0點起
        val sqlWeekBase = endDay - TimeUnit.DAYS.toMillis((1).toLong())
        // Show Date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        show_Today.text = dateFormat.format(endDay)
        show_Yesterday!!.text = dateFormat.format(endDay - TimeUnit.DAYS.toMillis((1).toLong()))
        //Log.d("getRealmWeek", sqlWeekBase.toString())
        //跑七筆BarChart
        // for (y in 0..1) {
        //結束點為日 23:59
        val sqlEndDate = sqlWeekBase + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
        //val realm= Realm.getDefaultInstance()
        val query1 = realm.where(AsmDataModel::class.java)
        //20180122
        val avgTVOC3: Float
        val avgPM25 = 0.0F
        Log.d("getRealmWeek" + useFor.toString(), sqlWeekBase.toString())
        Log.d("getRealmWeek" + useFor.toString(), sqlEndDate.toString())
        query1.between("Created_time", sqlWeekBase, sqlEndDate)
        val result2 = query1.findAll()
        Log.d("getRealmWeek" + useFor.toString(), result2.size.toString())
        if (result2.size != 0) {
            var sumYesterday = 0.0F
            for (i in result2) {
                when (useFor) {
                    DEFINE_FRAGMENT_TVOC -> {
                        sumYesterday += i.tvocValue.toInt()
                    }
                    DEFINE_FRAGMENT_ECO2 -> {
                        sumYesterday += i.ecO2Value.toInt()
                    }
                    DEFINE_FRAGMENT_TEMPERATURE -> {
                        sumYesterday += i.tempValue.toFloat()
                    }
                    DEFINE_FRAGMENT_HUMIDITY -> {
                        sumYesterday += i.humiValue.toInt()
                    }
                    DEFINE_FRAGMENT_PM25 -> {
                        sumYesterday += i.pM25Value.toInt()
                    }
                }
            }
            avgTVOC3 = (sumYesterday / result2.size)
        } else {
            avgTVOC3 = 0.0F
        }
        //}
        when (useFor) {
            DEFINE_FRAGMENT_TVOC -> {
                result_Today.text = avgValueInt.toString() + " ppb"
                result_Yesterday.text = avgTVOC3.toInt().toString() + " ppb"
            }
            DEFINE_FRAGMENT_ECO2 -> {
                result_Today.text = avgValueInt.toString() + " ppm"
                result_Yesterday.text = avgTVOC3.toInt().toString() + " ppm"
            }
            DEFINE_FRAGMENT_TEMPERATURE -> {
                result_Today.text = "%.1f".format(avgValueFloat) + " ℃"
                result_Yesterday.text = "%.1f".format(avgTVOC3) + " ℃"
            }
            DEFINE_FRAGMENT_HUMIDITY -> {
                result_Today.text = avgValueInt.toString() + " %"
                result_Yesterday.text = avgTVOC3.toInt().toString() + " %"
            }
            DEFINE_FRAGMENT_PM25 -> {
                result_Today.text = avgValueInt.toString() + " μg/m³"
                result_Yesterday.text = avgTVOC3.toInt().toString() + " μg/m³"
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getRealmWeek() {
        arrTime.clear()
        arrData.clear()
        //拿到現在是星期幾的Int
        val dayOfWeek = calObject.get(Calendar.DAY_OF_WEEK)
        val touchTime = if (calObject.get(Calendar.HOUR_OF_DAY) >= 8) calObject.timeInMillis else calObject.timeInMillis + calObject.timeZone.rawOffset
        //今天的00:00
        val nowDateMills = touchTime / (3600000 * 24) * (3600000 * 24) - calObject.timeZone.rawOffset
        //將星期幾退回到星期日為第一時間點
        val sqlWeekBase = nowDateMills - TimeUnit.DAYS.toMillis((dayOfWeek - 1).toLong())
        var thisWeekAVETvoc: Float
        //var aveLastWeekTvoc = 0
        Log.d("getRealmWeek" + useFor.toString(), sqlWeekBase.toString())
        //跑七筆BarChart
        for (y in 0..6) {
            //第一筆為日 00:00
            val sqlStartDate = sqlWeekBase + TimeUnit.DAYS.toMillis(y.toLong())
            //結束點為日 23:59
            val sqlEndDate = sqlStartDate + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
            val realm = Realm.getDefaultInstance()
            val query = realm.where(AsmDataModel::class.java)
            Log.e("thisGetRealmWeekStart" + useFor.toString(), sqlStartDate.toString())
            Log.e("thisGetRealmWeekEnd" + useFor.toString(), sqlEndDate.toString())
            query.between("Created_time", sqlStartDate, sqlEndDate)
            val result1 = query.findAll()
            Log.d("getRealmWeek" + useFor.toString(), result1.size.toString())
            if (result1.size != 0) {
                var sumThisAndLastWeek = 0f
                for (i in result1) {
                    when (useFor) {
                        DEFINE_FRAGMENT_TVOC -> {
                            sumThisAndLastWeek += i.tvocValue.toInt()
                        }
                        DEFINE_FRAGMENT_ECO2 -> {
                            sumThisAndLastWeek += i.ecO2Value.toInt()
                        }
                        DEFINE_FRAGMENT_TEMPERATURE -> {
                            sumThisAndLastWeek += i.tempValue.toFloat() + 10.0f
                        }
                        DEFINE_FRAGMENT_HUMIDITY -> {
                            sumThisAndLastWeek += i.humiValue.toInt()
                        }
                        DEFINE_FRAGMENT_PM25 -> {
                            sumThisAndLastWeek += i.pM25Value.toInt()
                        }
                    }
                }
                thisWeekAVETvoc = (sumThisAndLastWeek / result1.size)
                arrData.add(thisWeekAVETvoc.toString())
                //依序加入時間
                arrTime.add((sqlStartDate).toString())
            } else {
                //result_Today.text = "$lastWeekAVETvoc ppb"
                arrData.add("65538")
                arrTime.add((sqlStartDate).toString())
            }
        }
    }

    private fun getRealmMonth() {
        arrTime.clear()
        arrData.clear()
        //拿到現在是星期幾的Int
        val dayOfMonth = calObject.get(Calendar.DAY_OF_MONTH)
        val monthCount = calObject.getActualMaximum(Calendar.DAY_OF_MONTH)
        val touchTime = if (calObject.get(Calendar.HOUR_OF_DAY) >= 8) calObject.timeInMillis else calObject.timeInMillis + calObject.timeZone.rawOffset
        val nowDateMills = touchTime / (3600000 * 24) * (3600000 * 24) - calObject.timeZone.rawOffset
        //將星期幾退回到星期日為第一時間點
        val sqlMonthBase = nowDateMills - TimeUnit.DAYS.toMillis((dayOfMonth - 1).toLong())
        Log.d("getRealmMonth" + useFor.toString(), sqlMonthBase.toString())
        //跑七筆BarChart
        for (y in 0..(monthCount - 1)) {
            //第一筆為日 00:00
            val sqlStartDate = sqlMonthBase + TimeUnit.DAYS.toMillis(y.toLong())
            //結束點為日 23:59
            val sqlEndDate = sqlStartDate + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
            val realm = Realm.getDefaultInstance()
            val query = realm.where(AsmDataModel::class.java)
            val dataCount = (sqlEndDate - sqlStartDate) / (60 * 1000)
            Log.d("TimePeriod" + useFor.toString(), (dataCount.toString() + "thirtySecondsCount"))
            Log.d("getRealmMonth" + useFor.toString(), sqlStartDate.toString())
            Log.d("getRealmMonth" + useFor.toString(), sqlEndDate.toString())
            query.between("Created_time", sqlStartDate, sqlEndDate)
            val result1 = query.findAll()
            Log.d("getRealmMonth" + useFor.toString(), result1.size.toString())
            if (result1.size != 0) {
                var sumMonth = 0f
                for (i in result1) {
                    when (useFor) {
                        DEFINE_FRAGMENT_TVOC -> {
                            sumMonth += i.tvocValue.toInt()
                        }
                        DEFINE_FRAGMENT_ECO2 -> {
                            sumMonth += i.ecO2Value.toInt()
                        }
                        DEFINE_FRAGMENT_TEMPERATURE -> {
                            sumMonth += i.tempValue.toFloat() + 10.0f
                        }
                        DEFINE_FRAGMENT_HUMIDITY -> {
                            sumMonth += i.humiValue.toInt()
                        }
                        DEFINE_FRAGMENT_PM25 -> {
                            sumMonth += i.pM25Value.toInt()
                        }
                    }
                }
                val aveTvoc = (sumMonth / result1.size)
                arrData.add(aveTvoc.toString())
                //依序加入時間
                arrTime.add(sqlStartDate.toString())
                Log.d("getRealmMonth" + useFor.toString(), result1.last().toString())
            } else {
                arrData.add("65538")
                arrTime.add((sqlStartDate).toString())
            }
        }

    }

    private fun buildBarData(inputValue: ArrayList<String>, inputTime: ArrayList<String>, positionID: Int?): BarData {
        val dataSetA = MyBarDataSet(getChartData3(inputValue), chartLabel)
        dataSetA.setColors(intArray)

        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(dataSetA) // add the datasets
        cleanTextViewInTVOC()
        return BarData(getLabels3(inputTime, positionID), dataSets)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getLabels3(input: ArrayList<String>, positionID: Int?): List<String> {
        val chartLabels = ArrayList<String>()
        when (positionID) {
            0 -> {
                val dateFormat = SimpleDateFormat("HH:mm")
                val dateLabelFormat = SimpleDateFormat("MM/dd HH:mm")
                labelArray.clear()
                for (i in 0 until arrTime.size) {
                    val date = dateFormat.format(input[i].toLong())
                    val dateLabel = dateLabelFormat.format(input[i].toLong())
                    chartLabels.add(date)
                    labelArray.add(dateLabel)
                    //Log.v("Label Array", "index $i: $dateLabel")
                }
            }
            1 -> {
                val dateFormat = SimpleDateFormat("EEEE")
                val dateLabelFormat = SimpleDateFormat("MM/dd EEEE")
                labelArray.clear()
                for (i in 0 until arrTime.size) {
                    val date = dateFormat.format(input[i].toLong())
                    val dateLabel = dateLabelFormat.format(input[i].toLong())
                    chartLabels.add(date)
                    labelArray.add(dateLabel)
                }
                result_Today.text = getString(R.string.text_default_value)
                result_Yesterday.text = getString(R.string.text_default_value)
                show_Today.text = getString(R.string.text_default_value)
                show_Yesterday!!.text = getString(R.string.text_default_value)
            }
            2 -> {
                val dateFormat = SimpleDateFormat("MM/dd")
                val dateLabelFormat = SimpleDateFormat("yyyy/MM/dd")
                labelArray.clear()
                for (i in 0 until arrTime.size) {
                    val date = dateFormat.format(input[i].toLong())
                    val dateLabel = dateLabelFormat.format(input[i].toLong())
                    chartLabels.add(date)
                    labelArray.add(dateLabel)
                }
                result_Today.text = getString(R.string.text_default_value)
                result_Yesterday.text = getString(R.string.text_default_value)
                show_Today.text = getString(R.string.text_default_value)
                show_Yesterday!!.text = getString(R.string.text_default_value)
            }
            3 -> {
                val dateFormat = SimpleDateFormat("yyyy-MM")
                val dateLabelFormat = SimpleDateFormat("yyyy-MM")
                labelArray.clear()
                for (i in 0 until input.size) {
                    val date = dateFormat.format(input[i].toLong())
                    val dateLabel = dateLabelFormat.format(input[i].toLong())
                    chartLabels.add(date)
                    labelArray.add(dateLabel)
                }
                result_Today.text = getString(R.string.text_default_value)
                result_Yesterday.text = getString(R.string.text_default_value)
                show_Today.text = getString(R.string.text_default_value)
                show_Yesterday!!.text = getString(R.string.text_default_value)
            }
        }
        Log.d("TVOCGETLABEL3" + useFor.toString(), chartLabels.lastIndex.toString())
        return chartLabels
    }

    private fun getChartData3(input: ArrayList<String>): List<BarEntry> {
        val chartData = ArrayList<BarEntry>()
        for (i in 0 until arrTime.size) {
            chartData.add(BarEntry(input[i].toFloat(), i))
        }
        return chartData
    }

    private fun makeMainFragmentUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BroadcastActions.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BroadcastActions.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BroadcastActions.ACTION_GET_NEW_DATA)
        intentFilter.addAction(BroadcastActions.ACTION_GET_HISTORY_COUNT)
        intentFilter.addAction(BroadcastActions.ACTION_LOADING_DATA)
        intentFilter.addAction(BroadcastActions.ACTION_SAVE_INSTANT_DATA)
        intentFilter.addAction(BroadcastActions.ACTION_DATA_AVAILABLE)
        return intentFilter
    }

    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                BroadcastActions.ACTION_GATT_DISCONNECTED -> {
                    //執行斷線後的事
                    counter = 0
                    mConnectStatus = false
                }
                BroadcastActions.ACTION_GATT_CONNECTED -> {
                    //執行連線後的事
                    counter = 0
                    mConnectStatus = true
                    downloadComplete = false
                }
                BroadcastActions.ACTION_GET_HISTORY_COUNT -> {
                    val bundle = intent.extras
                    val totalData = bundle.getString(BroadcastActions.INTENT_KEY_GET_HISTORY_COUNT)
                    //setProgressBarMax(1440)
                    setProgressBarMax(totalData.toInt())
                }
                BroadcastActions.ACTION_LOADING_DATA -> {
                    val bundle = intent.extras
                    val nowData = bundle.getString(BroadcastActions.INTENT_KEY_LOADING_DATA)
                    setProgressBarNow(nowData.toInt())
                }
                BroadcastActions.ACTION_GET_NEW_DATA -> {
                    if (!downloadingData && !downloadComplete) {
                        getDeviceData()
                        downloadingData = true
                    }
                    val bundle = intent.extras
                    var tvocVal = "0"
                    var eco2Val = "0"
                    var tempVal = "0"
                    var humiVal = "0"
                    var pm25Val = "0"
                    when (useFor) {
                        DEFINE_FRAGMENT_TVOC -> {
                            tvocVal = bundle.getString(BroadcastActions.INTENT_KEY_TVOC_VALUE)
                        }
                        DEFINE_FRAGMENT_ECO2 -> {
                            eco2Val = bundle.getString(BroadcastActions.INTENT_KEY_TVOC_VALUE)
                        }
                        DEFINE_FRAGMENT_TEMPERATURE -> {
                            tempVal = bundle.getString(BroadcastActions.INTENT_KEY_TEMP_VALUE)
                        }
                        DEFINE_FRAGMENT_HUMIDITY -> {
                            humiVal = bundle.getString(BroadcastActions.INTENT_KEY_HUMI_VALUE)
                        }
                        DEFINE_FRAGMENT_PM25 -> {
                            pm25Val = bundle.getString(BroadcastActions.INTENT_KEY_PM25_VALUE)
                        }
                    }

                    //    val humiVal = bundle.getString(BroadcastActions.INTENT_KEY_HUMI_VALUE)
                    preHeat = bundle.getString(BroadcastActions.INTENT_KEY_PREHEAT_COUNT)
                    if (preHeat == "255") {
                        //新增AnimationCount
                        animationCount++
                        counter++
                        when (useFor) {
                            DEFINE_FRAGMENT_TVOC -> {
                                valueIntAVG += tvocVal.toInt()
                            }
                            DEFINE_FRAGMENT_ECO2 -> {
                                valueIntAVG += eco2Val.toInt()
                            }
                            DEFINE_FRAGMENT_TEMPERATURE -> {
                                valueFloatAVG += tempVal.toFloat()
                            }
                            DEFINE_FRAGMENT_HUMIDITY -> {
                                valueIntAVG += humiVal.toInt()
                            }
                            DEFINE_FRAGMENT_PM25 -> {
                                valueIntAVG += pm25Val.toInt()
                            }
                        }

                        if (counter % getDataCycle == 0) {
                            counter = 0
                            when (useFor) {
                                DEFINE_FRAGMENT_TEMPERATURE -> {
                                    valueFloatAVG /= getDataCycle
                                    valueFloatAVG = 0.0
                                }
                                else -> {
                                    valueIntAVG /= getDataCycle
                                    valueIntAVG = 0
                                }
                            }

                        }
                    }
                }
                BroadcastActions.ACTION_SAVE_INSTANT_DATA -> {
                    if (spinnerPositon == 0) {
                        btnTextChanged(spinnerPositon)
                        drawChart(spinnerPositon)
                    }
                }
                BroadcastActions.ACTION_DATA_AVAILABLE -> {
                    dataAvaliable(intent)
                }
            }
            checkUIState()
        }
    }

    private fun getBarData(): BarData {
        val dataSetA = MyBarDataSet(getChartData(), chartLabel)
        dataSetA.setColors(intArray)

        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(dataSetA) // add the datasets

        return BarData(getLabels(), dataSets)
    }

    private fun getChartData(): List<BarEntry> {
        val chartData = ArrayList<BarEntry>()
        var j = 0
        for (i in chartMin.toInt()..chartMax.toInt() step chartIntervalStep) {
            j += 1
            chartData.add(BarEntry((i).toFloat(), j))
        }
        return chartData
    }

    private fun getLabels(): List<String> {
        val chartLabels = ArrayList<String>()
        for (i in 1 until mDataCount) {
            chartLabels.add("X$i")
        }
        return chartLabels
    }

    private fun timePickerShow() {
        if (spinnerPositon == 0) {
            val tpd = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                val p = hourOfDay * 60 + minute
                chart_line.centerViewToAnimated(p.toFloat(), 0F, YAxis.AxisDependency.LEFT, 1000)
                //chart_line.moveViewToX((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                //        + Calendar.getInstance().get(Calendar.MINUTE) / 60F) * 118.5F) //移動視圖by x index
                val y = chart_line.data!!.dataSetCount
                chart_line.highlightValue(p, y - 1)
            }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), true)
            tpd.setMessage("請選擇時間")
            tpd.show()
        }
    }


    private fun dataAvaliable(intent: Intent) {
        val txValue = intent.getByteArrayExtra(BroadcastActions.ACTION_EXTRA_DATA)
        when (txValue[0]) {
            0xE0.toByte() -> {
            }
            0xE1.toByte() -> {
            }
            0xEA.toByte() -> {
            }
            else -> {
            }
        }
        when (txValue[2]) {
            0xB1.toByte() -> Log.d("AirMapAC", "cmd:0xB1 feedback")
            0xB2.toByte() -> Log.d("AirMapAC", "cmd:0xB2 feedback")
            0xB4.toByte() -> Log.d("AirMapAC", "cmd:0xB4 feedback")
            0xB5.toByte() -> Log.d("AirMapAC", "cmd:0xB5 feedback")
            0xB9.toByte() -> Log.d("AirMapAC", "cmd:0xB9 feedback")
        }
        when (txValue[3]) {
            0xE0.toByte() -> {
                Log.d("AirMapAC feeback", "ok"); }
            0xE1.toByte() -> {
                Log.d("AirMapAC feedback", "Couldn't write in device"); return
            }
            0xE2.toByte() -> {
                Log.d("AirMapAC feedback", "Temperature sensor fail"); return
            }
            0xE3.toByte() -> {
                Log.d("AirMapAC feedback", "B0TVOC sensor fail"); return
            }
            0xE4.toByte() -> {
                Log.d("AirMapAC feedback", "Pump power fail"); return
            }
            0xE5.toByte() -> {
                Log.d("AirMapAC feedback", "Invalid value"); return
            }
            0xE6.toByte() -> {
                Log.d("AirMapAC feedback", "Unknown command"); return
            }
            0xE7.toByte() -> {
                Log.d("AirMapAC feedback", "Waiting timeout"); return
            }
            0xE8.toByte() -> {
                Log.d("AirMapAC feedback", "Checksum error"); return
            }
        }

        if (errorTime >= 3) {
            errorTime = 0
        }
        if (!Utils.checkCheckSum(txValue)) {
            errorTime += 1
        } else {
            when (txValue[2]) {
                0xB0.toByte() -> {
                }
                0xB1.toByte() -> {
                }
                0xB2.toByte() -> {
                }
                0xB4.toByte() -> {
                }
                0xB5.toByte() -> {
                }
                0xB9.toByte() -> {
                }
                0xE0.toByte() -> {
                }
                0xBB.toByte() -> {
                }
                0xC0.toByte() -> {
                }
                0xC5.toByte() -> {
                }
                0xC6.toByte() -> {
                    if (spinnerPositon == 0) {
                        btnTextChanged(spinnerPositon)
                        drawChart(spinnerPositon)
                    }
                    Log.e("ChartFrg", "Now Starting Load Data.........")
                }
            }
        }
    }

    private fun GetJson():String {
        val `is` = resources.openRawResource(R.raw.range_standard)
        val writer = StringWriter()
        val buffer = CharArray(1024)
        try {
            val reader = BufferedReader(InputStreamReader(`is`, "UTF-8"))
            var n: Int
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


    @SuppressLint("SimpleDateFormat")
    private fun parseDataToCsv() {
        val foldeName = "ADDWII Mobile Nose"
        val date = SimpleDateFormat("yyyyMMdd")

        val type = when (useFor) {
            1 -> "TVOC"
            2 -> "eCO2"
            3 -> "Temperature"
            4 -> "Humidity"
            else -> "PM25"
        }

        val fileName = "${date.format(calObject.timeInMillis)}_${type}_Mobile_Nose"

        val writeCSV = CSVWriter(foldeName, fileName, CSVWriter.COMMA_SEPARATOR)

        val timeFormat = SimpleDateFormat(when (spinnerPositon) {
            0 -> "HH:mm"
            1 -> "EE"
            2 -> "MM/dd"
            else -> "yyyyMM"
        })

        val timeUnit = when (spinnerPositon) {
            0 -> "Time"
            1 -> "Week"
            2 -> "Day"
            else -> "Month"
        }

        val dataUnit = when (useFor) {
            1 -> "ppb"
            2 -> "ppm"
            3 -> "°C"
            4 -> "%"
            else -> "μg/m³"
        }

        val header = arrayOf("id", timeUnit, "Value")

        writeCSV.writeLine(header)

        for (i in 0 until arrData.size) {
            val time = arrTime[i].toLong()
            val dataVal = if (arrData[i] == "65538") "No Data" else "${arrData[i]} $dataUnit"

            val textCSV = arrayOf((i + 1).toString(), timeFormat.format(time), dataVal)

            writeCSV.writeLine(textCSV).toString()
        }

        writeCSV.close()

        Utils.toastMakeTextAndShow(context!!, getString(R.string.text_export_success_msg), Toast.LENGTH_SHORT)
    }

    private fun checkPermissions() {

        if (ActivityCompat.checkSelfPermission(context!!, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 2)
        } else {
            Log.e("ChectPerm", "Permission Granted. Starting export data...")
            parseDataToCsv()
        }
    }

    private fun getRealmYear() {
        arrTime.clear()
        arrData.clear()

        val getYearCal = Calendar.getInstance()
        getYearCal.set(Calendar.YEAR, calObject.get(Calendar.YEAR))
        getYearCal.set(Calendar.DAY_OF_YEAR, 1)
        getYearCal.set(Calendar.HOUR_OF_DAY, 0) // ! clear would not reset the hour of day ! //這幾行是新寫法，好用
        getYearCal.clear(Calendar.MINUTE) //這幾行是新寫法，好用
        getYearCal.clear(Calendar.SECOND) //這幾行是新寫法，好用
        getYearCal.clear(Calendar.MILLISECOND) //這幾行是新寫法，好用

        for (y in 0..11) {
            val sqlStartDate = getYearCal.timeInMillis
            getYearCal.add(Calendar.MONTH, 1) //加1個月
            val sqlEndDate = getYearCal.timeInMillis - TimeUnit.SECONDS.toMillis(1)
            val realm = Realm.getDefaultInstance()
            val query = realm.where(AsmDataModel::class.java)
            query.between("Created_time", sqlStartDate, sqlEndDate)
            val result1 = query.findAll()

            if (result1.size != 0) {
                var sumMonth = 0f
                for (i in result1) {
                    when (useFor) {
                        DEFINE_FRAGMENT_TVOC -> {
                            sumMonth += i.tvocValue.toInt()
                        }
                        DEFINE_FRAGMENT_ECO2 -> {
                            sumMonth += i.ecO2Value.toInt()
                        }
                        DEFINE_FRAGMENT_TEMPERATURE -> {
                            sumMonth += i.tempValue.toFloat() + 10.0f
                        }
                        DEFINE_FRAGMENT_HUMIDITY -> {
                            sumMonth += i.humiValue.toInt()
                        }
                        DEFINE_FRAGMENT_PM25 -> {
                            sumMonth += i.pM25Value.toInt()
                        }
                    }
                }
                arrData.add((sumMonth / result1.size).toString())
                //依序加入時間
                arrTime.add((sqlStartDate).toString())
            } else {
                arrData.add("65538")
                //依序加入時間
                arrTime.add((sqlStartDate).toString())
            }
        }
    }
}