package com.microjet.airqi2.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.*
import android.os.Bundle
import android.os.Handler
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
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import io.realm.Realm
import com.microjet.airqi2.AsmDataModel
import com.microjet.airqi2.CustomAPI.FixBarChart
//import com.github.mikephil.charting.utils.Highlight
import com.microjet.airqi2.CustomAPI.MyBarDataSet
import com.microjet.airqi2.CustomAPI.Utils.isFastDoubleClick
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.Definition.BroadcastIntents
import com.microjet.airqi2.R
import io.realm.Sort
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


@Suppress("DEPRECATION")
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

    private var mDataCount: Int = 60

    //UI元件
    private var mChart: FixBarChart? = null
    private var mTextViewTimeRange: TextView? = null
    private var mTextViewValue: TextView? = null
    private var mRadioGroup: RadioGroup? = null
    private var mHour: RadioButton? = null
    private var mProgressBar: ProgressBar? = null
    private var mImageViewDataUpdate: ImageView? = null
    private var mImageViewFace: ImageView? = null
    private var tvCharLabel: TextView? = null
    private var tvChartTitleTop : TextView? = null
    private var tvChartTitleMiddle : TextView? = null
    private var tvChartTitleBottom : TextView? = null
    private var imgBarRed : ImageView? = null
    private var imgBarYellow : ImageView? = null
    private var imgBarGreen : ImageView? = null
    private var imgBarBase : ImageView? = null
    private var sprTVOC : Spinner? = null
    private var btnCallDatePicker : Button? = null
    //UI元件


    //TestValue Start chungyen
    private val tvocArray = ArrayList<String>()
    private val timeArray = ArrayList<String>()
    private val batteryArray = ArrayList<String>()
    private var radioButtonID : Int? = 0

    private var mConnectStatus: Boolean = false

    //試Realm拉資料
    private var arrTime3 = ArrayList<String>()
    private var arrTvoc3 = ArrayList<String>()

    private var animationCount = 0
    private var downloadingData = false

    private var preHeat = "0"
    private var getDataCycle = 15

    private var calObject = Calendar.getInstance()
    private var spinnerPositon = 0
    private var datepickerHandler = Handler()
    private var chartHandler = Handler()
    private var downloadComplete = false

    var counter = 0
    var TVOCAVG = 0

    @Suppress("OverridingDeprecatedMember")
    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        mContext = this.context.applicationContext
    }

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
        mTextViewTimeRange = this.view!!.findViewById(R.id.tvSelectDetectionTime)
        mTextViewValue = this.view?.findViewById(R.id.tvSelectDetectionValue)
        tvCharLabel = this.view?.findViewById(R.id.tvChartLabel)
        tvChartTitleTop = this.view?.findViewById(R.id.tvChartTitleTop)
        tvChartTitleMiddle = this.view?.findViewById(R.id.tvChartTitleMiddle)
        tvChartTitleBottom = this.view?.findViewById(R.id.tvChartTitleBottom)
        imgBarRed = this.view?.findViewById(R.id.imgBarRed)
        imgBarYellow = this.view?.findViewById(R.id.imgBarYellow)
        imgBarGreen = this.view?.findViewById(R.id.imgBarGreen)
        imgBarBase = this.view?.findViewById(R.id.imgBarBase)
        //修改上排Spinner及Button
        sprTVOC = this.view?.findViewById(R.id.sprTVOC)
        val cycleList = ArrayAdapter.createFromResource(context,R.array.SpinnerArray,android.R.layout.simple_spinner_dropdown_item)
        sprTVOC!!.adapter = cycleList
        sprTVOC!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long)
            {
                spinnerPositon = position
                btnTextChanged(spinnerPositon)
                drawChart(spinnerPositon)

                val selectedItem = parent.getItemAtPosition(position).toString()
//                if (selectedItem == "Add new category") {
//                    // do your stuff
//                }
                Log.d("TVOC",selectedItem)
            } // to close the onItemSelected

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        btnCallDatePicker = this.view?.findViewById(R.id.btnCallDatePicker)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        btnCallDatePicker?.text = dateFormat.format(calObject.time)
        btnCallDatePicker?.setOnClickListener {
            datepickerHandler.post {
                val dpd = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    calObject.set(year,month,dayOfMonth)
                    Log.d("TVOCbtncall",calObject.get(Calendar.DAY_OF_MONTH).toString())
                    btnTextChanged(spinnerPositon)
                    drawChart(spinnerPositon)
                },calObject.get(Calendar.YEAR),calObject.get(Calendar.MONTH),calObject.get(Calendar.DAY_OF_MONTH))
                dpd.setMessage("請選擇日期")
                dpd.show()
            }
        }

        mImageViewDataUpdate = this.view?.findViewById(R.id.chart_Refresh)
        mImageViewDataUpdate?.visibility = View.INVISIBLE
        mImageViewDataUpdate?.background = resources.getDrawable(R.drawable.chart_update_icon_bg)
        mImageViewDataUpdate?.setOnClickListener {
            if (!isFastDoubleClick){
                getDeviceData()
                Log.d("TVOC","TOAST_ON")
            }
        }
//        mRadioGroup?.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, i ->
//            mChart?.clear()
//            when (i) {
//                R.id.radioButton_Hour -> {
//                    mChart?.data = getBarData2(tvocArray, timeArray)
//                    mChart?.data?.setDrawValues(false)
//                    mChart?.setVisibleXRangeMinimum(5.0f)
//                    mChart?.setVisibleXRangeMaximum(5.0f)//需要在设置数据源后生效
//                    mChart?.moveViewToX((100).toFloat())//移動視圖by x index
//                }
//                R.id.radioButton_Day -> {
//                    getRealmDay()
//                    mChart?.data = getBarData3(arrTvoc3, arrTime3,i)
//                    mChart?.data?.setDrawValues(false)
//                    mChart?.setVisibleXRange(5.0f,40.0f)
//                    //mChart?.setVisibleXRangeMinimum(20.0f)
//                    //mChart?.setVisibleXRangeMaximum(20.0f)//需要在设置数据源后生效
//                    mChart?.moveViewToX((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
//                            + Calendar.getInstance().get(Calendar.MINUTE) / 60F) * 119F) //移動視圖by x index
//                }
//                R.id.radioButton_Week -> {
//                    getRealmWeek()
//                    mChart?.data = getBarData3(arrTvoc3, arrTime3,i)
//                    mChart?.data?.setDrawValues(false)
//                    mChart?.setVisibleXRange(7.0f,7.0f)
//                }
//                R.id.radioButton_Month -> {
//                    getRealmMonth()
//                    mChart?.data = getBarData3(arrTvoc3, arrTime3,i)
//                    mChart?.data?.setDrawValues(false)
//                    mChart?.setVisibleXRange(35.0f,35.0f)
//
//                }
//            }
//            radioButtonID = mRadioGroup?.checkedRadioButtonId
//            //mChart?.setVisibleXRangeMinimum(5.0f)
//            //mChart?.setVisibleXRangeMaximum(5.0f)//需要在设置数据源后生效
//            //mChart?.moveViewToX((100).toFloat())//移動視圖by x index
//        })

        //mHour!!.isChecked = true
        //radioButtonID = mRadioGroup?.checkedRadioButtonId
        configChartView()
        mChart!!.setOnChartValueSelectedListener(this)
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun btnTextChanged(position: Int?) {
        when(position) {
            0 -> {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                btnCallDatePicker?.text = dateFormat.format(calObject.time)
            }
            1 -> {
                btnCallDatePicker?.text = calObject.get(Calendar.YEAR).toString() + "第" + calObject.get(Calendar.WEEK_OF_YEAR).toString() + "週"
            }
            2 -> {
                val dateFormat = SimpleDateFormat("yyyy-MM")
                btnCallDatePicker?.text = dateFormat.format(calObject.time)
            }
        }

    }

    private fun drawChart(position: Int?) {
        setImageBarSize()
        when (position) {
            0 -> {
                    getRealmDay()
                    mChart?.data = getBarData3(arrTvoc3, arrTime3, position)
                    mChart?.data?.setDrawValues(false)
                    mChart?.setVisibleXRange(5.0f, 40.0f)
                    //mChart?.setVisibleXRangeMinimum(20.0f)
                    //mChart?.setVisibleXRangeMaximum(20.0f)//需要在设置数据源后生效
                    //mChart?.centerViewToAnimated((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    //        + Calendar.getInstance().get(Calendar.MINUTE) / 60F) * 120F,0F, YAxis.AxisDependency.LEFT,1000)
                    mChart?.moveViewToX((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                            + Calendar.getInstance().get(Calendar.MINUTE) / 60F) * 118.5F) //移動視圖by x index
            }
            1 -> {
                    getRealmWeek()
                    mChart?.data = getBarData3(arrTvoc3, arrTime3, position)
                    mChart?.data?.setDrawValues(false)
                    mChart?.animateY(3000, Easing.EasingOption.EaseOutBack)
                    mChart?.setVisibleXRange(7.0f, 7.0f)
            }
            2 -> {
                    getRealmMonth()
                    mChart?.data = getBarData3(arrTvoc3, arrTime3, position)
                    mChart?.data?.setDrawValues(false)
                    mChart?.animateY(3000, Easing.EasingOption.EaseOutBack)
                    mChart?.setVisibleXRange(35.0f, 35.0f)

            }
        }
    }
    private fun setImageBarSize(){
        mChart!!.data = getBarData()
        val line220 = mChart!!.getBarBounds(BarEntry(220f, 1))
        val line660 = mChart!!.getBarBounds(BarEntry(660f, 2))
        val line1000 = mChart!!.getBarBounds(BarEntry(65535f, 3))
        tvChartTitleMiddle?.y = line660.top - (tvChartTitleMiddle!!.height / 2)//Text660 position
        tvChartTitleBottom?.y = line220.top - (tvChartTitleBottom!!.height / 2)//Text220 position
        imgBarRed?.y = line1000.top//red
        imgBarYellow?.y = line660.top//yellow
        imgBarGreen?.y = line220.top//green
        imgBarBase?.y = line220.bottom//base

        //視Radio id畫圖
        mChart!!.clear()
//        when (radioButtonID) {
//            0 -> {
//                mChart?.data = getBarData2(tvocArray, timeArray)
//                mChart?.data?.setDrawValues(false)
//                mChart?.setVisibleXRangeMinimum(5.0f)
//                mChart?.setVisibleXRangeMaximum(5.0f)
//            }
//            1, 2, 3 -> {
//                mChart?.data = getBarData3(arrTvoc3, arrTime3)
//                mChart?.data?.setDrawValues(false)
//                mChart?.setVisibleXRangeMinimum(5.0f)
//                mChart?.setVisibleXRangeMaximum(5.0f)
//            }
//        }
        //dependRadioIDDrawChart(radioButtonID)
        //chartHandler.post { drawChart(spinnerPositon) }
    }
    override fun onStart() {
        super.onStart()
        checkUIState()
    }

    override fun onResume() {
        super.onResume()
        //視Radio id畫圖
        //dependRadioIDDrawChart(radioButtonID)
        drawChart(spinnerPositon)
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

    @SuppressLint("SetTextI18n")
    override fun onValueSelected(e: Entry?, dataSetIndex: Int, h: Highlight?) {
        //   TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        mTextViewTimeRange!!.text = mChart?.xAxis?.values?.get(h!!.xIndex)//listString[h.xIndex]
        mTextViewValue!!.text = h!!.value.toString() + "ppb"
    }

    @Synchronized private fun checkUIState() {
        if (mConnectStatus) {
            mImageViewDataUpdate?.setImageResource(R.drawable.chart_update_icon_connect)
            mImageViewDataUpdate?.isEnabled = true
            if (animationCount > 1440) {
                downloadingData = false
                //stopUpdateDataAnimation()
                setProgressBarZero()
            }
        } else {
            downloadingData = false
            //stopUpdateDataAnimation()
            setProgressBarZero()
            mImageViewDataUpdate?.setImageResource(R.drawable.chart_update_icon_disconnect)
            mImageViewDataUpdate?.isEnabled = false
        }
    }

//    private fun setCurrentConnectStatusIcon() {
//        if (mConnectStatus) {
//            mImageViewDataUpdate?.setImageResource(R.drawable.chart_update_icon_connect)
//            mImageViewDataUpdate?.isEnabled = true
//        } else {
//            mImageViewDataUpdate?.setImageResource(R.drawable.chart_update_icon_disconnect)
//            mImageViewDataUpdate?.isEnabled = false
//        }
//    }

    private fun getDeviceData() {
        if (mConnectStatus && !downloadingData) {
            val intent: Intent? = Intent(BroadcastIntents.PRIMARY)
            intent!!.putExtra("status", BroadcastActions.ACTION_GET_SAMPLE_RATE)
            context.sendBroadcast(intent)
            Log.d("TVOC","getDeviceData")
        }
    }

    private fun setProgressBarMax(input: Int) {
        mProgressBar?.progress = 0
        mProgressBar?.max = input
    }

    private fun setProgressBarNow(input: Int) {
        mProgressBar?.progress = input
    }

    private fun setProgressBarZero() {
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
        // val mDataCount = 5
        // mDataCount
        val chartData = ArrayList<BarEntry>()
        if (input.size < mDataCount - 1) {
            for (i in 0 until input.size) {
                chartData.add(BarEntry(input[i].toFloat(), i))
            }
        } else {
            for (i in 0 until mDataCount - 1) {
                chartData.add(BarEntry(input[i].toFloat(), i))
            }
        }
        return chartData
    }

    private fun getLabels2(input: ArrayList<String>): List<String> {
        val chartLabels = ArrayList<String>()

        if (input.size < mDataCount - 1) {
            for (i in 0 until input.size) {
                chartLabels.add(input[i])
            }
        } else {
            for (i in 0 until mDataCount - 1) {
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

        leftAxis.setAxisMaxValue(65535f) // the axis maximum is 100
        leftAxis.setAxisMinValue(0f) // start at zero
        leftAxis.setDrawLabels(false) // no axis labels
        leftAxis.setDrawAxisLine(false) // no axis line
        leftAxis.setDrawGridLines(true) // no grid lines

        xAxis.position = XAxis.XAxisPosition.BOTTOM
        val nums = ArrayList<Float>()
        nums.add(220f)
        nums.add(660f)

        mChart!!.legend.isEnabled = false
        mChart!!.yChartInterval=nums

     //   leftAxis.setDrawValues(false)
    //    leftAxis.setDraw
        mChart!!.setDrawValueAboveBar(false)
        /*var yLimitLine =  LimitLine(220f,"yLimit 测试");
        yLimitLine.setLineColor(Color.RED)
        yLimitLine.setTextColor(Color.RED)
        leftAxis.addLimitLine(yLimitLine)

        var yLimitLine2 =  LimitLine(660f,"yLimit 测试");
        yLimitLine2.setLineColor(Color.RED)
        yLimitLine2.setTextColor(Color.RED)
        leftAxis.addLimitLine(yLimitLine2)*/


        //var top=leftAxis.spaceTop
        //var bottom=leftAxis.spaceBottom
    //    mChart?.setDrawValueAboveBar(false)
        rightAxis.isEnabled = false
        mChart?.setDescription("")// clear default string
    }

    private fun getRealmDay() {
        arrTime3.clear()
        arrTvoc3.clear()
        //現在時間實體毫秒
        //var touchTime = Calendar.getInstance().timeInMillis
        val touchTime = calObject.timeInMillis
        Log.d("TVOCbtncallRealm",calObject.get(Calendar.DAY_OF_MONTH).toString())
        //將日期設為今天日子加一天減1秒
        val endDay = touchTime / (3600000 * 24) * (3600000 * 24) - calObject.timeZone.rawOffset
        val endDayLast = endDay + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
        val realm = Realm.getDefaultInstance()
        val query = realm.where(AsmDataModel::class.java)
        //設定時間區間
        val endTime = endDayLast
        val startTime = endDay
        //一天共有2880筆
        val dataCount = (endTime - startTime) / (30 * 1000)
        Log.d("TimePeriod", (dataCount.toString() + "thirtySecondsCount"))
        query.between("Created_time", startTime, endTime).sort("Created_time", Sort.ASCENDING)
        val result1 = query.findAll()
        Log.d("getRealmDay", result1.size.toString())
        //先生出2880筆值為0的陣列
        for (y in 0..dataCount) {
            arrTvoc3.add("0")
            arrTime3.add((startTime + y * 30000).toString())
        }
        //關鍵!!利用取出的資料減掉抬頭時間除以30秒算出index換掉TVOC的值
        if (result1.size != 0) {
            result1.forEachIndexed { index, asmDataModel ->
                var count = ((asmDataModel.created_time - startTime) / (30 * 1000)).toInt()
                arrTvoc3[count] = asmDataModel.tvocValue.toString()
            }
            Log.d("getRealmDay", result1.last().toString())
        }
    }

    private fun getRealmWeek() {
        arrTime3.clear()
        arrTvoc3.clear()
        //拿到現在是星期幾的Int
        val dayOfWeek = calObject.get(Calendar.DAY_OF_WEEK)
        val touchTime = calObject.timeInMillis
        val nowDateMills = touchTime / (3600000 * 24) * (3600000 * 24) - calObject.timeZone.rawOffset
        //將星期幾退回到星期日為第一時間點
        val sqlWeekBase = nowDateMills - TimeUnit.DAYS.toMillis((dayOfWeek - 1).toLong())
        Log.d("getRealmWeek", sqlWeekBase.toString())
        //跑七筆BarChart
        for (y in 0..6) {
            //第一筆為日 00:00
            val sqlStartDate = sqlWeekBase + TimeUnit.DAYS.toMillis(y.toLong())
            //結束點為日 23:59
            val sqlEndDate = sqlStartDate + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
            val realm = Realm.getDefaultInstance()
            val query = realm.where(AsmDataModel::class.java)
            Log.d("getRealmWeek", sqlStartDate.toString())
            Log.d("getRealmWeek", sqlEndDate.toString())
            query.between("Created_time", sqlStartDate, sqlEndDate)
            val result1 = query.findAll()
            Log.d("getRealmWeek", result1.size.toString())
            if (result1.size != 0) {
                var sumTvoc = 0
                for (i in result1) {
                    sumTvoc += i.tvocValue.toInt()
                }
                val aveTvoc = (sumTvoc / result1.size)
                arrTvoc3.add(aveTvoc.toString())
                //依序加入時間
                arrTime3.add(sqlStartDate.toString())
                Log.d("getRealmWeek", result1.last().toString())
            } else {
                arrTvoc3.add("0")
                arrTime3.add((sqlStartDate.toString()))
            }
        }
    }
    private fun getRealmMonth() {
        arrTime3.clear()
        arrTvoc3.clear()
        //拿到現在是星期幾的Int
        val dayOfMonth = calObject.get(Calendar.DAY_OF_MONTH)
        val monthCount = calObject.getActualMaximum(Calendar.DAY_OF_MONTH)
        val touchTime = calObject.timeInMillis
        val nowDateMills = touchTime / (3600000 * 24) * (3600000 * 24) - calObject.timeZone.rawOffset
        //將星期幾退回到星期日為第一時間點
        val sqlMonthBase = nowDateMills - TimeUnit.DAYS.toMillis((dayOfMonth - 1).toLong())
        Log.d("getRealmMonth", sqlMonthBase.toString())
        //跑七筆BarChart
        for (y in 0..(monthCount-1)) {
            //第一筆為日 00:00
            val sqlStartDate = sqlMonthBase + TimeUnit.DAYS.toMillis(y.toLong())
            //結束點為日 23:59
            val sqlEndDate = sqlStartDate + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
            val realm = Realm.getDefaultInstance()
            val query = realm.where(AsmDataModel::class.java)
            val dataCount = (sqlEndDate - sqlStartDate) / (30 * 1000)
            Log.d("TimePeriod", (dataCount.toString() + "thirtySecondsCount"))
            Log.d("getRealmMonth", sqlStartDate.toString())
            Log.d("getRealmMonth", sqlEndDate.toString())
            query.between("Created_time", sqlStartDate, sqlEndDate)
            val result1 = query.findAll()
            Log.d("getRealmMonth", result1.size.toString())
            if (result1.size != 0) {
                var sumTvoc = 0
                for (i in result1) {
                    sumTvoc += i.tvocValue.toInt()
                }
                val aveTvoc = (sumTvoc / result1.size)
                arrTvoc3.add(aveTvoc.toString())
                //依序加入時間
                arrTime3.add(sqlStartDate.toString())
                Log.d("getRealmMonth", result1.last().toString())
            } else {
                arrTvoc3.add("0")
                arrTime3.add((sqlStartDate.toString()))
            }
        }

    }
    private fun getBarData3(inputTVOC: ArrayList<String>, inputTime: ArrayList<String>,positionID: Int?): BarData {
        val dataSetA = MyBarDataSet(getChartData3(inputTVOC), "TVOC")
        dataSetA.setColors(intArrayOf(ContextCompat.getColor(context, R.color.progressBarStartColor),
                ContextCompat.getColor(context, R.color.progressBarMidColor),
                ContextCompat.getColor(context, R.color.progressBarEndColor)))

        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(dataSetA) // add the datasets
        cleanTextViewInTVOC()
        return BarData(getLabels3(inputTime,positionID), dataSets)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getLabels3(input: ArrayList<String>, positionID: Int?): List<String> {
        val chartLabels = ArrayList<String>()
        when (positionID) {
            0 -> {
                val dateFormat = SimpleDateFormat("MM/dd HH:mm")
                for (i in 0 until arrTime3.size) {
                    val date = dateFormat.format(input[i].toLong())
                    chartLabels.add(date)
                }
            }
            1 -> {
                val dateFormat = SimpleDateFormat("MM/dd EEEE")
                for (i in 0 until arrTime3.size) {
                    val date = dateFormat.format(input[i].toLong())
                    chartLabels.add(date)
                }
            }
            2 -> {
                val dateFormat = SimpleDateFormat("yyyy/MM/dd")
                for (i in 0 until arrTime3.size) {
                    val date = dateFormat.format(input[i].toLong())
                    chartLabels.add(date)
                }
            }
        }
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
        animationCount = 0
        downloadingData = true
    }

    private fun stopUpdateDataAnimation() {
        mImageViewDataUpdate?.clearAnimation()
        mImageViewDataUpdate?.isEnabled = true
        downloadingData = false
    }

//    private fun startDataAnimationCount() {
//        animationCount = 0
//    }

    private fun setRealTimeBarData(Tvoc: String, Battery: String) {
        val sdFormat = SimpleDateFormat("MM/dd HH:mm:ss", Locale.TAIWAN)
        val date = Date()
        sdFormat.format(date)

        timeArray.add(sdFormat.format(date))
        tvocArray.add(Tvoc)
    //    tvocArray.add("20")
        batteryArray.add(Battery)

        while (tvocArray.size > mDataCount) {
            tvocArray.removeAt(0)
            timeArray.removeAt(0)
        }
        if (radioButtonID == R.id.radioButton_Hour) {
//            mChart?.clear()
//            mChart?.data = getBarData2(tvocArray, timeArray)
//            mChart?.data?.setDrawValues(false)
//            mChart?.setVisibleXRangeMinimum(5.0f)
//            mChart?.setVisibleXRangeMaximum(5.0f)//需要在设置数据源后生效
            //mRadioGroup?.check(radioButtonID!!)
            dependRadioIDDrawChart(radioButtonID!!)
            mChart?.moveViewToX(tvocArray.size.toFloat())//移動視圖by x index
        }
    }

    private fun dependRadioIDDrawChart(radioID:Int?) {
        when (radioID) {
            R.id.radioButton_Hour -> {
                //mChart?.data = getBarData2(tvocArray, timeArray)
                //mChart?.data?.setDrawValues(false)
                //mChart?.setVisibleXRangeMinimum(5.0f)
                //mChart?.setVisibleXRangeMaximum(5.0f)
            }
            R.id.radioButton_Day,
            R.id.radioButton_Week,
            R.id.radioButton_Month -> {
                //mChart?.data = getBarData3(arrTvoc3, arrTime3,radioID)
                //mChart?.data?.setDrawValues(false)
                //mChart?.setVisibleXRangeMinimum(5.0f)
                //mChart?.setVisibleXRangeMaximum(5.0f)
            }
        }
    }

    private fun makeMainFragmentUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BroadcastActions.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BroadcastActions.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BroadcastActions.ACTION_GET_NEW_DATA)
        intentFilter.addAction(BroadcastActions.ACTION_GET_HISTORY_COUNT)
        intentFilter.addAction(BroadcastActions.ACTION_LOADING_DATA)
        intentFilter.addAction(BroadcastActions.ACTION_SAVE_INSTANT_DATA)
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
                BroadcastActions.ACTION_GET_HISTORY_COUNT ->{
                    val bundle = intent.extras
                    val totalData = bundle.getString(BroadcastActions.INTENT_KEY_GET_HISTORY_COUNT)
                    if (totalData.toInt() != 0) {
                        setProgressBarMax(totalData.toInt())
                        //startUpdateDataAnimation()
                        animationCount = 0
                        downloadingData = true
                    }
                    Toast.makeText(context,"共有資料"+ totalData + "筆",Toast.LENGTH_LONG).show()
                }
                BroadcastActions.ACTION_LOADING_DATA -> {
                    val bundle = intent.extras
                    val nowData = bundle.getString(BroadcastActions.INTENT_KEY_LOADING_DATA)
                    setProgressBarNow(nowData.toInt())
                    if(nowData.toInt() == mProgressBar?.max) {
                        downloadingData = false
                        //stopUpdateDataAnimation()
                        downloadComplete = true
                        //mRadioGroup?.check(R.id.radioButton_Hour)
                        drawChart(spinnerPositon)
                    }
                }
                BroadcastActions.ACTION_GET_NEW_DATA -> {
                    if (!downloadingData && !downloadComplete) {
                        getDeviceData()
                        downloadingData = true
                    }
                    val bundle = intent.extras
                    //val tempVal = bundle.getString(BroadcastActions.INTENT_KEY_TEMP_VALUE)
                    //val humiVal = bundle.getString(BroadcastActions.INTENT_KEY_HUMI_VALUE)
                    val tvocVal = bundle.getString(BroadcastActions.INTENT_KEY_TVOC_VALUE)
                    //val co2Val = bundle.getString(BroadcastActions.INTENT_KEY_CO2_VALUE)
                    //val BatteryLife = bundle.getString(BroadcastActions.INTENT_KEY_BATTERY_LIFE)
                    //val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                    //val date = Date()
                    preHeat = bundle.getString(BroadcastActions.INTENT_KEY_PREHEAT_COUNT)
                    if(preHeat == "255") {
                        //新增AnimationCount
                        animationCount++

                        counter++
                        TVOCAVG += tvocVal.toInt()
                        if (counter % getDataCycle == 0) {
                            counter = 0
                            TVOCAVG /= getDataCycle
                            //setRealTimeBarData(TVOCAVG.toString(), BatteryLife)
                            TVOCAVG = 0
                        }
                    }
                }
                BroadcastActions.ACTION_SAVE_INSTANT_DATA -> {
//                    val bundle = intent.extras
//                    val tempVal = bundle.getString(BroadcastActions.INTENT_KEY_TEMP_VALUE)
//                    val humiVal = bundle.getString(BroadcastActions.INTENT_KEY_HUMI_VALUE)
//                    val tvocVal = bundle.getString(BroadcastActions.INTENT_KEY_TVOC_VALUE)
//                    val co2Val = bundle.getString(BroadcastActions.INTENT_KEY_CO2_VALUE)
//                    val createdTime = bundle.getLong(BroadcastActions.INTENT_KEY_CREATED_TIME)
//                    arrTvoc3.add(arrTvoc3.lastIndex + 1,tvocVal)
//                    arrTime3.add(arrTime3.lastIndex + 1,createdTime.toString())
//                    mChart?.clear()
//                    mChart?.data = getBarData3(arrTvoc3, arrTime3, 0)
//                    mChart?.data?.setDrawValues(false)
//                    mChart?.setVisibleXRange(5.0f, 40.0f)
                    if (spinnerPositon == 0) {
                        drawChart(spinnerPositon)
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
        // val mDataCount = 5
        // mDataCount
        val chartData = ArrayList<BarEntry>()
       // for (i in 1 until mDataCount) {
            chartData.add(BarEntry((220).toFloat(), 1))
            chartData.add(BarEntry((660).toFloat(), 2))
            chartData.add(BarEntry((65535).toFloat(), 3))
       // }
        return chartData
    }

    private fun getLabels(): List<String> {
        val chartLabels = ArrayList<String>()
        for (i in 1 until mDataCount) {
            chartLabels.add("X" + i)
        }
        return chartLabels
    }

    private fun nothing() {
        val realm = Realm.getDefaultInstance()
        val query = realm.where(AsmDataModel::class.java)
        for (y in 10..1) {
            val countTime = Date().time - 60 * 60 * 1000 * (y)
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
    private fun getRealmDay123() {
        arrTime3.clear()
        arrTvoc3.clear()
        val touchTime = calObject.timeInMillis
        val endDay = touchTime / 3600000 / 24 * 3600000 * 24 + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
        for (y in 1..1440) {
            val realm = Realm.getDefaultInstance()
            val query = realm.where(AsmDataModel::class.java)
            //設定時間區間

            val endTime = endDay - (y - 1) * 30000 - 3600000*8
            val startTime = endTime - 30000
            Log.d("TimePeriod", ((endTime - startTime) / 1000).toString() + "Second")

            query.between("Created_time", startTime, endTime)

            val result1 = query.findAll()
            Log.d("getRealmFour", result1.size.toString())
            if (result1.size != 0) {
                //var vvoc = 0
                //for (i in result1) {
                //    sumTvoc += i.tvocValue.toInt()
                //}
                //var aveTvoc = (sumTvoc / result1.size)
                Log.d("getRealmFour", result1.last().toString())
                arrTvoc3.add(result1.first()?.tvocValue.toString())
                arrTime3.add(endTime.toString())
            } else {
                arrTvoc3.add("0")
                arrTime3.add(endTime.toString())
            }

        }
        arrTvoc3.reverse()
        arrTime3.reverse()

    }
}


