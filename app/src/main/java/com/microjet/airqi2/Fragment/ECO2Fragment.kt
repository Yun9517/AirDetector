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
import com.microjet.airqi2.CustomAPI.FixBarChart
import com.microjet.airqi2.CustomAPI.MyBarDataSet
import com.microjet.airqi2.CustomAPI.Utils.isFastDoubleClick
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.Definition.BroadcastIntents
import com.microjet.airqi2.R
import io.realm.Realm
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
class ECO2Fragment : Fragment() {
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
    //private var mImageViewFace: ImageView? = null
    private var tvCharLabel: TextView? = null
    private var tvChartTitleTop : TextView? = null
    private var tvChartTitleMiddle : TextView? = null
    private var tvChartTitleBottom : TextView? = null
    //private var imgBarRed : ImageView? = null
    //private var imgBarYellow : ImageView? = null
    //private var imgBarGreen : ImageView? = null
    //private var imgBarBase : ImageView? = null
    private var sprTVOC : Spinner? = null
    private var btnCallDatePicker : Button? = null
    private var show_Yesterday : TextView? = null
    private var show_Today : TextView? = null
    private var result_Yesterday : TextView? = null
    private var result_Today : TextView? = null
    //UI元件
    private var showAvg_ByTime : TextView? = null


    //TestValue Start chungyen
    //private val tvocArray = ArrayList<String>()
    //private val timeArray = ArrayList<String>()
    //private val batteryArray = ArrayList<String>()
    //private var radioButtonID : Int? = 0

    private var mConnectStatus: Boolean = false

    //試Realm拉資料
    private var arrTime3 = ArrayList<String>()
    private var arrTvoc3 = ArrayList<String>()

    private var animationCount = 0
    private var downloadingData = false

    private var preHeat = "0"
    private var getDataCycle = 15

    private val calObject = Calendar.getInstance()
    private var spinnerPositon = 0
    private var datepickerHandler = Handler()
    //private var chartHandler = Handler()
    private var downloadComplete = false

    private var counter = 0
    private var TVOCAVG = 0


    private var labelArray = ArrayList<String>()


    @Suppress("OverridingDeprecatedMember")
    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        mContext = this.context!!.applicationContext
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(mContext!!).registerReceiver(mGattUpdateReceiver, makeMainFragmentUpdateIntentFilter())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater!!.inflate(R.layout.frg_eco2, container, false)

    @SuppressLint("SimpleDateFormat")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mRadioGroup = this.view?.findViewById(R.id.frg_radioGroup)
        mProgressBar = this.view!!.findViewById(R.id.chartDataLoading)
        mHour = this.view!!.findViewById(R.id.radioButton_Hour)
        mTextViewTimeRange = this.view!!.findViewById(R.id.tvSelectDetectionTime)
        mTextViewValue = this.view?.findViewById(R.id.tvSelectDetectionValue)
        tvCharLabel = this.view?.findViewById(R.id.tvChartLabel)
        tvChartTitleTop = this.view?.findViewById(R.id.tvChartTitleTop)
        tvChartTitleMiddle = this.view?.findViewById(R.id.tvChartTitleMiddle)
        tvChartTitleBottom = this.view?.findViewById(R.id.tvChartTitleBottom)
        show_Yesterday = this.view!!.findViewById(R.id.show_Yesterday)
        show_Today = this.view!!.findViewById(R.id.show_Today)
        result_Yesterday = this.view?.findViewById(R.id.result_Yesterday)
        result_Today = this.view?.findViewById(R.id.result_Today)
        showAvg_ByTime = this.view?.findViewById(R.id.averageExposureByTime)
        mChart = this.view!!.findViewById(R.id.chart_line)
        mChart!!.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {
                // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
            @SuppressLint("SetTextI18n")
            override fun onValueSelected(e: Entry?, dataSetIndex: Int, h: Highlight?) {
                //   TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                mTextViewTimeRange!!.text = labelArray[h!!.xIndex]//listString[h.xIndex]
                //mTextViewTimeRange!!.text = mChart?.xAxis?.values?.get(h!!.xIndex)//listString[h.xIndex]
                //mTextViewValue!!.text = h!!.value.toString()+ "ppm"
                val temp = e?.`val`
                mTextViewValue!!.text = temp?.toInt().toString()+" ppm"
            }
        })
        // imgBarRed = this.view?.findViewById(R.id.imgBarRed)
        //imgBarYellow = this.view?.findViewById(R.id.imgBarYellow)
        //imgBarGreen = this.view?.findViewById(R.id.imgBarGreen)
        //imgBarBase = this.view?.findViewById(R.id.imgBarBase)
        //修改上排Spinner及Button
        sprTVOC = this.view?.findViewById(R.id.sprECO2)
        val cycleList = ArrayAdapter.createFromResource(context,R.array.SpinnerArray,android.R.layout.simple_spinner_dropdown_item)
        sprTVOC!!.adapter = cycleList
        sprTVOC!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long)
            {
                view?.textAlignment = View.TEXT_ALIGNMENT_CENTER
                spinnerPositon = position
                when(spinnerPositon) {
                    0 -> {
                        showAvg_ByTime?.text = getString(R.string.averageExposure_Daily)
                    }
                    1 -> {
                        showAvg_ByTime?.text = getString(R.string.averageExposure_Daily)
                    }
                    2 -> {
                        showAvg_ByTime?.text = getString(R.string.averageExposure_Daily)
                    }
                }
                btnTextChanged(spinnerPositon)
                drawChart(spinnerPositon)

                val selectedItem = parent.getItemAtPosition(position).toString()
//                if (selectedItem == "Add new category") {
//                    // do your stuff
//                }
                Log.d("ECO2",selectedItem)
            } // to close the onItemSelected

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        btnCallDatePicker = this.view?.findViewById(R.id.btnCallDatePicker)
        val dateFormat = SimpleDateFormat("yyyy/MM/dd")
        btnCallDatePicker?.text = dateFormat.format(calObject.time)
        btnCallDatePicker?.setOnClickListener {
            datepickerHandler.post {
                val dpd = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    calObject.set(year, month, dayOfMonth)
                    Log.d("ECO2btncall", calObject.get(Calendar.DAY_OF_MONTH).toString())
                    btnTextChanged(spinnerPositon)
                    drawChart(spinnerPositon)
                    timePickerShow()
                },calObject.get(Calendar.YEAR), calObject.get(Calendar.MONTH), calObject.get(Calendar.DAY_OF_MONTH))
                dpd.setMessage(getString(R.string.select_Date))//請選擇日期
                dpd.show()
            }
        }

        mImageViewDataUpdate = this.view?.findViewById(R.id.chart_Refresh)
        mImageViewDataUpdate?.visibility = View.INVISIBLE
        mImageViewDataUpdate?.background = resources.getDrawable(R.drawable.chart_update_icon_bg)
//        mImageViewDataUpdate?.setOnClickListener {
//            if (!isFastDoubleClick){
//                //getDeviceData()
//                Log.d("ECO2","TOAST_ON")
//            }
//        }

        configChartView()
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun btnTextChanged(position: Int?) {
        when(position) {
            0 -> {
                val dateFormat = SimpleDateFormat("yyyy/MM/dd")
                btnCallDatePicker?.text = dateFormat.format(calObject.time)
            }
            1 -> {
                btnCallDatePicker?.text = calObject.get(Calendar.YEAR).toString() + " " + getString(R.string.week_First_Word) + calObject.get(Calendar.WEEK_OF_YEAR).toString() + getString(R.string.week_Last_Word)
            }
            2 -> {
                val dateFormat = SimpleDateFormat("yyyy/MM")
                btnCallDatePicker?.text = dateFormat.format(calObject.time)
            }
        }

    }

    private fun drawChart(position: Int?) {
        setImageBarSize()
        when (position) {
            0 -> {
                val p = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60 * 60+Calendar.getInstance().get(Calendar.MINUTE) * 60 + Calendar.getInstance().get(Calendar.SECOND)
                val l = p / 60
                if (l <= 2) {
                    calObject.set(Calendar.DAY_OF_MONTH,Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    Log.d("drawChart",calObject.toString())
                }
                getRealmDay()
                mChart?.data = getBarData3(arrTvoc3, arrTime3, position)
                mChart?.data?.setDrawValues(false)
                mChart?.setVisibleXRange(14.0f, 14.0f)
                //mChart?.setVisibleXRangeMinimum(20.0f)
                //mChart?.setVisibleXRangeMaximum(20.0f)//需要在设置数据源后生效
                mChart?.centerViewToAnimated(l.toFloat(),0F, YAxis.AxisDependency.LEFT,1000)
                //mChart?.moveViewToX((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                //        + Calendar.getInstance().get(Calendar.MINUTE) / 60F) * 118.5F) //移動視圖by x index
                val y = mChart!!.data!!.dataSetCount
                mChart?.highlightValue(l, y-1)
                //Log.v("Highligh:",l.toString())

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
                mChart?.setVisibleXRange(14.0f, 14.0f)
                mChart?.centerViewToAnimated(Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toFloat(),0F, YAxis.AxisDependency.LEFT,1000)

            }
        }
    }
    private fun setImageBarSize(){
        mChart!!.data = getBarData()
        val line500 = mChart!!.getBarBounds(BarEntry(500f, 1))
        val line1000 = mChart!!.getBarBounds(BarEntry(1000f, 2))
        //val line20000 = mChart!!.getBarBounds(BarEntry(20000f, 3))
        tvChartTitleMiddle?.y = line1000.top - (tvChartTitleMiddle!!.height / 2) - (tvChartTitleMiddle!!.height / 2)//Text1000 position
        tvChartTitleBottom?.y = line500.top - (tvChartTitleBottom!!.height / 2) - (tvChartTitleBottom!!.height / 2)//Text500 position
        //imgBarRed?.y = line1000.top//red
        //imgBarYellow?.y = line660.top//yellow
        //imgBarGreen?.y = line220.top//green
        //imgBarBase?.y = line220.bottom//base

        //視Radio id畫圖
        mChart!!.clear()
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
        btnTextChanged(spinnerPositon)
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

    // 20171128 Added by Raymond
    private fun configChartView() {
        val xAxis: XAxis = mChart!!.xAxis
        val leftAxis: YAxis = mChart!!.axisLeft
        val rightAxis: YAxis = mChart!!.axisRight

        mChart!!.isScaleXEnabled = false
        mChart!!.isScaleYEnabled = false
        leftAxis.setLabelCount(15,true)
        leftAxis.setAxisMaxValue(1500f) // the axis maximum is 1500
        leftAxis.setAxisMinValue(0f) // start at zero
        leftAxis.setDrawLabels(false) // no axis labels
        leftAxis.setDrawAxisLine(false) // no axis line
        leftAxis.setDrawGridLines(true) // no grid lines
        leftAxis.gridColor= Color.WHITE

        xAxis.setDrawGridLines(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        val nums = ArrayList<Float>()
        nums.add(500f)
        nums.add(1000f)

        mChart!!.legend.isEnabled = false
        mChart!!.yChartInterval = nums

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
        val touchTime = calObject.timeInMillis + calObject.timeZone.rawOffset
        Log.d("ECO2btncallRealm",calObject.get(Calendar.DAY_OF_MONTH).toString())
        //將日期設為今天日子加一天減1秒
        val endDay = touchTime / (3600000 * 24) * (3600000 * 24)// - calObject.timeZone.rawOffset
        val endDayLast = endDay + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
        val realm = Realm.getDefaultInstance()
        val query = realm.where(AsmDataModel::class.java)
        //設定時間區間
        val endTime = endDayLast
        val startTime = endDay
        //一天共有2880筆
        val dataCount = (endTime - startTime) / (60 * 1000)
        Log.d("TimePeriod", (dataCount.toString() + "thirtySecondsCount"))
        query.between("Created_time", startTime, endTime).sort("Created_time", Sort.ASCENDING)
        val result1 = query.findAll()
        Log.d("getRealmDay", result1.size.toString())
        var sumTvoc = 0
        //先生出2880筆值為0的陣列
        for (y in 0..dataCount) {
            arrTvoc3.add("0")
            arrTime3.add(((startTime + y * 60 * 1000) - calObject.timeZone.rawOffset).toString())
        }
        var aveTvoc = 0
        //關鍵!!利用取出的資料減掉抬頭時間除以30秒算出index換掉TVOC的值
        if (result1.size != 0) {
            result1.forEachIndexed { index, asmDataModel ->
                val count = ((asmDataModel.created_time - startTime) / (60 * 1000)).toInt()
                arrTvoc3[count] = asmDataModel.ecO2Value.toString()
                //20180122
                sumTvoc += arrTvoc3[count].toInt()
                //Log.v("hilightCount:", count.toString())
            }
            Log.d("getRealmDay", result1.last().toString())
            //20180122
            aveTvoc = (sumTvoc / result1.size)
        }


        //20180124
        //前一天的０點起
        val sqlWeekBase = startTime - TimeUnit.DAYS.toMillis((1).toLong())
        // Show Date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")


        show_Today!!.text = dateFormat.format(startTime)
        show_Yesterday!!.text =  dateFormat.format(startTime - TimeUnit.DAYS.toMillis((1).toLong()))


        //Log.d("getRealmWeek", sqlWeekBase.toString())
        //跑七筆BarChart
        // for (y in 0..1) {
        //第一筆為日 00:00
        val sqlStartDate = sqlWeekBase//+TimeUnit.DAYS.toMillis()
        //結束點為日 23:59
        val sqlEndDate = sqlStartDate + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
        //val realm= Realm.getDefaultInstance()
        val query1 = realm.where(AsmDataModel::class.java)
        //20180122
        var AVGCo2 :Float= 0F
        Log.d("getRealmWeek", sqlStartDate.toString())
        Log.d("getRealmWeek", sqlEndDate.toString())
        query1.between("Created_time", sqlStartDate, sqlEndDate)
        val result2 = query1.findAll()
        Log.d("getRealmWeek", result2.size.toString())
        if (result2.size != 0) {
            var sumCO2Yesterday = 0F
            for (i in result2) {
                sumCO2Yesterday += i.ecO2Value.toInt()
            }
            AVGCo2 = (sumCO2Yesterday / result2.size)
        } else {
            AVGCo2 = 0F
        }

        //}
        result_Today!!.text = aveTvoc.toString() + " ppm"        //arrTvoc3[1].toString()+" ppm"
        result_Yesterday!!.text = AVGCo2.toInt().toString()+ " ppm"
    }

    private fun getRealmWeek() {
        arrTime3.clear()
        arrTvoc3.clear()
        //拿到現在是星期幾的Int
        val dayOfWeek = calObject.get(Calendar.DAY_OF_WEEK)
        val touchTime = calObject.timeInMillis + calObject.timeZone.rawOffset
        //今天的00:00
        val nowDateMills = touchTime / (3600000 * 24) * (3600000 * 24)// - calObject.timeZone.rawOffset
        //將星期幾退回到星期日為第一時間點
        val sqlWeekBase = nowDateMills - TimeUnit.DAYS.toMillis((dayOfWeek - 1).toLong())
        var thisWeekAVETvoc: Int = 0
        var aveLastWeekTvoc = 0
        Log.d("getRealmWeek", sqlWeekBase.toString())
        //跑七筆BarChart
        for (y in 0..6) {
            //第一筆為日 00:00
            val sqlStartDate = sqlWeekBase + TimeUnit.DAYS.toMillis(y.toLong())
            //結束點為日 23:59
            val sqlEndDate = sqlStartDate + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
            val realm = Realm.getDefaultInstance()
            val query = realm.where(AsmDataModel::class.java)
            Log.e("thisGetRealmWeekStart", sqlStartDate.toString())
            Log.e("thisGetRealmWeekEnd", sqlEndDate.toString())
            query.between("Created_time", sqlStartDate, sqlEndDate)
            val result1 = query.findAll()
            Log.d("getRealmWeek", result1.size.toString())
            if (result1.size != 0) {
                var sumThisAndLastWeek = 0
                for (i in result1) {
                    sumThisAndLastWeek += i.ecO2Value.toInt()
                }
                thisWeekAVETvoc = (sumThisAndLastWeek / result1.size)
                arrTvoc3.add(thisWeekAVETvoc.toString())
                //依序加入時間
                arrTime3.add((sqlStartDate - calObject.timeZone.rawOffset).toString())
                //result_Today!!.text = "$thisWeekAVETvoc ppm"        //arrTvoc3[1].toString()+" ppm"
                //Log.e("thisGetRealmWeekAVG", lastWeekAVETvoc.toString())
            } else {
                //result_Today!!.text = "$lastWeekAVETvoc ppm"
                arrTvoc3.add("0")
                arrTime3.add((sqlStartDate - calObject.timeZone.rawOffset).toString())
            }
        }
///*
//        //******************************************************************************************************************************************************************************************************************************************
//        //
//        //上周日的00:00
//        val lastWeeksqlBase = sqlWeekBase - TimeUnit.DAYS.toMillis((7).toLong())
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
//        Log.e("上個禮拜日：", dateFormat.format(lastWeeksqlBase))
//        show_Today!!.text = dateFormat.format(sqlWeekBase)
//        show_Yesterday!!.text =  dateFormat.format(lastWeeksqlBase)
//
//
//        //跑七筆BarChart
//        for (y in 0..6) {
//            //第一筆為日 00:00
//            val sqlStartDate = lastWeeksqlBase + TimeUnit.DAYS.toMillis(y.toLong())
//            //結束點為日 23:59
//            val sqlEndDate = sqlStartDate + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
//            val realm = Realm.getDefaultInstance()
//            val query1 = realm.where(AsmDataModel::class.java)
//            Log.d("lastGetRealmWeekStart", sqlStartDate.toString())
//            Log.d("lastGetRealmWeekEnd", sqlEndDate.toString())
//            query1.between("Created_time", sqlStartDate, sqlEndDate)
//            val lastWeekresult1 = query1.findAll()
//            //Log.d("getRealmWeek", result1.size.toString())
//            if (lastWeekresult1.size != 0) {
//                var lastWeeksumTvoc = 0
//                for (i in lastWeekresult1) {
//                    lastWeeksumTvoc += i.tvocValue.toInt()
//                }
//                aveLastWeekTvoc = (lastWeeksumTvoc / lastWeekresult1.size)
//                //arrTvoc3.add(aveTvoc.toString())
//                //依序加入時間
//                //arrTime3.add((sqlStartDate - calObject.timeZone.rawOffset).toString())
//                //Log.e("lastGetRealmWeekAVG", aveLastWeekTvoc.toString())
//            } else {
//                arrTvoc3.add("0")
//                //result_Yesterday!!.text = "0 ppm"
//                //arrTime3.add((sqlStartDate -calObject.timeZone.rawOffset).toString().toString())
//            }
//        }
//        result_Today!!.text = thisWeekAVETvoc.toString() + " ppm"        //arrTvoc3[1].toString()+" ppm"
//        result_Yesterday!!.text = aveLastWeekTvoc.toInt().toString()+ " ppm"
//
//        //result_Yesterday!!.text = aveLastWeekTvoc.toInt().toString()+ " ppm"
//            //******************************************************************************************************************************************************************************************************************************************
//        */
    }
    private fun getRealmMonth() {
        arrTime3.clear()
        arrTvoc3.clear()
        //拿到現在是星期幾的Int
        val dayOfMonth = calObject.get(Calendar.DAY_OF_MONTH)
        val monthCount = calObject.getActualMaximum(Calendar.DAY_OF_MONTH)
        val touchTime = calObject.timeInMillis + calObject.timeZone.rawOffset
        val nowDateMills = touchTime / (3600000 * 24) * (3600000 * 24)// - calObject.timeZone.rawOffset
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
            val dataCount = (sqlEndDate - sqlStartDate) / (60 * 1000)
            Log.d("TimePeriod", (dataCount.toString() + "thirtySecondsCount"))
            Log.d("getRealmMonth", sqlStartDate.toString())
            Log.d("getRealmMonth", sqlEndDate.toString())
            query.between("Created_time", sqlStartDate, sqlEndDate)
            val result1 = query.findAll()
            Log.d("getRealmMonth", result1.size.toString())
            if (result1.size != 0) {
                var sumTvoc = 0
                for (i in result1) {
                    sumTvoc += i.ecO2Value.toInt()
                }
                val aveTvoc = (sumTvoc / result1.size)
                arrTvoc3.add(aveTvoc.toString())
                //依序加入時間
                arrTime3.add((sqlStartDate - calObject.timeZone.rawOffset).toString())
                Log.d("getRealmMonth", result1.last().toString())
            } else {
                arrTvoc3.add("0")
                arrTime3.add((sqlStartDate - calObject.timeZone.rawOffset).toString())
            }
        }

    }
    private fun getBarData3(inputTVOC: ArrayList<String>, inputTime: ArrayList<String>,positionID: Int?): BarData {
        val dataSetA = MyBarDataSet(getChartData3(inputTVOC), "ECO2")
        dataSetA.setColors(intArrayOf(ContextCompat.getColor(context!!, R.color.Main_textResult_Good),
                ContextCompat.getColor(context!!, R.color.Main_textResult_Moderate),
                ContextCompat.getColor(context!!, R.color.Main_textResult_Orange),
                ContextCompat.getColor(context!!, R.color.Main_textResult_Bad),
                ContextCompat.getColor(context!!, R.color.Main_textResult_Purple),
                ContextCompat.getColor(context!!, R.color.Main_textResult_Unhealthy)))

        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(dataSetA) // add the datasets
        cleanTextViewInTVOC()
        return BarData(getLabels3(inputTime,positionID), dataSets)
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun getLabels3(input: ArrayList<String>, positionID: Int?): List<String> {
        val chartLabels = ArrayList<String>()
        when (positionID) {
            0 -> {
                val dateFormat = SimpleDateFormat("HH:mm")
                val dateLabelFormat = SimpleDateFormat("HH:mm")
                labelArray.clear()
                for (i in 0 until arrTime3.size) {
                    val date = dateFormat.format(input[i].toLong())
                    val dateLabel = dateLabelFormat.format(input[i].toLong())
                    chartLabels.add(date)
                    labelArray.add(dateLabel)
                }

//                getCO2ToAndYesterdayAvgData()
//                result_Today!!.text = arrTvoc3[1] + " ppm"        //arrTvoc3[1].toString()+" ppm"
//                result_Yesterday!!.text = arrTvoc3[0] + " ppm"
//                Log.e("兩天資料:", arrTvoc3.toString())
//                Log.e("兩天時數:", arrTime3.toString())
            }
            1 -> {
                val dateFormat = SimpleDateFormat("EEEE")
                val dateLabelFormat = SimpleDateFormat("MM/dd EEEE")
                labelArray.clear()
                for (i in 0 until arrTime3.size) {
                    val date = dateFormat.format(input[i].toLong())
                    val dateLabel = dateLabelFormat.format(input[i].toLong())
                    chartLabels.add(date)
                    labelArray.add(dateLabel)
                }
                result_Today!!.text = getString(R.string.text_default_value)
                result_Yesterday!!.text = getString(R.string.text_default_value)
                show_Today!!.text = getString(R.string.text_default_value)
                show_Yesterday!!.text = getString(R.string.text_default_value)
            }
            2 -> {
                val dateFormat = SimpleDateFormat("MM/dd")
                val dateLabelFormat = SimpleDateFormat("yyyy/MM/dd")
                labelArray.clear()
                for (i in 0 until arrTime3.size) {
                    val date = dateFormat.format(input[i].toLong())
                    val dateLabel = dateLabelFormat.format(input[i].toLong())
                    chartLabels.add(date)
                    labelArray.add(dateLabel)
                }
                result_Today!!.text = getString(R.string.text_default_value)
                result_Yesterday!!.text = getString(R.string.text_default_value)
                show_Today!!.text = getString(R.string.text_default_value)
                show_Yesterday!!.text = getString(R.string.text_default_value)
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
                    //Toast.makeText(context,"共有資料"+ totalData + "筆",Toast.LENGTH_LONG).show()
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
                        btnTextChanged(spinnerPositon)
                        drawChart(spinnerPositon)
                    }
                }
                BroadcastActions.ACTION_GET_NEW_DATA -> {
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

                        counter ++
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
                        btnTextChanged(spinnerPositon)
                        drawChart(spinnerPositon)
                    }
                }
            }
            checkUIState()
        }
    }




    private fun getBarData(): BarData {
        val dataSetA = MyBarDataSet(getChartData(), "TVOC")
        dataSetA.setColors(intArrayOf(ContextCompat.getColor(context!!, R.color.progressBarStartColor),
                ContextCompat.getColor(context!!, R.color.progressBarMidColor),
                ContextCompat.getColor(context!!, R.color.progressBarEndColor)))

        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(dataSetA) // add the datasets

        return BarData(getLabels(), dataSets)
    }

    private fun getChartData(): List<BarEntry> {
        // val mDataCount = 5
        // mDataCount
        val chartData = ArrayList<BarEntry>()
        // for (i in 1 until mDataCount) {
        chartData.add(BarEntry((500).toFloat(), 1))
        chartData.add(BarEntry((1000).toFloat(), 2))
        chartData.add(BarEntry((20000).toFloat(), 3))
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

    @SuppressLint("SetTextI18n")
//    private fun getCO2ToAndYesterdayAvgData() {
//        arrTime3.clear()
//        arrTvoc3.clear()
//        //拿到現在是星期幾的Int
//        val dayOfWeek = calObject.get(Calendar.DAY_OF_WEEK)
//        val touchTime = calObject.timeInMillis + calObject.timeZone.rawOffset
//        //今天的0點為起點
//        val nowDateMills = touchTime / (3600000 * 24) * (3600000 * 24)// - calObject.timeZone.rawOffset
//        //前一天的０點起
//        val sqlWeekBase = nowDateMills - TimeUnit.DAYS.toMillis((1).toLong())
//        // Show Date
//        val dateFormat = SimpleDateFormat("yyyy/MM/dd")
//        show_Today!!.text = dateFormat.format(nowDateMills)
//        show_Yesterday!!.text =  dateFormat.format(nowDateMills - TimeUnit.DAYS.toMillis((1).toLong()))
//        Log.d("getRealmWeek", sqlWeekBase.toString())
//        //跑七筆BarChart
//        for (y in 0..1) {
//            //第一筆為日 00:00
//            val sqlStartDate = sqlWeekBase+TimeUnit.DAYS.toMillis((y.toLong()))
//            //結束點為日 23:59
//            val sqlEndDate = sqlStartDate + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
//            val realm = Realm.getDefaultInstance()
//            val query = realm.where(AsmDataModel::class.java)
//            Log.d("getRealmWeek", sqlStartDate.toString())
//            Log.d("getRealmWeek", sqlEndDate.toString())
//            query.between("Created_time", sqlStartDate, sqlEndDate)
//            val result1 = query.findAll()
//            Log.d("getRealmWeek", result1.size.toString())
//            if (result1.size != 0) {
//                var sumTvoc = 0
//                for (i in result1) {
//                    sumTvoc += i.ecO2Value.toInt()
//                }
//                val aveTvoc = (sumTvoc / result1.size)
//                arrTvoc3.add(aveTvoc.toString())
//                //依序加入時間
//                //arrTime3.add(sqlStartDate.toString())
//                Log.e("值", arrTvoc3[y])
//                Log.e("getRealmWeek", result1.last().toString())
//            } else {
//                arrTvoc3.add("0")
//                arrTime3.add((sqlStartDate.toString()))
//            }
//
//        }
//    }

    private fun timePickerShow(){
        if (spinnerPositon == 0) {
            val tpd = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                val p = hourOfDay * 60 + minute
                mChart?.centerViewToAnimated(p.toFloat(), 0F, YAxis.AxisDependency.LEFT, 1000)
                //mChart?.moveViewToX((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                //        + Calendar.getInstance().get(Calendar.MINUTE) / 60F) * 118.5F) //移動視圖by x index
                val y = mChart!!.data!!.dataSetCount
                mChart?.highlightValue(p, y - 1)
            }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), false)
            tpd.setMessage("請選擇時間")
            tpd.show()
        }
    }
}


