package com.microjet.airqi2.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.RectF
import android.os.Bundle
import android.os.Handler
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
import com.microjet.airqi2.CustomAPI.MyBarDataSet
import com.microjet.airqi2.CustomAPI.Utils
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.R
import com.microjet.airqi2.TvocNoseData
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.frg_chart.*
import java.text.SimpleDateFormat
import java.time.Month
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * Created by B00055 on 2018/2/9.
 *
 */

class Pm10Fragment : Fragment() {
    private val DEFINE_FRAGMENT_PM10 = 6
    private var mContext: Context? = null
    private var mDataCount: Int = 60
    private var mConnectStatus: Boolean = false


    private var animationCount = 0
    private var downloadingData = false

    private val calObject = Calendar.getInstance()
    private var spinnerPositon = 0
    private var datepickerHandler = Handler()

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

        //視Radio id畫圖
        chart_line.clear()
    }

    fun configFragment(input: Int) {
        useFor = input
        when (input) {
            DEFINE_FRAGMENT_PM10 -> {

//                chartLabel = "PM10"
//                chartMin = 0.0f
//                chartMax = 100.0f
//                chartIntervalStep = 20
//                chartIntervalStart = 20
//                chartIntervalEnd = 80
//                chartLabelYCount = 11
//                chartIsShowMinTextView = false
//                chartLabelUnit = "(μg/m³)"

                chartLabel = "PM10"
                chartMin = 0.0f
                chartMax = 300.0f
                chartIntervalStep = 30
                chartIntervalStart = 30
                chartIntervalEnd = 270
                chartLabelYCount = 11
                chartIsShowMinTextView = false
                chartLabelUnit = "(μg/m³)"
            }
        }
    }

    // 20171128 Added by Raymond
    private fun configChartView() {
        val xAxis: XAxis = chart_line.xAxis
        val leftAxis: YAxis = chart_line.axisLeft
        val rightAxis: YAxis = chart_line.axisRight

        chart_line.isScaleXEnabled = false
        chart_line.isScaleYEnabled = false
        leftAxis.setLabelCount(chartLabelYCount, true)
        leftAxis.setAxisMaxValue(chartMax) // the axis maximum is 1500
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
        outState?.putInt("useFor", useFor)
        outState?.putInt("chartIntervalStep", chartIntervalStep)
        outState?.putFloat("chartMin", chartMin)
        outState?.putFloat("chartMax", chartMax)
        outState?.putInt("chartIntervalStart", chartIntervalStart)
        outState?.putInt("chartIntervalEnd", chartIntervalEnd)
        outState?.putInt("chartLabelYCount", chartLabelYCount)
        outState?.putBoolean("chartIsShowMinTextView", chartIsShowMinTextView)
        outState?.putString("chartLabelUnit", chartLabelUnit)
        outState?.putString("chartLabel", chartLabel)
        super.onSaveInstanceState(outState!!)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            useFor = savedInstanceState.getInt("useFor")
            chartIntervalStep = savedInstanceState.getInt("chartIntervalStep")
            chartMin = savedInstanceState.getFloat("chartMin")
            chartMax = savedInstanceState.getFloat("chartMax")
            chartIntervalStart = savedInstanceState.getInt("chartIntervalStart")
            chartIntervalEnd = savedInstanceState.getInt("chartIntervalEnd")
            chartLabelYCount = savedInstanceState.getInt("chartLabelYCount")
            chartIsShowMinTextView = savedInstanceState.getBoolean("chartIsShowMinTextView")
            chartLabelUnit = savedInstanceState.getString("chartLabelUnit")
            chartLabel = savedInstanceState.getString("chartLabel")

        } else {
            // Probably initialize members with default values for a new instance
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
                    if (i % 60 == 0) {
                        textView.text = ""
                    } else {
                        textView.text = (chartMin + (j) * chartIntervalStep).toInt().toString()
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
                    DEFINE_FRAGMENT_PM10 -> {
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
            DEFINE_FRAGMENT_PM10 -> {
                ChartLabel.text = getString(R.string.text_label_pm10)
                faceBar.setImageResource(R.drawable.face_bar_pm10)
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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater!!.inflate(R.layout.frg_chart, container, false)


    private fun changeBackground(input: Int) {
        when (useFor) {
            DEFINE_FRAGMENT_PM10 -> {
                when (input) {
                    in 0..53 -> {
                        ChartBackground.setBackgroundResource(R.drawable.app_bg_cloud_green)
                    }
                    in 54..124 -> {
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

    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    override fun onAttach(activity: Activity?) {
        super.onAttach(activity!!)
        mContext = this.context!!.applicationContext
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(mContext!!).registerReceiver(mGattUpdateReceiver, makeMainFragmentUpdateIntentFilter())
        //pullData(spinnerPositon)
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
                pullData(position)
                chart_line?.data = getBarData3(TvocNoseData.arrPm10Day, TvocNoseData.arrTimeDay, position)
                chart_line?.data?.setDrawValues(false)
                chart_line?.setVisibleXRange(14.0f, 14.0f)
                chart_line?.centerViewToAnimated(l.toFloat() / 5, 0F, YAxis.AxisDependency.LEFT, 1000)
                val y = chart_line?.data!!.dataSetCount
                chart_line?.highlightValue(l, y - 1)
            }
            1 -> {
                pullData(position)
                chart_line.data = getBarData3(TvocNoseData.arrPm10Week, TvocNoseData.arrTimeWeek, position)
                chart_line.data?.setDrawValues(false)
                chart_line.animateY(3000, Easing.EasingOption.EaseOutBack)
                chart_line.setVisibleXRange(7.0f, 7.0f)
                chart_line.centerViewToAnimated(Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toFloat(),
                        0F, YAxis.AxisDependency.LEFT, 1000)
            }
            2 -> {
                pullData(position)
                chart_line.data = getBarData3(TvocNoseData.arrPm10Month, TvocNoseData.arrTimeMonth, position)
                chart_line.data?.setDrawValues(false)
                chart_line.animateY(3000, Easing.EasingOption.EaseOutBack)
                chart_line.setVisibleXRange(14.0f, 14.0f)
            }
            3 -> {
                pullData(position)
                chart_line.data = getBarData3(TvocNoseData.arrPm10Year, TvocNoseData.arrTimeYear, position)
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
        calObject.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    }

    override fun onResume() {
        super.onResume()
        //btnTextChanged(spinnerPositon)
        //drawChart(spinnerPositon)
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


    private fun setProgressBarMax(input: Int) {
        chartDataLoading.progress = 0
        chartDataLoading.max = input
    }

    private fun setProgressBarNow(input: Int) {
        chartDataLoading.progress = input
        //chartDataLoading.setProgress(input, true)
    }

    private fun setProgressBarZero() {
        chartDataLoading.progress = 0
    }

    private fun cleanTextViewInTVOC() {
        ChartSelectDetectionValue.text = ""
        ChartSelectDetectionTime.text = ""
    }

    private fun getBarData3(inputValue: ArrayList<String>, inputTime: ArrayList<String>, positionID: Int?): BarData {
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
                for (i in 0 until input.size) {
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
            2 -> {
                val dateFormat = SimpleDateFormat("MM/dd")
                val dateLabelFormat = SimpleDateFormat("yyyy/MM/dd")
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
        for (i in 0 until input.size) {
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
                    mConnectStatus = false
                }
                BroadcastActions.ACTION_GATT_CONNECTED -> {
                    mConnectStatus = true
                }
                BroadcastActions.ACTION_GET_HISTORY_COUNT -> {
                    val bundle = intent.extras
                    val totalData = bundle.getString(BroadcastActions.INTENT_KEY_GET_HISTORY_COUNT)
                    setProgressBarMax(totalData.toInt())
                }
                BroadcastActions.ACTION_LOADING_DATA -> {
                    val bundle = intent.extras
                    val nowData = bundle.getString(BroadcastActions.INTENT_KEY_LOADING_DATA)
                    setProgressBarNow(nowData.toInt())
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
                chart_line.centerViewToAnimated(p.toFloat() / 5, 0F, YAxis.AxisDependency.LEFT, 1000)
                //chart_line.moveViewToX((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                //        + Calendar.getInstance().get(Calendar.MINUTE) / 60F) * 118.5F) //移動視圖by x index
                val y = chart_line.data!!.dataSetCount
                chart_line.highlightValue(p, y - 1)
            }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), true)
            tpd.setMessage("請選擇時間")
            tpd.show()
        }
    }

    private fun getRealmDay() {
        TvocNoseData.arrTvocDay.clear()
        TvocNoseData.arrEco2Day.clear()
        TvocNoseData.arrTempDay.clear()
        TvocNoseData.arrHumiDay.clear()
        TvocNoseData.arrPm25Day.clear()
        TvocNoseData.arrPm10Day.clear()

        TvocNoseData.arrTimeDay.clear()
        val pm10Realm = Realm.getDefaultInstance()
        //val touchTime = if (calObject.get(Calendar.HOUR_OF_DAY) >= 8) calObject.timeInMillis else calObject.timeInMillis + calObject.timeZone.rawOffset //舊方法
        //val startTime = touchTime / (3600000 * 24) * (3600000 * 24) - calObject.timeZone.rawOffset //舊方法
        val getDayCal = getCalendarInstanceForDB()
        val startTime = getDayCal.timeInMillis
        //將日期設為今天日子加一天減1秒
        val endTime = startTime + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
        val query = pm10Realm.where(AsmDataModel::class.java)
        //一天共有1440筆
        val dataCount = (endTime - startTime) / (60 * 1000)
        Log.d("TimePeriod", (dataCount.toString() + "Count"))
        query.between("Created_time", startTime, endTime).sort("Created_time", Sort.ASCENDING)
        val pm10Result = query.findAll()
        var avgPM10Today = 0
        var avgPM10Yesterday = 0

        //先生出1440筆值為0的陣列
        for (y in 0..dataCount step 5) {
            TvocNoseData.arrTvocDay.add("65538")
            TvocNoseData.arrEco2Day.add("65538")
            TvocNoseData.arrTempDay.add("65538")
            TvocNoseData.arrHumiDay.add("65538")
            TvocNoseData.arrPm25Day.add("65538")
            TvocNoseData.arrPm10Day.add("65538")
            TvocNoseData.arrTimeDay.add((startTime + y * 60 * 1000).toString())
        }

        if (pm10Result.size != 0) {
            //關鍵!!利用取出的資料減掉抬頭時間除以30秒算出index換掉TVOC的值
            //pm10Realm.addChangeListener(RealmChangeListener {
            pm10Result?.forEach { asmDataModel ->
                if (useFor == DEFINE_FRAGMENT_PM10) { //PM25
                    val count = ((asmDataModel.created_time - startTime) / (60 * 1000 * 5)).toInt()
                    TvocNoseData.arrPm25Day[count] = asmDataModel.pM25Value.toString()
                    TvocNoseData.arrPm10Day[count] = if (asmDataModel.pM10Value != null) asmDataModel.pM10Value.toString() else "0"
                } else {
                    val count = ((asmDataModel.created_time - startTime) / (60 * 1000)).toInt()
                    TvocNoseData.arrTvocDay[count] = asmDataModel.tvocValue.toString()
                    TvocNoseData.arrEco2Day[count] = asmDataModel.ecO2Value.toString()
                    TvocNoseData.arrTempDay[count] = (asmDataModel.tempValue.toFloat() + 10f).toString()
                    TvocNoseData.arrHumiDay[count] = asmDataModel.humiValue.toString()
                }
            }
            //})
        }
        avgPM10Today = pm10Result.average("PM10Value").toInt()
        avgPM10Yesterday = pm10Realm.where(AsmDataModel::class.java)
                .between("Created_time", startTime - TimeUnit.DAYS.toMillis(1), endTime - TimeUnit.DAYS.toMillis(1))
                .findAll()
                .average("PM10Value")
                .toInt()
        showAvg(startTime, avgPM10Today, avgPM10Yesterday)
        pm10Realm.close()
    }

    private fun getRealmWeek() {
        TvocNoseData.arrTvocWeek.clear()
        TvocNoseData.arrEco2Week.clear()
        TvocNoseData.arrTempWeek.clear()
        TvocNoseData.arrHumiWeek.clear()
        TvocNoseData.arrPm25Week.clear()
        TvocNoseData.arrPm10Week.clear()
        TvocNoseData.arrTimeWeek.clear()

        val dayOfWeek = calObject.get(Calendar.DAY_OF_WEEK)
        val getWeekCal = getCalendarInstanceForDB()
        val startTime = getWeekCal.timeInMillis
        //將星期幾退回到星期日為第一時間點
        val sqlWeekBase = startTime - TimeUnit.DAYS.toMillis((dayOfWeek - 1).toLong())
        //跑七筆BarChart
        for (y in 0..6) {
            //第一筆為日 00:00
            val sqlStartDate = sqlWeekBase + TimeUnit.DAYS.toMillis(y.toLong())
            //結束點為日 23:59
            val sqlEndDate = sqlStartDate + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
            val realm = Realm.getDefaultInstance()
            val query = realm.where(AsmDataModel::class.java)
            query.between("Created_time", sqlStartDate, sqlEndDate)
            val result1 = query.findAll()
            Log.d("getRealmWeek" + useFor.toString(), result1.size.toString())
            if (result1.size != 0) {
                var sumTvoc = 0
                var sumEco2 = 0
                var sumTemp = 0f
                var sumHumi = 0
                var sumPm25 = 0
                var sumPm10 = 0
                result1.forEach {
                    sumTvoc += it.tvocValue.toInt()
                    sumEco2 += it.ecO2Value.toInt()
                    sumTemp += it.tempValue.toFloat()
                    sumHumi += it.humiValue.toInt()
                    sumPm25 += it.pM25Value.toInt()
                    sumPm10 += if (it.pM10Value != null) it.pM10Value else 0
                }
                TvocNoseData.arrTvocWeek.add((sumTvoc / result1.size).toString())
                TvocNoseData.arrEco2Week.add((sumEco2 / result1.size).toString())
                TvocNoseData.arrTempWeek.add((sumTemp / result1.size).toString())
                TvocNoseData.arrHumiWeek.add((sumHumi / result1.size).toString())
                TvocNoseData.arrPm25Week.add((sumPm25 / result1.size).toString())
                TvocNoseData.arrPm10Week.add((sumPm10 / result1.size).toString())
                //依序加入時間
                TvocNoseData.arrTimeWeek.add((sqlStartDate).toString())
            } else {
                TvocNoseData.arrTvocWeek.add("65538")
                TvocNoseData.arrEco2Week.add("65538")
                TvocNoseData.arrTempWeek.add("65538")
                TvocNoseData.arrHumiWeek.add("65538")
                TvocNoseData.arrPm25Week.add("65538")
                TvocNoseData.arrPm10Week.add("65538")
                //依序加入時間
                TvocNoseData.arrTimeWeek.add((sqlStartDate).toString())
            }
        }
    }

    private fun getRealmMonth() {
        TvocNoseData.arrTvocMonth.clear()
        TvocNoseData.arrEco2Month.clear()
        TvocNoseData.arrTempMonth.clear()
        TvocNoseData.arrHumiMonth.clear()
        TvocNoseData.arrPm25Month.clear()
        TvocNoseData.arrPm10Month.clear()
        TvocNoseData.arrTimeMonth.clear()

        val dayOfMonth = calObject.get(Calendar.DAY_OF_MONTH)
        val monthCount = calObject.getActualMaximum(Calendar.DAY_OF_MONTH)
        //將星期幾退回到星期日為第一時間點
        val getMonthCal = getCalendarInstanceForDB()
        val sqlMonthBase = getMonthCal.timeInMillis - TimeUnit.DAYS.toMillis((dayOfMonth - 1).toLong())
        Log.d("getRealmMonth" + useFor.toString(), sqlMonthBase.toString())
        for (y in 0..(monthCount - 1)) {
            val sqlStartDate = sqlMonthBase + TimeUnit.DAYS.toMillis(y.toLong())
            val sqlEndDate = sqlStartDate + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
            val realm = Realm.getDefaultInstance()
            val query = realm.where(AsmDataModel::class.java)
            query.between("Created_time", sqlStartDate, sqlEndDate)
            val result1 = query.findAll()
            Log.d("getRealmMonth" + useFor.toString(), result1.size.toString())
            if (result1.size != 0) {
                var sumTvoc = 0
                var sumEco2 = 0
                var sumTemp = 0f
                var sumHumi = 0
                var sumPm25 = 0
                var sumPm10 = 0
                result1.forEach {
                    sumTvoc += it.tvocValue.toInt()
                    sumEco2 += it.ecO2Value.toInt()
                    sumTemp += it.tempValue.toFloat()
                    sumHumi += it.humiValue.toInt()
                    sumPm25 += it.pM25Value.toInt()
                    sumPm10 += if (it.pM10Value != null) it.pM10Value else 0
                }
                TvocNoseData.arrTvocMonth.add((sumTvoc / result1.size).toString())
                TvocNoseData.arrEco2Month.add((sumEco2 / result1.size).toString())
                TvocNoseData.arrTempMonth.add((sumTemp / result1.size).toString())
                TvocNoseData.arrHumiMonth.add((sumHumi / result1.size).toString())
                TvocNoseData.arrPm25Month.add((sumPm25 / result1.size).toString())
                TvocNoseData.arrPm10Month.add((sumPm10 / result1.size).toString())
                //依序加入時間
                TvocNoseData.arrTimeMonth.add((sqlStartDate).toString())
            } else {
                TvocNoseData.arrTvocMonth.add("65538")
                TvocNoseData.arrEco2Month.add("65538")
                TvocNoseData.arrTempMonth.add("65538")
                TvocNoseData.arrHumiMonth.add("65538")
                TvocNoseData.arrPm25Month.add("65538")
                TvocNoseData.arrPm10Month.add("65538")
                //依序加入時間
                TvocNoseData.arrTimeMonth.add((sqlStartDate).toString())
            }
        }

    }

    private fun pullData(position: Int?) {
        when (position) {
            0 -> { getRealmDay() }
            1 -> { getRealmWeek() }
            2 -> { getRealmMonth() }
            3 -> { getRealmYear() }
            else -> {}
        }
    }

    private fun showAvg(startDate: Long, avgValueToday: Int, avgValueYesterday: Int) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        show_Today?.text = dateFormat.format(startDate)
        show_Yesterday?.text = dateFormat.format(startDate - TimeUnit.DAYS.toMillis(1))

        result_Today?.text = avgValueToday.toString() + " μg/m³"
        result_Yesterday?.text = avgValueYesterday.toString() + " μg/m³"
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
                0xD6.toByte() -> {
                    if (spinnerPositon == 0) {
                        btnTextChanged(spinnerPositon)
                        drawChart(spinnerPositon)
                    }
                    Log.e("ChartFrg", "Now Starting Load Data.........")
                }
            }
        }
    }

    private fun getRealmYear() {
        TvocNoseData.arrTvocYear.clear()
        TvocNoseData.arrEco2Year.clear()
        TvocNoseData.arrTempYear.clear()
        TvocNoseData.arrHumiYear.clear()
        TvocNoseData.arrPm25Year.clear()
        TvocNoseData.arrPm10Year.clear()
        TvocNoseData.arrTimeYear.clear()

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
            Log.d("getRealmMonth" + useFor.toString(), result1.size.toString())
            if (result1.size != 0) {
                var sumTvoc = 0
                var sumEco2 = 0
                var sumTemp = 0f
                var sumHumi = 0
                var sumPm25 = 0
                var sumPm10 = 0
                result1.forEach {
                    sumTvoc += it.tvocValue.toInt()
                    sumEco2 += it.ecO2Value.toInt()
                    sumTemp += it.tempValue.toFloat()
                    sumHumi += it.humiValue.toInt()
                    sumPm25 += it.pM25Value.toInt()
                    sumPm10 += if (it.pM10Value != null) it.pM10Value else 0
                }
                TvocNoseData.arrTvocYear.add((sumTvoc / result1.size).toString())
                TvocNoseData.arrEco2Year.add((sumEco2 / result1.size).toString())
                TvocNoseData.arrTempYear.add((sumTemp / result1.size).toString())
                TvocNoseData.arrHumiYear.add((sumHumi / result1.size).toString())
                TvocNoseData.arrPm25Year.add((sumPm25 / result1.size).toString())
                TvocNoseData.arrPm10Year.add((sumPm10 / result1.size).toString())
                //依序加入時間
                TvocNoseData.arrTimeYear.add((sqlStartDate).toString())
            } else {
                TvocNoseData.arrTvocYear.add("65538")
                TvocNoseData.arrEco2Year.add("65538")
                TvocNoseData.arrTempYear.add("65538")
                TvocNoseData.arrHumiYear.add("65538")
                TvocNoseData.arrPm25Year.add("65538")
                TvocNoseData.arrPm10Year.add("65538")
                //依序加入時間
                TvocNoseData.arrTimeYear.add((sqlStartDate).toString())
            }
        }

    }

    private fun getCalendarInstanceForDB(): Calendar {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, calObject.get(Calendar.YEAR))
        cal.set(Calendar.MONTH, calObject.get(Calendar.MONTH))
        cal.set(Calendar.DAY_OF_MONTH, calObject.get(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 0) // ! clear would not reset the hour of day ! //這幾行是新寫法，好用
        cal.clear(Calendar.MINUTE) //這幾行是新寫法，好用
        cal.clear(Calendar.SECOND) //這幾行是新寫法，好用
        cal.clear(Calendar.MILLISECOND) //這幾行是新寫法，好用
        return cal
    }
}