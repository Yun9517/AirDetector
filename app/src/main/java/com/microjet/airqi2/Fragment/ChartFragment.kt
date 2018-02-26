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
import com.microjet.airqi2.CustomAPI.FixBarChart
import com.microjet.airqi2.CustomAPI.MyBarDataSet
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.Definition.BroadcastIntents
import com.microjet.airqi2.R
import io.realm.Realm
import io.realm.Sort
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * Created by B00055 on 2018/2/9.
 */
class ChartFragment: Fragment() {
    private val DEFINE_FRAGMENT_TVOC = 1
    private val DEFINE_FRAGMENT_ECO2 = 2
    private val DEFINE_FRAGMENT_TEMPERATURE = 3
    private val DEFINE_FRAGMENT_HUMIDITY = 4

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
    private var CharLabel: TextView? = null
    private var tvChartTitleTop : TextView? = null
    // 20% ~ 80%
    //private var tvChartTitleMiddle : TextView? = null
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
    private var faceBar :   ImageView? = null
    private var CharRelativeLayoutForLabel:RelativeLayout? = null
    //UI元件
    private var showAvg_ByTime : TextView? = null

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
    var UseFor = 0
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
    private var intArray :IntArray?=null
    private var chartLabel:String=""
    private fun setImageBarPosition(){
        mChart!!.data = getBarData()
        mChart!!.yChartInterval.size
        var j = 1
        var lineRectFArray = ArrayList<RectF>()
        for (i in chartMin.toInt()..chartMax.toInt() step chartIntervalStep){//取得有標籤的數值位置，從最小值放至最大值
            lineRectFArray.add(mChart!!.getBarBounds(BarEntry(i.toFloat(), j)))
            j++
        }
        for  (  i in lineRectFArray.indices){//放置標籤
            labelTextViewArray[i].y = lineRectFArray[i].top - (labelTextViewArray[i].height / 2f )
            labelTextViewArray[i].x = mChart!!.x-labelTextViewArray[i].width.toFloat()
        }
    //    labelTextViewArray[0].y=lineRectFArray[0].top - labelTextViewArray[0].height/2f  //放置最底層的標籤
        if (!chartIsShowMinTextView){
            labelTextViewArray[0].visibility = View.INVISIBLE
        }

        //視Radio id畫圖
        mChart!!.clear()
    }
    fun ConfigFragment(input:Int){
        UseFor = input
        when (input){
            DEFINE_FRAGMENT_TVOC->{
                chartLabel = "TVOC"
                chartMin = 0.0f
                chartMax = 1500.0f
                chartIntervalStep = 500
                chartIntervalStart = 500
                chartIntervalEnd = 1000
                chartLabelYCount = 16
                chartIsShowMinTextView = false
                chartLabelUnit = "(ppb)"
            }
            DEFINE_FRAGMENT_ECO2->{
                chartLabel = "ECO2"
                chartMin = 0.0f
                chartMax = 1500.0f
                chartIntervalStep = 500
                chartIntervalStart = 500
                chartIntervalEnd = 1000
                chartLabelYCount = 16
                chartIsShowMinTextView=false
                chartLabelUnit = "(ppm)"
            }
            DEFINE_FRAGMENT_TEMPERATURE->{
                chartLabel = "Temp"
                chartMin = 0.0f
                chartMax = 60.0f
                chartIntervalStep = 5
                chartIntervalStart = 5
                chartIntervalEnd = 55
                chartLabelYCount = 13
                chartIsShowMinTextView = true
                chartLabelUnit = "(°C)"
            }
            DEFINE_FRAGMENT_HUMIDITY->{
                chartLabel = "Humi"
                chartMin = 0.0f
                chartMax = 100.0f
                chartIntervalStep = 20
                chartIntervalStart = 20
                chartIntervalEnd = 80
                chartLabelYCount = 6
                chartIsShowMinTextView = false
                chartLabelUnit = "( %)"
            }
        }
    }
    // 20171128 Added by Raymond
    private fun configChartView() {
        val xAxis: XAxis = mChart!!.xAxis
        val leftAxis: YAxis = mChart!!.axisLeft
        val rightAxis: YAxis = mChart!!.axisRight

        mChart!!.isScaleXEnabled = false
        mChart!!.isScaleYEnabled = false
        leftAxis.setLabelCount(chartLabelYCount,true)
        leftAxis.setAxisMaxValue(chartMax) // the axis maximum is 1500
        leftAxis.setAxisMinValue(chartMin) // start at zero
        leftAxis.setDrawLabels(false) // no axis labels
        leftAxis.setDrawAxisLine(false) // no axis line
        leftAxis.setDrawGridLines(true) // no grid lines
        leftAxis.gridColor = Color.WHITE

        xAxis.setDrawGridLines(false)

        xAxis.position = XAxis.XAxisPosition.BOTTOM
        val nums = ArrayList<Float>()

        for (i in chartIntervalStart..chartIntervalEnd step chartIntervalStep)
        {
            nums.add(i.toFloat())
        }
        mChart!!.legend.isEnabled = false
        mChart!!.yChartInterval = nums
        mChart!!.setDrawValueAboveBar(false)
        rightAxis.isEnabled = false
        mChart?.setDescription("")// clear default string
    }
    override fun onSaveInstanceState(outState: Bundle?) {

        outState?.putInt("UseFor", UseFor)
        outState?.putInt("chartIntervalStep", chartIntervalStep)
        outState?.putFloat("chartMin", chartMin)
        outState?.putFloat("chartMax", chartMax)
        outState?.putInt("chartIntervalStart", chartIntervalStart)
        outState?.putInt("chartIntervalEnd", chartIntervalEnd)
        outState?.putInt("chartLabelYCount", chartLabelYCount)
        outState?.putBoolean("chartIsShowMinTextView", chartIsShowMinTextView)
        outState?.putString("chartLabelUnit", chartLabelUnit)
        outState?.putString("chartLabel", chartLabel)
        super.onSaveInstanceState(outState)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mRadioGroup = this.view?.findViewById(R.id.frg_radioGroup)
        mProgressBar = this.view!!.findViewById(R.id.chartDataLoading)
        mHour = this.view!!.findViewById(R.id.radioButton_Hour)
        mTextViewTimeRange = this.view!!.findViewById(R.id.ChartSelectDetectionTime)
        mTextViewValue = this.view?.findViewById(R.id.ChartSelectDetectionValue)
        CharLabel = this.view?.findViewById(R.id.ChartLabel)
        tvChartTitleTop = this.view?.findViewById(R.id.tvChartTitleTop)
        faceBar = this.view?.findViewById(R.id.faceBar)
        //tvChartTitleMiddle = this.view?.findViewById(R.id.tvChartTitleMiddle)
        CharRelativeLayoutForLabel = this.view?.findViewById(R.id.RelativeLayoutForLabelTextView)
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            UseFor = savedInstanceState.getInt("UseFor")
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

        var j = 0
        for (i in chartMin.toInt()..chartMax.toInt() step chartIntervalStep)
        {
            var textView = TextView(this.context)
            textView.width = 200
            textView.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            labelTextViewArray.add(textView)
            when (i){
                chartMax.toInt()->{textView.text = chartLabelUnit}
                else->{
                    when (UseFor){
                        DEFINE_FRAGMENT_TEMPERATURE->{
                            textView.text = (chartMin-10+(j)*chartIntervalStep).toInt().toString()
                        }
                        else-> {
                            textView.text = (chartMin+(j)*chartIntervalStep).toInt().toString()
                        }
                    }
                }
            }
            j++
            CharRelativeLayoutForLabel?.addView(textView)
        }

        /*
        for (i in 1..chartLabelYCount){
            var textView=TextView(this.context)
            textView.width=50
            textView.textAlignment=View.TEXT_ALIGNMENT_VIEW_END
            labelTextViewArray.add(textView)
            when (i){
                chartLabelYCount->{textView.text = chartLabelUnit}
                else->{
                    when (UseFor){
                        DEFINE_FRAGMENT_TEMPERATURE->{
                            textView.text = (chartMin-10+(i-1)*chartIntervalStep).toInt().toString()
                        }
                        else-> {
                            textView.text = (chartMin+(i-1)*chartIntervalStep).toInt().toString()
                        }
                    }
                }
              /*  else ->{
                    when (UseFor){
                        DEFINE_FRAGMENT_TEMPERATURE->{

                        }
                        else-> {
                            textView.text=((i-1)*chartIntervalStep).toString()
                        }
                    }
                }*/
            }
            CharRelativeLayoutForLabel?.addView(textView)
        }
        */
    /*    humiChartTitle5 = this.view?.findViewById(R.id.humiChartTitle5)
        humiChartTitle4 = this.view?.findViewById(R.id.humiChartTitle4)
        humiChartTitle3 = this.view?.findViewById(R.id.humiChartTitle3)
        humiChartTitle2 = this.view?.findViewById(R.id.humiChartTitle2)
        humiChartTitleBottom = this.view?.findViewById(R.id.humiChartTitleBottom)
        */
       // CharRelativeLayoutForLabel?.removeView( humiChartTitle4 )
        show_Yesterday = this.view?.findViewById(R.id.show_Yesterday)
        show_Today = this.view?.findViewById(R.id.show_Today)
        result_Yesterday = this.view?.findViewById(R.id.result_Yesterday)
        result_Today = this.view?.findViewById(R.id.result_Today)
        showAvg_ByTime = this.view?.findViewById(R.id.averageExposureByTime)
        mChart = this.view!!.findViewById(R.id.chart_line)
        mChart!!.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {
                // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
            @SuppressLint("SetTextI18n", "SimpleDateFormat")
            override fun onValueSelected(e: Entry?, dataSetIndex: Int, h: Highlight?) {
                //   TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                mTextViewTimeRange!!.text = labelArray[h!!.xIndex]//listString[h.xIndex]
                //mTextViewTimeRange!!.text = mChart?.xAxis?.values?.get(h!!.xIndex)//listString[h.xIndex]
                //mTextViewValue!!.text = h!!.value.toString()+ "ppb"
                when (UseFor){
                    DEFINE_FRAGMENT_TVOC ->{
                        val temp = e?.`val`
                        mTextViewValue!!.text = temp?.toInt().toString() + " ppb"
                    }
                    DEFINE_FRAGMENT_ECO2 ->{
                        val temp = e?.`val`
                        mTextViewValue!!.text = temp?.toInt().toString()+" ppm"
                    }
                    DEFINE_FRAGMENT_TEMPERATURE ->{
                        val temp: Float? = e?.`val`
                        val temp1: Float? = (temp!! - 10.0f)
                        if (temp1!! <= -10.0f) {
                            mTextViewValue!!.text = "---" + " ℃"
                        }else{
                            val newTemp = "%.1f".format(temp1)
                            mTextViewValue!!.text = newTemp + " ℃"
                        }
                    }
                    DEFINE_FRAGMENT_HUMIDITY->{
                        val temp = e?.`val`
                        mTextViewValue!!.text = temp?.toInt().toString() + " %"
                    }
                }

            }
        })

        //修改上排Spinner及Button
        sprTVOC = this.view?.findViewById(R.id.sprChart)
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
                    1 ->{
                        showAvg_ByTime?.text = getString(R.string.averageExposure_Daily)
                    }
                    2->{
                        showAvg_ByTime?.text = getString(R.string.averageExposure_Daily)
                    }
                }
                btnTextChanged(spinnerPositon)
                drawChart(spinnerPositon)

                val selectedItem = parent.getItemAtPosition(position).toString()
//                if (selectedItem == "Add new category") {
//                    // do your stuff
//                }
                Log.d("Humi",selectedItem)
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
                    calObject.set(year,month,dayOfMonth)
                    Log.d("ChartBtncall"+UseFor.toString(),calObject.get(Calendar.DAY_OF_MONTH).toString())
                    btnTextChanged(spinnerPositon)
                    drawChart(spinnerPositon)
                    timePickerShow()
                },calObject.get(Calendar.YEAR),calObject.get(Calendar.MONTH),calObject.get(Calendar.DAY_OF_MONTH))
                dpd.setMessage("請選擇日期")
                dpd.show()
            }
        }

        mImageViewDataUpdate = this.view?.findViewById(R.id.chart_Refresh)
        mImageViewDataUpdate?.visibility = View.INVISIBLE
        mImageViewDataUpdate?.background = resources.getDrawable(R.drawable.chart_update_icon_bg)
        mImageViewDataUpdate?.setOnClickListener {
            //            if (!isFastDoubleClick){
//                getDeviceData()
//                Log.d("TVOC","TOAST_ON")
//            }
        }
        when(UseFor){
            DEFINE_FRAGMENT_TVOC ->{
                CharLabel?.text = getString(R.string.text_label_tvoc)
                faceBar?.setImageResource(R.drawable.face_bar_tvoc)
                intArray = intArrayOf(ContextCompat.getColor(mContext, R.color.Main_textResult_Good),
                        ContextCompat.getColor(context, R.color.Main_textResult_Moderate),
                        ContextCompat.getColor(context, R.color.Main_textResult_Orange),
                        ContextCompat.getColor(context, R.color.Main_textResult_Bad),
                        ContextCompat.getColor(context, R.color.Main_textResult_Purple),
                        ContextCompat.getColor(context, R.color.Main_textResult_Unhealthy))
            }
            DEFINE_FRAGMENT_ECO2 ->{
                CharLabel?.text = getString(R.string.text_label_co2)
                faceBar?.setImageResource(R.drawable.face_bar_eco2)
                intArray = intArrayOf(ContextCompat.getColor(mContext, R.color.Main_textResult_Good),
                        ContextCompat.getColor(context, R.color.Main_textResult_Moderate),
                        ContextCompat.getColor(context, R.color.Main_textResult_Orange),
                        ContextCompat.getColor(context, R.color.Main_textResult_Bad),
                        ContextCompat.getColor(context, R.color.Main_textResult_Purple),
                        ContextCompat.getColor(context, R.color.Main_textResult_Unhealthy))
            }
            DEFINE_FRAGMENT_TEMPERATURE ->{
                CharLabel?.text = getString(R.string.text_label_temperature_full)
                faceBar?.setImageResource(R.drawable.face_bar_temp)
                intArray = intArrayOf(ContextCompat.getColor(mContext, R.color.Main_textResult_Blue),
                        ContextCompat.getColor(context, R.color.Main_textResult_Good),
                        ContextCompat.getColor(context, R.color.Main_textResult_Bad))
            }
            DEFINE_FRAGMENT_HUMIDITY->{
                CharLabel?.text = getString(R.string.text_label_humidity)
                faceBar?.setImageResource(R.drawable.face_bar_humidity)
                intArray = intArrayOf(ContextCompat.getColor(context, R.color.Main_textResult_Blue),
                        ContextCompat.getColor(context, R.color.Main_textResult_Good),
                        ContextCompat.getColor(context, R.color.Main_textResult_Bad))
            }
        }

        configChartView()
        //mChart!!.setOnChartValueSelectedListener(this)
    }
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater!!.inflate(R.layout.frg_chart, container, false)

    @Suppress("OverridingDeprecatedMember")
    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        mContext = this.context.applicationContext
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(mContext!!).registerReceiver(mGattUpdateReceiver, makeMainFragmentUpdateIntentFilter())
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

    @SuppressLint("SetTextI18n")
    private fun drawChart(position: Int?) {
        setImageBarPosition()

        when (position) {
            0 -> {
                val p = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60 * 60 + Calendar.getInstance().get(Calendar.MINUTE) * 60 + Calendar.getInstance().get(Calendar.SECOND)
                val l = p / 60
                if (l <= 2) {
                    calObject.set(Calendar.DAY_OF_MONTH,Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                    Log.d("drawChart"+UseFor.toString(),calObject.toString())
                }
                getRealmDay()
                mChart?.data = getBarData3(arrData, arrTime, position)
                mChart?.data?.setDrawValues(false)
                mChart?.setVisibleXRange(14.0f, 14.0f)
                //mChart?.setVisibleXRangeMinimum(20.0f)
                //mChart?.setVisibleXRangeMaximum(20.0f)//需要在设置数据源后生效
                //mChart?.centerViewToAnimated((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                //        + Calendar.getInstance().get(Calendar.MINUTE) / 60F) * 120F,0F, YAxis.AxisDependency.LEFT,1000)
                mChart?.centerViewToAnimated(l.toFloat(),0F, YAxis.AxisDependency.LEFT,1000)
                //mChart?.moveViewToX((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                //        + Calendar.getInstance().get(Calendar.MINUTE) / 60F) * 118.5F) //移動視圖by x index
                val y = mChart!!.data!!.dataSetCount
                mChart?.highlightValue(l, y-1)
                //Log.v("Highligh:",l.toString())
            }
            1 -> {
                getRealmWeek()
                mChart?.data = getBarData3(arrData, arrTime, position)
                mChart?.data?.setDrawValues(false)
                mChart?.animateY(3000, Easing.EasingOption.EaseOutBack)
                mChart?.setVisibleXRange(7.0f, 7.0f)
                mChart?.centerViewToAnimated(Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toFloat(),0F, YAxis.AxisDependency.LEFT,1000)
            }
            2 -> {
                getRealmMonth()
                mChart?.data = getBarData3(arrData, arrTime, position)
                mChart?.data?.setDrawValues(false)
                mChart?.animateY(3000, Easing.EasingOption.EaseOutBack)
                mChart?.setVisibleXRange(14.0f, 14.0f)

            }
        }

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

    private fun getDeviceData() {
        when (UseFor) {
            DEFINE_FRAGMENT_TVOC->{
                if (mConnectStatus && !downloadingData) {
                    val intent: Intent? = Intent(BroadcastIntents.PRIMARY)
                    intent!!.putExtra("status", BroadcastActions.ACTION_GET_SAMPLE_RATE)
                    context.sendBroadcast(intent)
                    Log.d("Fragment"+UseFor.toString(),"getDeviceData")
                }
            }
            else->{
            }
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
    private fun getRealmDay() {
        arrTime.clear()
        arrData.clear()
        //現在時間實體毫秒
        //var touchTime = Calendar.getInstance().timeInMillis
        val touchTime = calObject.timeInMillis + calObject.timeZone.rawOffset
        Log.d("TVOCbtncallRealm"+UseFor.toString(),calObject.get(Calendar.DAY_OF_MONTH).toString())
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
        Log.d("TimePeriod"+UseFor.toString(), (dataCount.toString() + "thirtySecondsCount"))
        query.between("Created_time", startTime, endTime).sort("Created_time", Sort.ASCENDING)
        val result1 = query.findAll()
        Log.d("getRealmDay"+UseFor.toString(), result1.size.toString())
        var avgValueFloat = 0.0f
        var avgValueInt = 0
        var sumValueFloat = 0.0f
        var sumValueInt = 0

        //先生出2880筆值為0的陣列
        for (y in 0..dataCount) {
            arrData.add("0")
            arrTime.add(((startTime + y * 60 * 1000) - calObject.timeZone.rawOffset).toString())
        }

        //關鍵!!利用取出的資料減掉抬頭時間除以30秒算出index換掉TVOC的值
        if (result1.size != 0) {
            result1.forEachIndexed { index, asmDataModel ->
                val count = ((asmDataModel.created_time - startTime) / (60 * 1000)).toInt()
                when(UseFor) {
                    DEFINE_FRAGMENT_TVOC ->{
                        arrData[count] = asmDataModel.tvocValue.toString()
                        sumValueInt += arrData[count].toInt()
                    }
                    DEFINE_FRAGMENT_ECO2 ->{
                        arrData[count] = asmDataModel.ecO2Value.toString()
                        sumValueInt += arrData[count].toInt()
                    }
                    DEFINE_FRAGMENT_TEMPERATURE ->{
                        arrData[count] = (asmDataModel.tempValue.toFloat()+10.0F).toString()
                        sumValueFloat += arrData[count].toFloat()
                    }
                    DEFINE_FRAGMENT_HUMIDITY->{
                        arrData[count] = asmDataModel.humiValue.toString()
                        sumValueInt += arrData[count].toInt()
                    }
                }
                //Log.v("hilightCount:", count.toString())
            }
            Log.d("getRealmDay"+UseFor.toString(), result1.last().toString())
            //20180122
            when(UseFor) {
                DEFINE_FRAGMENT_TEMPERATURE->{
                    avgValueFloat = (sumValueFloat / result1.size)-10.0f
                }
                else->{
                    avgValueInt = (sumValueInt / result1.size)
                }
            }
        }
        //前一天的0點起
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
        var AVGTvoc3 = 0.0F
        Log.d("getRealmWeek"+UseFor.toString(), sqlStartDate.toString())
        Log.d("getRealmWeek"+UseFor.toString(), sqlEndDate.toString())
        query1.between("Created_time", sqlStartDate, sqlEndDate)
        val result2 = query1.findAll()
        Log.d("getRealmWeek"+UseFor.toString(), result2.size.toString())
        if (result2.size != 0) {
            var sumYesterday = 0.0F
            for (i in result2) {
                when(UseFor)
                {
                    DEFINE_FRAGMENT_TVOC ->{
                        sumYesterday += i.tvocValue.toInt()
                    }
                    DEFINE_FRAGMENT_ECO2 ->{
                        sumYesterday += i.ecO2Value.toInt()
                    }
                    DEFINE_FRAGMENT_TEMPERATURE ->{
                        sumYesterday += i.tempValue.toFloat()
                    }
                    DEFINE_FRAGMENT_HUMIDITY->{
                        sumYesterday += i.humiValue.toInt()
                    }
                }
            }
            AVGTvoc3 = (sumYesterday / result2.size)
        } else {
            AVGTvoc3 = 0.0F
        }
        //}
        when(UseFor)
        {
            DEFINE_FRAGMENT_TVOC ->{
                result_Today!!.text = avgValueInt.toString() + " ppb"
                result_Yesterday!!.text = AVGTvoc3.toInt().toString()+ " ppb"
            }
            DEFINE_FRAGMENT_ECO2 ->{
                result_Today!!.text = avgValueInt.toString() + " ppm"
                result_Yesterday!!.text = AVGTvoc3.toInt().toString()+ " ppm"
            }
            DEFINE_FRAGMENT_TEMPERATURE ->{
                result_Today!!.text = "%.1f".format(avgValueFloat) + " ℃"
                result_Yesterday!!.text = "%.1f".format(AVGTvoc3)+ " ℃"
            }
            DEFINE_FRAGMENT_HUMIDITY->{
                result_Today!!.text = avgValueInt.toString() + " %"
                result_Yesterday!!.text = AVGTvoc3.toInt().toString()+ " %"
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getRealmWeek() {
        arrTime.clear()
        arrData.clear()
        //拿到現在是星期幾的Int
        val dayOfWeek = calObject.get(Calendar.DAY_OF_WEEK)
        val touchTime = calObject.timeInMillis + calObject.timeZone.rawOffset
        //今天的00:00
        val nowDateMills = touchTime / (3600000 * 24) * (3600000 * 24)// - calObject.timeZone.rawOffset
        //將星期幾退回到星期日為第一時間點
        val sqlWeekBase = nowDateMills - TimeUnit.DAYS.toMillis((dayOfWeek -1).toLong())
        var thisWeekAVETvoc = 0f
        var aveLastWeekTvoc = 0
        Log.d("getRealmWeek"+UseFor.toString(), sqlWeekBase.toString())
        //跑七筆BarChart
        for (y in 0..6) {
            //第一筆為日 00:00
            val sqlStartDate = sqlWeekBase + TimeUnit.DAYS.toMillis(y.toLong())
            //結束點為日 23:59
            val sqlEndDate = sqlStartDate + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
            val realm = Realm.getDefaultInstance()
            val query = realm.where(AsmDataModel::class.java)
            Log.e("thisGetRealmWeekStart"+UseFor.toString(), sqlStartDate.toString())
            Log.e("thisGetRealmWeekEnd"+UseFor.toString(), sqlEndDate.toString())
            query.between("Created_time", sqlStartDate, sqlEndDate)
            val result1 = query.findAll()
            Log.d("getRealmWeek"+UseFor.toString(), result1.size.toString())
            if (result1.size != 0) {
                var sumThisAndLastWeek = 0f
                for (i in result1) {
                    when(UseFor)
                    {
                        DEFINE_FRAGMENT_TVOC ->{
                            sumThisAndLastWeek += i.tvocValue.toInt()
                        }
                        DEFINE_FRAGMENT_ECO2 ->{
                            sumThisAndLastWeek += i.ecO2Value.toInt()
                        }
                        DEFINE_FRAGMENT_TEMPERATURE ->{
                            sumThisAndLastWeek += i.tempValue.toFloat()+10.0f
                        }
                        DEFINE_FRAGMENT_HUMIDITY->{
                            sumThisAndLastWeek += i.humiValue.toInt()
                        }
                    }
                }
                thisWeekAVETvoc = (sumThisAndLastWeek / result1.size)
                arrData.add(thisWeekAVETvoc.toString())
                //依序加入時間
                arrTime.add((sqlStartDate - calObject.timeZone.rawOffset).toString())
            } else {
                //result_Today!!.text = "$lastWeekAVETvoc ppb"
                arrData.add("0")
                arrTime.add((sqlStartDate -calObject.timeZone.rawOffset).toString())
            }
        }
    }
    private fun getRealmMonth() {
        arrTime.clear()
        arrData.clear()
        //拿到現在是星期幾的Int
        val dayOfMonth = calObject.get(Calendar.DAY_OF_MONTH)
        val monthCount = calObject.getActualMaximum(Calendar.DAY_OF_MONTH)
        val touchTime = calObject.timeInMillis + calObject.timeZone.rawOffset
        val nowDateMills = touchTime / (3600000 * 24) * (3600000 * 24)// - calObject.timeZone.rawOffset
        //將星期幾退回到星期日為第一時間點
        val sqlMonthBase = nowDateMills - TimeUnit.DAYS.toMillis((dayOfMonth - 1).toLong())
        Log.d("getRealmMonth"+UseFor.toString(), sqlMonthBase.toString())
        //跑七筆BarChart
        for (y in 0..(monthCount-1)) {
            //第一筆為日 00:00
            val sqlStartDate = sqlMonthBase + TimeUnit.DAYS.toMillis(y.toLong())
            //結束點為日 23:59
            val sqlEndDate = sqlStartDate + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
            val realm = Realm.getDefaultInstance()
            val query = realm.where(AsmDataModel::class.java)
            val dataCount = (sqlEndDate - sqlStartDate) / (60 * 1000)
            Log.d("TimePeriod"+UseFor.toString(), (dataCount.toString() + "thirtySecondsCount"))
            Log.d("getRealmMonth"+UseFor.toString(), sqlStartDate.toString())
            Log.d("getRealmMonth"+UseFor.toString(), sqlEndDate.toString())
            query.between("Created_time", sqlStartDate, sqlEndDate)
            val result1 = query.findAll()
            Log.d("getRealmMonth"+UseFor.toString(), result1.size.toString())
            if (result1.size != 0) {
                var sumMonth = 0f
                for (i in result1) {
                    when(UseFor)
                    {
                        DEFINE_FRAGMENT_TVOC ->{
                            sumMonth += i.tvocValue.toInt()
                        }
                        DEFINE_FRAGMENT_ECO2 ->{
                            sumMonth += i.ecO2Value.toInt()
                        }
                        DEFINE_FRAGMENT_TEMPERATURE ->{
                            sumMonth += i.tempValue.toFloat()+10.0f
                        }
                        DEFINE_FRAGMENT_HUMIDITY->{
                            sumMonth += i.humiValue.toInt()
                        }
                    }
                }
                val aveTvoc = (sumMonth / result1.size)
                arrData.add(aveTvoc.toString())
                //依序加入時間
                arrTime.add((sqlStartDate - calObject.timeZone.rawOffset).toString())
                Log.d("getRealmMonth"+UseFor.toString(), result1.last().toString())
            } else {
                arrData.add("0")
                arrTime.add((sqlStartDate - calObject.timeZone.rawOffset).toString())
            }
        }

    }
    private fun getBarData3(inputValue: ArrayList<String>, inputTime: ArrayList<String>,positionID: Int?): BarData {
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
                result_Today!!.text = getString(R.string.text_default_value)
                result_Yesterday!!.text = getString(R.string.text_default_value)
                show_Today!!.text=getString(R.string.text_default_value)
                show_Yesterday!!.text=getString(R.string.text_default_value)
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
                result_Today!!.text = getString(R.string.text_default_value)
                result_Yesterday!!.text = getString(R.string.text_default_value)
                show_Today!!.text=getString(R.string.text_default_value)
                show_Yesterday!!.text=getString(R.string.text_default_value)
            }
        }
        Log.d("TVOCGETLABEL3" + UseFor.toString(), chartLabels.lastIndex.toString())
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
                    if (!downloadingData && !downloadComplete) {
                        getDeviceData()
                        downloadingData = true
                    }
                    val bundle = intent.extras
                    var tvocVal = "0"
                    var eco2Val = "0"
                    var tempVal = "0"
                    var humiVal = "0"
                    when (UseFor){
                        DEFINE_FRAGMENT_TVOC ->{
                            tvocVal = bundle.getString(BroadcastActions.INTENT_KEY_TVOC_VALUE)
                        }
                        DEFINE_FRAGMENT_ECO2 ->{
                            eco2Val = bundle.getString(BroadcastActions.INTENT_KEY_TVOC_VALUE)
                        }
                        DEFINE_FRAGMENT_TEMPERATURE ->{
                            tempVal = bundle.getString(BroadcastActions.INTENT_KEY_TEMP_VALUE)
                        }
                        DEFINE_FRAGMENT_HUMIDITY->{
                            humiVal = bundle.getString(BroadcastActions.INTENT_KEY_HUMI_VALUE)
                        }
                    }

                //    val humiVal = bundle.getString(BroadcastActions.INTENT_KEY_HUMI_VALUE)
                    preHeat = bundle.getString(BroadcastActions.INTENT_KEY_PREHEAT_COUNT)
                    if(preHeat == "255") {
                        //新增AnimationCount
                        animationCount++
                        counter++
                        when (UseFor){
                            DEFINE_FRAGMENT_TVOC ->{
                                valueIntAVG += tvocVal.toInt()
                            }
                            DEFINE_FRAGMENT_ECO2 ->{
                                valueIntAVG += eco2Val.toInt()
                            }
                            DEFINE_FRAGMENT_TEMPERATURE ->{
                                valueFloatAVG += tempVal.toFloat()
                            }
                            DEFINE_FRAGMENT_HUMIDITY->{
                                valueIntAVG += humiVal.toInt()
                            }
                        }

                        if (counter % getDataCycle == 0) {
                            counter = 0
                            when (UseFor){
                                DEFINE_FRAGMENT_TEMPERATURE->{
                                    valueFloatAVG /= getDataCycle
                                    valueFloatAVG = 0.0
                                }
                                else ->{
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
        for (i in chartMin.toInt()..chartMax.toInt() step chartIntervalStep)
        {
            j += 1
            chartData.add(BarEntry((i).toFloat(), j))
        }
        return chartData
    }

    private fun getLabels(): List<String> {
        val chartLabels = ArrayList<String>()
        for (i in 1 until mDataCount) {
            chartLabels.add("X" + i)
        }
        return chartLabels
    }
    private fun timePickerShow(){
        if (spinnerPositon == 0) {
            val tpd = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                val p = hourOfDay * 60 + minute
                mChart?.centerViewToAnimated(p.toFloat(), 0F, YAxis.AxisDependency.LEFT, 1000)
                //mChart?.moveViewToX((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                //        + Calendar.getInstance().get(Calendar.MINUTE) / 60F) * 118.5F) //移動視圖by x index
                val y = mChart!!.data!!.dataSetCount
                mChart?.highlightValue(p, y - 1)
            }, calObject.get(Calendar.HOUR_OF_DAY), calObject.get(Calendar.MINUTE), false)
            tpd.setMessage("請選擇時間")
            tpd.show()
        }
    }
}