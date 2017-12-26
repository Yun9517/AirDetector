package com.microjet.airqi2.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.*
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import io.realm.Realm
import com.microjet.airqi2.AndyAirDBhelper
import com.microjet.airqi2.AsmDataModel
import com.microjet.airqi2.CustomAPI.FixBarChart
//import com.github.mikephil.charting.utils.Highlight
import com.microjet.airqi2.CustomAPI.MyBarDataSet
import com.microjet.airqi2.CustomAPI.Utils.isFastDoubleClick
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.Definition.BroadcastIntents
import com.microjet.airqi2.MainActivity
import com.microjet.airqi2.R
import com.microjet.airqi2.myData
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TVOCFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [TVOCFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TVOCFragment : Fragment()  ,OnChartValueSelectedListener {
    private var mContext: Context? = null

    private var mChart: FixBarChart? = null
    private var mTextViewTimeRange: TextView? = null
    private var mTextViewValue: TextView? = null
    private var DATA_COUNT: Int = 60
    private var mRadioGroup: RadioGroup? = null

    private var mHour: RadioButton? = null



    @Suppress("OverridingDeprecatedMember")
    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        mContext = this.context.applicationContext
    }


    //TestValue Start chungyen
    private val tvocArray = ArrayList<String>()
    private val timeArray = ArrayList<String>()
    private val batteryArray = ArrayList<String>()
    private var mProgressBar: ProgressBar? = null
    private var mImageViewDataUpdate: ImageView? = null
    private var radioButtonID = mRadioGroup?.getCheckedRadioButtonId()

    private var mConnectStatus: Boolean = false

    //試Realm拉資料
    private var arrTime3 = ArrayList<String>()
    private var arrTvoc3 = ArrayList<String>()

    private var animationCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(mContext!!).registerReceiver(mGattUpdateReceiver, makeMainFragmentUpdateIntentFilter())
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater!!.inflate(R.layout.frg_tvoc, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mRadioGroup = this.view?.findViewById(R.id.frg_radioGroup)
        mChart = this.view!!.findViewById(R.id.chart_line)
        mProgressBar = this.view!!.findViewById(R.id.chartDataLoading)
        mHour = this.view!!.findViewById(R.id.radioButton_Hour)
        mImageViewDataUpdate = this.view?.findViewById(R.id.chart_Refresh)
        mImageViewDataUpdate?.background = resources.getDrawable(R.drawable.chart_update_icon_bg)
        mImageViewDataUpdate?.setOnClickListener {
            if (!isFastDoubleClick){
            getDeviceData()
                Log.d("TVOC","TOAST_ON")
            }
        }
        mRadioGroup?.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, i ->
            mChart?.clear()
            when (i) {
                R.id.radioButton_Hour -> {
                    mChart?.data = getBarData2(tvocArray, timeArray)
                }
                R.id.radioButton_Day -> {
                    getRealmFour(4)
                    mChart?.data = getBarData3(arrTvoc3, arrTime3)
                }
                R.id.radioButton_Week -> {

                    getRealmFour(8)
                    mChart?.data = getBarData3(arrTvoc3, arrTime3)
                }
                R.id.radioButton_Month -> {
                    getRealmFour(12)
                    mChart?.data = getBarData3(arrTvoc3, arrTime3)

                }
            }
            mChart?.setVisibleXRangeMinimum(5.0f)
            mChart?.setVisibleXRangeMaximum(5.0f)//需要在设置数据源后生效
            mChart?.moveViewToX((100).toFloat())//移動視圖by x index
        })

        mHour!!.isChecked = true
        configChartView()
        mChart!!.setOnChartValueSelectedListener(this)
    }

    override fun onStart() {
        super.onStart()
        checkUIState()
    }

    override fun onResume() {
        super.onResume()
        //視Radio id畫圖
        when (radioButtonID) {
            0 -> {
                mChart?.data = getBarData2(tvocArray, timeArray)
                mChart?.setVisibleXRangeMinimum(5.0f)
                mChart?.setVisibleXRangeMaximum(5.0f)
            }
            1, 2, 3 -> {
                mChart?.data = getBarData3(arrTvoc3, arrTime3)
                mChart?.setVisibleXRangeMinimum(5.0f)
                mChart?.setVisibleXRangeMaximum(5.0f)
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            LocalBroadcastManager.getInstance(mContext!!).unregisterReceiver(mGattUpdateReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNothingSelected() {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onValueSelected(e: Entry?, dataSetIndex: Int, h: Highlight?) {
        //   TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        mTextViewValue!!.text = h!!.value.toString() + "ppb"
        mTextViewTimeRange!!.text = mChart?.xAxis?.values?.get(h.xIndex)//listString[h.xIndex]
    }

    @Synchronized private fun checkUIState() {
        if (mConnectStatus) {

        } else {
            stopUpdateDataAnimation()
            setProgessBarZero()
        }
        setCurrentConnectStatusIcon()
        if (animationCount > 1440) {
            stopUpdateDataAnimation()
            setProgessBarZero()
        }
    }

    private fun setCurrentConnectStatusIcon() {
        if (mConnectStatus) {
            mImageViewDataUpdate?.setImageResource(R.drawable.chart_update_icon_connect)
            mImageViewDataUpdate?.isEnabled = true
        } else {
            mImageViewDataUpdate?.setImageResource(R.drawable.chart_update_icon_disconnect)
            mImageViewDataUpdate?.isEnabled = false
        }
    }

    private fun getDeviceData() {
        val intent: Intent? = Intent(BroadcastIntents.PRIMARY)
        intent!!.putExtra("status", "getSampleRate")
        context.sendBroadcast(intent)
    }

    private fun setProgessBarMax(input: Int) {
        mProgressBar?.progress = 0
        mProgressBar?.max = input
    }

    private fun setProgessBarNow(input: Int) {
        mProgressBar?.progress = input
    }

    private fun setProgessBarZero() {
        mProgressBar?.progress = 0
    }

    private fun cleanTextViewInTVOC() {
        mTextViewValue?.text = ""
        mTextViewTimeRange?.text = ""
    }

    private fun getBarData2(inputTVOC: ArrayList<String>, inputTime: ArrayList<String>): BarData {
        val dataSetA = MyBarDataSet(getChartData2(inputTVOC), "TVOC")
        dataSetA.setColors(intArrayOf(ContextCompat.getColor(context, R.color.progressBarStartColor),
                ContextCompat.getColor(context, R.color.progressBarMidColor),
                ContextCompat.getColor(context, R.color.progressBarEndColor)))

        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(dataSetA) // add the datasets
        cleanTextViewInTVOC()
        return BarData(getLabels2(inputTime), dataSets)
    }

    private fun getChartData2(input: ArrayList<String>): List<BarEntry> {
        // val DATA_COUNT = 5
        // DATA_COUNT
        val chartData = ArrayList<BarEntry>()
        if (input.size < DATA_COUNT - 1) {
            for (i in 0 until input.size) {
                chartData.add(BarEntry(input[i].toFloat(), i))
            }
        } else {
            for (i in 0 until DATA_COUNT - 1) {
                chartData.add(BarEntry(input[i].toFloat(), i))
            }
        }
        return chartData
    }

    private fun getLabels2(input: ArrayList<String>): List<String> {
        val chartLabels = ArrayList<String>()

        if (input.size < DATA_COUNT - 1) {
            for (i in 0 until input.size) {
                chartLabels.add(input[i])
            }
        } else {
            for (i in 0 until DATA_COUNT - 1) {
                chartLabels.add(input[i])
            }
        }
        return chartLabels
    }

    // 20171128 Added by Raymond
    private fun configChartView() {
        val xAxis: XAxis = mChart!!.xAxis
        val leftAxis: YAxis = mChart!!.axisLeft
        val rightAxis: YAxis = mChart!!.axisRight

        mChart!!.isScaleXEnabled = false
        mChart!!.isScaleYEnabled = false
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        mChart!!.legend.isEnabled = false

        leftAxis.setDrawLabels(false) // no axis labels
        leftAxis.setDrawAxisLine(false) // no axis line
        leftAxis.setDrawGridLines(true) // no grid lines

        leftAxis.setAxisMaxValue(1000f) // the axis maximum is 100
        leftAxis.setAxisMinValue(0f) // start at zero
        rightAxis.isEnabled = false
        mChart?.setDescription("")// clear default string
    }

    private fun getRealmFour(hour: Int) {
        arrTime3.clear()
        arrTvoc3.clear()
        //touchTime = Date().time
        for (y in 1..61) {
            var realm = Realm.getDefaultInstance()
            val query = realm.where(AsmDataModel::class.java)
            //設定時間區間
            var touchTime = Date().time
            var nowHourRemainer = 0
            when (hour) {
            //將sql搜尋時間往後推至區間 4 8 12 16 20 24
                4 -> {
                    nowHourRemainer = 4 - (Date().hours % 4)
                }
                8 -> {
                    nowHourRemainer = 8 - (Date().hours % 8)
                }
                12 -> {
                    nowHourRemainer = 12 - (Date().hours % 12)
                }
            }
            var endTime = ((touchTime / 3600000) + nowHourRemainer - (hour * (y - 1))) * 3600000
            var startTime = endTime - (hour) * 3600000
            Log.d("TimePeriod", ((endTime - startTime) / 3600000).toString() + "Hour")

            query.between("Created_time", startTime, endTime)

            var result1 = query.findAll()
            if (result1.size != 0) {
                var sumTvoc = 0
                for (i in result1) {
                    sumTvoc += i.tvocValue.toInt()
                }
                var aveTvoc = (sumTvoc / result1.size)
                Log.d("getRealmFour", result1.last().toString())
                arrTvoc3.add(aveTvoc.toString())
                arrTime3.add(endTime.toString())
            } else {
                arrTvoc3.add("0")
                arrTime3.add(endTime.toString())
            }

        }


        arrTvoc3.reverse()
        arrTime3.reverse()

    }
    private fun getBarData3(inputTVOC: ArrayList<String>, inputTime: ArrayList<String>): BarData {
        val dataSetA = MyBarDataSet(getChartData3(inputTVOC), "TVOC")
        dataSetA.setColors(intArrayOf(ContextCompat.getColor(context, R.color.progressBarStartColor),
                ContextCompat.getColor(context, R.color.progressBarMidColor),
                ContextCompat.getColor(context, R.color.progressBarEndColor)))

        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(dataSetA) // add the datasets
        cleanTextViewInTVOC()
        return BarData(getLabels3(inputTime), dataSets)
    }

    private fun getLabels3(input: ArrayList<String>): List<String> {


        val chartLabels = ArrayList<String>()
        val dateFormat = SimpleDateFormat("MM/dd HH:mm")
        for (i in 0 until arrTime3.size) {
            val date = dateFormat.format(input[i].toLong())
            chartLabels.add(date)
        }
        chartLabels.removeAt(arrTime3.size - 1)
        chartLabels.add(arrTime3.size - 1,dateFormat.format(Date().time))
        Log.d("TVOCGETLABEL3", chartLabels.lastIndex.toString())
        return chartLabels
    }

    private fun getChartData3(input: ArrayList<String>): List<BarEntry> {


        val chartData = ArrayList<BarEntry>()
        for (i in 0 until arrTime3.size) {
            chartData.add(BarEntry(input[i].toFloat(), i))
        }
        return chartData
    }

    private fun startUpdateDataAnimation() {
        val operatingAnim: Animation = AnimationUtils.loadAnimation(mContext, R.anim.tip)
        val lin = LinearInterpolator()
        operatingAnim.interpolator = lin
        mImageViewDataUpdate?.startAnimation(operatingAnim)
        mImageViewDataUpdate?.isEnabled = false
        startDataAnimationCount()
    }

    private fun stopUpdateDataAnimation() {
        mImageViewDataUpdate?.clearAnimation()
        mImageViewDataUpdate?.isEnabled = true
    }

    private fun startDataAnimationCount() {
        animationCount = 0
    }

    private fun setRealTimeBarData(Tvoc: String, Battery: String) {
        val sdFormat = SimpleDateFormat("MM/dd HH:mm:ss", Locale.TAIWAN)
        val date = Date()
        sdFormat.format(date)
        timeArray.add(sdFormat.format(date))
        tvocArray.add(Tvoc)
        batteryArray.add(Battery)

        val radioButtonIDBar = mRadioGroup?.getCheckedRadioButtonId()

        if (radioButtonIDBar == R.id.radioButton_Hour) {
            mChart?.clear()
            if (tvocArray.size > DATA_COUNT) {
                tvocArray.removeAt(0)
            }
            if (timeArray.size > DATA_COUNT) {
                timeArray.removeAt(0)
            }
            mChart?.data = getBarData2(tvocArray, timeArray)
            mChart?.setVisibleXRangeMinimum(5.0f)
            mChart?.setVisibleXRangeMaximum(5.0f)//需要在设置数据源后生效
            mChart?.moveViewToX(tvocArray.size.toFloat())//移動視圖by x index
        }
    }

    private fun makeMainFragmentUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BroadcastActions.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BroadcastActions.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BroadcastActions.ACTION_GET_NEW_DATA)
        intentFilter.addAction(BroadcastActions.ACTION_GET_HISTORY_COUNT)
        intentFilter.addAction(BroadcastActions.ACTION_LOADING_DATA)
        return intentFilter
    }

    var counter = 0
    var TVOCAVG = 0
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


                }
                BroadcastActions.ACTION_GET_HISTORY_COUNT ->{
                    val bundle = intent.extras
                    val totalData = bundle.getString(BroadcastActions.INTENT_KEY_GET_HISTORY_COUNT)
                    if (totalData.toInt() != 0) {
                        setProgessBarMax(totalData.toInt())
                        startUpdateDataAnimation()
                    }
                    Toast.makeText(context,"共有資料"+ totalData + "筆",Toast.LENGTH_LONG).show()
                }
                BroadcastActions.ACTION_LOADING_DATA -> {
                    val bundle = intent.extras
                    val nowData = bundle.getString(BroadcastActions.INTENT_KEY_LOADING_DATA)
                    setProgessBarNow(nowData.toInt())
                    if(nowData.toInt() == mProgressBar?.max) {
                        stopUpdateDataAnimation()
                        mRadioGroup?.check(R.id.radioButton_Hour)
                    }
                }
                BroadcastActions.ACTION_GET_NEW_DATA -> {
                    val bundle = intent.extras
                    val tempVal = bundle.getString(BroadcastActions.INTENT_KEY_TEMP_VALUE)
                    val humiVal = bundle.getString(BroadcastActions.INTENT_KEY_HUMI_VALUE)
                    val tvocVal = bundle.getString(BroadcastActions.INTENT_KEY_TVOC_VALUE)
                    val co2Val = bundle.getString(BroadcastActions.INTENT_KEY_CO2_VALUE)
                    val BatteryLife = bundle.getString(BroadcastActions.INTENT_KEY_BATTERY_LIFE)
                    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                    val date = Date()

                    //新增AnimationCount
                    animationCount++

                    counter++
                    TVOCAVG += tvocVal.toInt()
                    if (counter % 15 == 0) {
                        counter = 0
                        TVOCAVG /= 15
                        setRealTimeBarData(TVOCAVG.toString(), BatteryLife)
                        TVOCAVG = 0
                    }
                }
            }
            checkUIState()
        }
    }




    private fun getBarData(): BarData {
        val dataSetA = MyBarDataSet(getChartData(), "TVOC")
        dataSetA.setColors(intArrayOf(ContextCompat.getColor(context, R.color.progressBarStartColor),
                ContextCompat.getColor(context, R.color.progressBarMidColor),
                ContextCompat.getColor(context, R.color.progressBarEndColor)))

        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(dataSetA) // add the datasets

        return BarData(getLabels(), dataSets)
    }

    private fun getChartData(): List<BarEntry> {
        // val DATA_COUNT = 5
        // DATA_COUNT
        val chartData = ArrayList<BarEntry>()
        for (i in 1 until DATA_COUNT) {
            chartData.add(BarEntry((i * 20).toFloat(), i))
        }
        return chartData
    }

    private fun getLabels(): List<String> {
        val chartLabels = ArrayList<String>()
        for (i in 1 until DATA_COUNT) {
            chartLabels.add("X" + i)
        }
        return chartLabels
    }

    private fun nothing() {
        var realm = Realm.getDefaultInstance()
        val query = realm.where(AsmDataModel::class.java)
        for (y in 10..1) {
            var countTime = Date().time - 60 * 60 * 1000 * (y)
            query.lessThan("Created_time", countTime)//.greaterThan("Created_time",countTime)
            val result1 = query.findAll()
            var sumTvoc = 0
            var sumTime: Long = 0
            for (i in result1) {
                sumTvoc += i.tvocValue.toInt()
                sumTime += i.created_time.toLong()
                if (result1.size != 0) {
                    arrTvoc3.add((sumTvoc / result1.size).toString())
                }
            }
        }

    }
}


