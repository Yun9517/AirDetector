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

    private var mButtonDate: Button? = null
    private var mButtonTimeStart: Button? = null
    private var mButtonTimeEnd: Button? = null
    private var mTextViewTimeRange: TextView? = null
    private var mTextViewValue: TextView? = null
    private var mSpinner: Spinner? = null
    private var DATA_COUNT: Int = 60
    private var mRadioGroup: RadioGroup? = null

    private var mHour: RadioButton? = null
    private var mMyCharData: ArrayList<String>? = null
    private var mMyCharLabel: ArrayList<String>? = null
    //20171124 Andy月曆的方法聆聽者
    var dateSetListener: DatePickerDialog.OnDateSetListener? = null
    var cal = Calendar.getInstance()
    var timeStartSetListener: TimePickerDialog.OnTimeSetListener? = null

    @Suppress("OverridingDeprecatedMember")
    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        mContext = this.context.applicationContext
    }


    //20171130   Andy SQLlite
    internal lateinit var dbrw: SQLiteDatabase
    internal lateinit var dbhelper: AndyAirDBhelper
    internal var tablename = "Andyairtable"


    internal var colstT = arrayOf("編號", "時間", "溫度", "濕度", "揮發", "二氧")// };
    internal var columT = arrayOf("_id", "collection_time", "temper", "hum", "tvoc", "co2")//,"CO2"};
    internal var co10T = "_id"
    internal var co11T = "collection_time"
    internal var co12T = "temper"
    internal var co13T = "hum"
    internal var co14T = "tvoc"
    internal var co15T = "co2"
    //20171204   Andy 嘗試用SQL語法用時間查資料
    internal var colum_collection_time = arrayOf("2017/11/30 20:20:20")//,"CO2"};
    internal var colum_tvoc = arrayOf("tvoc")//,"CO2"};

    internal var coTTDBTEST = ""
    internal var SaveToDB = arrayOf("2017/11/30 20:20:20", "100", "200", "300", "400")
    internal var idTTDB: Long = 4
    internal var c: Cursor? = null
    internal var cv: ContentValues? = null
    internal var IDID = ""
    internal var Count: Long = 0
    internal var idTTDBStr = ""
    //20171130   Andy SQLlite
    internal var values: ContentValues? = null
    var list = ArrayList<ArrayList<String>>()
    var bigTimeData = ArrayList<ArrayList<String>>()
    var smallTimeData = ArrayList<ArrayList<String>>()

    //TestValue Start chungyen
    val tvocArray = ArrayList<String>()
    val timeArray = ArrayList<String>()
    val batteryArray = ArrayList<String>()
    var mProgressBar: ProgressBar? = null
    private var mImageViewDataUpdate: ImageView? = null
    var radioButtonID = mRadioGroup?.getCheckedRadioButtonId()


    fun setRealTimeBarData(Tvoc: String, Battery: String) {
        val sdFormat = SimpleDateFormat("MM/dd HH:mm:ss", Locale.TAIWAN)
        val date = Date()
        sdFormat.format(date)
        timeArray.add(sdFormat.format(date))
        tvocArray.add(Tvoc)
        batteryArray.add(Battery)
        //    setFisrtChooseChartTimeLableAndData()
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

    //TestValue End chungyen

    /**
     *
     * @param conncetStatus for BlueTooth Connect Status
     * true for connect
     * false for disconnect
     * //@return
     */
    var mConnectStatus: Boolean = false

    fun setCurrentConnectStatusIcon(conncetStatus: Boolean) {
        mConnectStatus = conncetStatus
        if (conncetStatus) {
            mImageViewDataUpdate?.setImageResource(R.drawable.chart_update_icon_connect)
            mImageViewDataUpdate?.isEnabled = conncetStatus
        } else {
            mImageViewDataUpdate?.setImageResource(R.drawable.chart_update_icon_disconnect)
            //    mImageViewDataUpdate?.setImageDrawable(resources.getDrawable(R.drawable.chart_update_icon_disconnect))
            //    mImageViewDataUpdate?.background = resources.getDrawable(R.drawable.chart_update_icon_bg)
            mImageViewDataUpdate?.isEnabled = conncetStatus
        }
    }

    fun getDeviceData() {
        val mainactivity: MainActivity = (getActivity() as MainActivity)
        mainactivity.loadDeviceData()
    }

    fun setProgessBarMax(input: Int) {
        mProgressBar?.progress = 0
        mProgressBar?.max = input
    }

    fun setProgessBarNow(input: Int) {
        mProgressBar?.progress = input
    }

    fun afterGetDeviceData() {
        mImageViewDataUpdate?.isEnabled = mConnectStatus
    }

    fun setFisrtChooseChartTimeLableAndData() {
        mTextViewValue?.text = tvocArray.get(tvocArray.size - 1) + "ppb"
        mTextViewTimeRange?.text = timeArray.get(timeArray.size - 1)
        //    mTextViewTimeRange?.text = mChart?.xAxis?.values?.get(h.xIndex)//listString[h.xIndex]
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater!!.inflate(R.layout.frg_tvoc, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dbhelper = AndyAirDBhelper(mContext)
        dbrw = dbhelper.writableDatabase
        //  Toast.makeText(mContext, AndyAirDBhelper.database18 + "資料庫是否建立?" + dbrw.isOpen + "版本" + dbrw.version, Toast.LENGTH_LONG).show()


        //20171201  Andy 一次取資料庫


        //AddedSQLlite(60000)
        //SearchSQLlite()

        mRadioGroup = this.view?.findViewById(R.id.frg_radioGroup)
        mChart = this.view!!.findViewById(R.id.chart_line)
        mProgressBar = this.view!!.findViewById(R.id.chartDataLoading)
        mHour = this.view!!.findViewById(R.id.radioButton_Hour)
        mImageViewDataUpdate = this.view?.findViewById(R.id.chart_Refresh)
        mImageViewDataUpdate?.background = resources.getDrawable(R.drawable.chart_update_icon_bg)
        mImageViewDataUpdate?.setOnClickListener {
            if (!isFastDoubleClick){
            //mImageViewDataUpdate?.isEnabled =false
            getDeviceData()
                Log.d("TVOC","TOAST_ON")
            }
        }
        mRadioGroup?.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, i ->
            mChart?.clear()
            when (i) {
                R.id.radioButton_Hour -> {
                    //   mChart?.data = getBarData()
                    //20171130   Andy使用傳統SQL語法新增資料
                    //    val dbHelper = AndyAirDBhelper(mContext, tablename, null, 1)
                    //得到一个可写的数据库
                    //       val db = dbHelper.getReadableDatabase()
                    //insertDB(db)
                    mChart?.data = getBarData2(tvocArray, timeArray)
                    // SearchSQLlite()
                    //  mChart?.data = SearchSQLlite_Day()
                }
                R.id.radioButton_Day -> {
                    getRealmFour(4)
                    mChart?.data = getBarData3(tvoc3, time3)
                }
                R.id.radioButton_Week -> {
                    //mChart?.data = getBarData()
                    getRealmFour(8)
                    mChart?.data = getBarData3(tvoc3, time3)
                }
                R.id.radioButton_Month -> {

                    //mChart?.data = getBarData()
                    getRealmFour(12)
                    mChart?.data = getBarData3(tvoc3, time3)

                }
            }
            mChart?.setVisibleXRangeMinimum(5.0f)
            mChart?.setVisibleXRangeMaximum(5.0f)//需要在设置数据源后生效
            mChart?.moveViewToX((100).toFloat())//移動視圖by x index
            //   Toast.makeText(mContext,i.toString(),Toast.LENGTH_SHORT).show()
        })

        mHour!!.isChecked = true
        mChart?.data = getBarData2(tvocArray, timeArray)

        configChartView()

        mChart!!.setOnChartValueSelectedListener(this)
        /*   mSpinner=this.view!!.findViewById(R.id.spinner)
      //  ArrayAdapter<String>(this,R.layout.spinner_layout,conversionsadd);
        val aAdapter = ArrayAdapter.createFromResource(this.mContext, R.array.SpinnerArray, R.layout.spinner_layout)
        mSpinner!!.adapter=aAdapter
        mSpinner!!.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
               // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                mChart!!.clear()
                when(position){
                    0-> {
                        mChart!!.data = getBarData()
                        mChart!!.setVisibleXRangeMaximum(5.0f)//需要在设置数据源后生效
                        mChart!!.animateX(20)
                    //  mChart!!.setVisibleXRangeMinimum(3.0f);//设置最少数量，不常用。
                    }
                    1-> {
                        mChart!!.data = getBarData()
                        mChart!!.setVisibleXRangeMaximum(5.0f)//需要在设置数据源后生效
                    //    mChart!!.setVisibleXRangeMinimum(3.0f);//设置最少数量，不常用。
                    }
                    2-> {

                        mChart!!.data = getBarData()
                        mChart!!.setVisibleXRangeMaximum(5.0f)//需要在设置数据源后生效
                    //    mChart!!.setVisibleXRangeMinimum(3.0f);//设置最少数量，不常用。
                    }
                    3-> {
                        mChart!!.data = getBarData()
                        mChart!!.setVisibleXRangeMaximum(5.0f)//需要在设置数据源后生效
                    //    mChart!!.setVisibleXRangeMinimum(3.0f);//设置最少数量，不常用。
                    }
                    else -> {

                    }
                }
            }
        }
        */
        //20171124 Andy


        //mButtonDate = this.view!!.findViewById(R.id.btnPickDate)


        //20171127 師傅
        mTextViewTimeRange = this.view!!.findViewById(R.id.textVSelectDetectionTime)
        mTextViewValue = this.view!!.findViewById(R.id.textVSelectDetectionValue)

/*
=======
    //    mButtonDate = this.view!!.findViewById(R.id.btnPickDate)
    //    mButtonTimeStart = this.view!!.findViewById(R.id.btnPickTimeStart)
        mTextViewTimeRange = this.view!!.findViewById(R.id.textVSelectDetectionTime)
        mTextViewValue= this.view!!.findViewById(R.id.textVSelectDetectionValue)
>>>>>>> ce2de6c56129ed28ce2a2dae19d268bd50fff9bd
        // create an OnDateSetListener
        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        mButtonDate!!.setOnClickListener {
            DatePickerDialog(activity, R.style.MyDatePickerDialogTheme,
                    dateSetListener,
                    // set DatePickerDialog to point to today's date when it loads up
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
        }
<<<<<<< HEAD

        //20171127 Andy開始小時月曆
        //mButtonTimeStart = this.view!!.findViewById(R.id.btnPickDate)
        //mButtonTimeStart = this.view!!.findViewById(R.id.btnPickTimeStart)


        //20171127 Andy
        mButtonTimeStart = this.view!!.findViewById(R.id.btnPickTimeStart)


        //20171127  師傅
        mTextViewTimeRange = this.view!!.findViewById(R.id.textVSelectDetectionTime)
        //20171127  師傅
        mTextViewValue= this.view!!.findViewById(R.id.textVSelectDetectionValue)


        //20171127   Andy開始時間月曆
        // create an OnDateSetListener
        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        mButtonTimeStart!!.setOnClickListener {
            showTimePickerDialog()
        }
    }

    fun showTimePickerDialog() {
        mCalendar = Calendar.getInstance()
        val dialog = TimePickerDialog(activity, TimePickerDialog.OnTimeSetListener() { timePicker: TimePicker, i: Int, i1: Int ->
                mCalendar!!.set(Calendar.HOUR, i)
                mCalendar!!.set(Calendar.MINUTE, i1)

                val format: SimpleDateFormat = SimpleDateFormat("yyyy年MM月dd日HH:mm")
                Toast.makeText(mContext, "" + format.format(mCalendar!!.getTime()), Toast.LENGTH_SHORT).show()

        }, mCalendar!!.get(Calendar.HOUR), mCalendar!!.get(Calendar.MINUTE), true)
        dialog.show()
        */
        //20171128 Andy SQL
        //*********************************************************************************************
// ------------------------------------------------------------------------------------------------------------------------------------------------

        dbhelper = AndyAirDBhelper(mContext)
        dbrw = dbhelper.writableDatabase
        //   Toast.makeText(mContext, AndyAirDBhelper.database18 + "資料庫是否建立?" + dbrw.isOpen + "版本" + dbrw.version, Toast.LENGTH_LONG).show()
        //SearchSQLlite()


        //20171128 Andy SQL
        //*********************************************************************************************
// ------------------------------------------------------------------------------------------------------------------------------------------------

        mImageViewDataUpdate?.setImageResource(R.drawable.chart_update_icon_disconnect)

    }

    override fun onNothingSelected() {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    }


    override fun onValueSelected(e: Entry?, dataSetIndex: Int, h: Highlight?) {
        //   TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        mTextViewValue!!.text = h!!.value.toString() + "ppb"
        // mTextViewTimeRange!!.text=h.toString()
        // val listString: List<String> = getLabels2()
        mTextViewTimeRange!!.text = mChart?.xAxis?.values?.get(h.xIndex)//listString[h.xIndex]
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(mContext!!).registerReceiver(mGattUpdateReceiver, makeMainFragmentUpdateIntentFilter())
        //視Radio id畫圖
        when (radioButtonID) {
            0 -> {
                mChart?.data = getBarData2(tvocArray, timeArray)
                mChart?.setVisibleXRangeMinimum(5.0f)
                mChart?.setVisibleXRangeMaximum(5.0f)
            }
            1, 2, 3 -> {
                mChart?.data = getBarData3(tvoc3, time3)
                mChart?.setVisibleXRangeMinimum(5.0f)
                mChart?.setVisibleXRangeMaximum(5.0f)
            }
        }

        //mChart!!.data = getBarData()
        //mChart?.setVisibleXRangeMaximum(5.0f)
    }

    override fun onPause() {
        try {
            LocalBroadcastManager.getInstance(mContext!!).unregisterReceiver(mGattUpdateReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onPause()
    }

    private fun cleanTextViewInTVOC() {
        mTextViewValue?.text = ""
        mTextViewTimeRange?.text = ""
    }

    override fun onStop() {
        super.onStop()
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


    private fun getBarData(): BarData {
        val dataSetA = MyBarDataSet(getChartData(), "TVOC")
        dataSetA.setColors(intArrayOf(ContextCompat.getColor(context, R.color.progressBarStartColor),
                ContextCompat.getColor(context, R.color.progressBarMidColor),
                ContextCompat.getColor(context, R.color.progressBarEndColor)))

        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(dataSetA) // add the datasets

        return BarData(getLabels(), dataSets)
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

    private fun getChartData(): List<BarEntry> {
        // val DATA_COUNT = 5
        // DATA_COUNT
        val chartData = ArrayList<BarEntry>()
        for (i in 1 until DATA_COUNT) {
            chartData.add(BarEntry((i * 20).toFloat(), i))
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

    private fun getLabels(): List<String> {
        val chartLabels = ArrayList<String>()
        for (i in 1 until DATA_COUNT) {
            chartLabels.add("X" + i)
        }
        return chartLabels
    }

    private fun setLabels() {}
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

    //20171124 Andy日期月曆
    private fun updateDateInView() {
        val myFormat = "yyyy/MM/dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        mButtonDate!!.setText(sdf.format(cal.getTime()).toString())
        //  Toast.makeText(mContext,sdf.format(cal.getTime()), Toast.LENGTH_LONG).show()
    }

    //20171127 Andy開始時間日期月曆
    private fun updateStartTimeInView() {
        val myFormat = "yyyy/MM/dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        mButtonDate!!.setText(sdf.format(cal.getTime()).toString())
        //  Toast.makeText(mContext,sdf.format(cal.getTime()), Toast.LENGTH_LONG).show()
    }

    //20171130 Andy SQL
    private fun SearchSQLlite() {
        //****************************************************************************************************************************************************
//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //查詢CO2資料
        //查詢CO2資料
        //查詢CO2資料
        c = dbrw.query(tablename, columT, null, null, null, null, null)

        //Toast.makeText(MainActivity.this, "現在位置:"+c.getPosition(), 3000).show();
        //Toast.makeText(MainActivity.this, "現在ColumnIndex:"+ c.getString(c.getColumnIndex(columT[0])), 3000).show();


        // 排版
        co10T += colstT[0] + "\n";
        co11T += colstT[1] + "\n";
        co12T += colstT[2] + "\n";
        co13T += colstT[3] + "\n";
        co14T += colstT[4] + "\n"
        co15T += colstT[5] + "\n"

        if (c!!.getCount() > 0) {
            //Toast.makeText(MainActivity.this, "測試是否有進去!!  " + c.getCount() + "筆紀錄",Toast.LENGTH_LONG).show();
            c!!.moveToFirst()

            for (i in 0 until c!!.getCount()) {
                //Toast.makeText(this@MainActivity, "測試是否進For!!  " + c!!.getCount() + "第" + i + "筆紀錄", Toast.LENGTH_LONG).show()
                //co10T += c!!.getString(c!!.getColumnIndex(columT[0])) + "\n"
                //co11T += c!!.getString(c!!.getColumnIndex(columT[1])) + "\n"
                //co12T += c!!.getString(c!!.getColumnIndex(columT[2])) + "\n"
                //co13T += c!!.getString(c!!.getColumnIndex(columT[3])) + "\n"
                //co14T += c!!.getString(c!!.getColumnIndex(columT[4])) + "\n"
                // sqlite比較不嚴僅，都用getString()取值即可
                //co10T += c!!.getString(0) + "\n"
                //co11T += c!!.getString(1) + "\n"
                //co12T += c!!.getString(2) + "\n"
                //co13T += c!!.getString(3) + "\n"
                //co14T += c!!.getString(4) + "\n"
                //Toast.makeText(this@MainActivity, "增資料庫CO2第 [ " + (i + 1) + " ]筆CO2:" + c!!.getString(0 + 1) +"ppm", Toast.LENGTH_LONG).show()

                Count = c!!.getCount().toLong()
                //c.close();
                val CountString = Count.toString()
                //    Toast.makeText(mContext, "共有" + CountString + "筆紀錄，第[" + (i + 1) + "]筆資料內容", Toast.LENGTH_LONG).show()
/*
                Toast.makeText(mContext, "資料庫ID第 [ " + (i + 1) + " ]筆: NO" + c!!.getString(0) + "\n"
                        + "資料庫時間第 [ " + (i + 1) + " ]筆:" + c!!.getString(1) + " \n"
                        + "資料庫溫度第 [ " + (i + 1) + " ]筆:" + c!!.getString(2) + "C \n"
                        + "資料庫濕度第 [ " + (i + 1) + " ]筆:" + c!!.getString(3) + "% \n"
                        + "資料庫CO2第 [ " + (i + 1) + " ]筆:" + c!!.getString(4) + "ppm \n"
                        + "資料庫TVOC第 [ " + (i + 1) + " ]筆:" + c!!.getString(5) + "ppb", Toast.LENGTH_LONG).show()
*/
                c!!.moveToNext()
            }

        } else {
            Toast.makeText(mContext, "資料庫查無資料", Toast.LENGTH_LONG).show()
        }
    }

    //20171130 Andy 傳統SQL寫法ADD
    private fun insertDB(db: SQLiteDatabase) {

//        //var ID = "1"
//
//        //先行定義時間格式
//
//        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
//
//        //取得現在時間
//
//        val dt = Date()
//
////透過SimpleDateFormat的format方法將Date轉為字串
//
//        val dts = sdf.format(dt)
//
//
//        var COLLECTION_TIME = dts.toString()
//        var TEMPER = "100"
//        var HUM = "100"
//        var TVOC = "60000"
//        var CO2 = "60000"
//
//        //插入数据SQL语句
//        val insertDB_String = "insert into Andyairtable(collection_time,temper,hum,tvoc,co2) values('$COLLECTION_TIME','100','100','60000','60000')"
//        //执行SQL语句
//        db.execSQL(insertDB_String)
//        Log.e("時", "插入完成: ")
//
//
//        c = dbrw.query(tablename, columT, null, null, null, null, null)
//        //c!!.getCount()
//        Count = c!!.getCount().toLong()
//        //c.close();
//        val CountString = Count.toString()
//
//        Toast.makeText(mContext, "插入完成", Toast.LENGTH_LONG).show()
//        Toast.makeText(mContext, "資料庫共:" + CountString + "筆", Toast.LENGTH_LONG).show()

        //  AddedSQLlite(6000)
    }

    //20171130 Andy 傳統SQL寫法ADD
    var time1 = ArrayList<String>()
    var tvoc1 = ArrayList<String>()
    public fun ADDDATAForDatachart(Datalist: java.util.ArrayList<myData>): BarData {
        var time = ArrayList<String>()
        var tvoc = ArrayList<String>()
        time1 = time
        tvoc1 = tvoc
        for (i in 0 until Datalist.size) {
            time.add(Datalist[i].time)
            tvoc.add(Datalist[i].tvoC_Data)
        }
        getLabels2(time)
        return getBarData2(tvoc, time)
        // getChartData2(tvoc)

    }

    //20171128 Andy SQL
    public fun AddedSQLlite(Datalist: java.util.ArrayList<myData>) {
        //////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        values = ContentValues()
        c = dbrw.query(tablename, columT, null, null, null, null, null)
        var mydata: myData = Datalist.get(0)


        //先行定義時間格式
        // val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

        //取得現在時間

        // val dt = Date()

        //透過SimpleDateFormat的format方法將Date轉為字串

        //val dts = sdf.format(dt)


        Count = c!!.getCount().toLong()
        //c.close();
        var CountString = Count.toString()

        Toast.makeText(mContext, "資料庫共:" + CountString + "筆", Toast.LENGTH_LONG).show()

        //c!!.getCount()
        //idTTDB = c!!.getCount().toLong()
        //Toast.makeText(this,"我要查比數:"+idTTDB,Toast.LENGTH_LONG).show()

        // SaveToDB[0]=dts
        //  if (SaveToDB[0] !== "" && SaveToDB[1] !== "" && SaveToDB[2] !== "" && SaveToDB[3] !== "" && SaveToDB[4] !== "" && idTTDB >= 0) {//****************************************************************************
        //          Toast.makeText(mContext, "資料滿5筆，我將要存到資料庫去!!!!!", Toast.LENGTH_LONG).show()
        //cv.put(columT[0],c.getPosition());
        for (i in 0 until Datalist.size) {
            values!!.put(columT[1], Datalist[i].time)//時間
            values!!.put(columT[2], Datalist[i].temperatur_Data)//溫度
            values!!.put(columT[3], Datalist[i].humidy_Data)//humidity
            values!!.put(columT[4], Datalist[i].cO2_Data)//C02
            values!!.put(columT[5], Datalist[i].tvoC_Data)//TVOC
            idTTDB = dbrw.insert(tablename, null, values)
        }
        //新增一筆五個測項資料到資料庫中

        //    Toast.makeText(mContext, "資料滿5，這筆資料內容:" + SaveToDB[0] + "," + SaveToDB[1] + "," + SaveToDB[2] + "," + SaveToDB[3] + "," + "," + SaveToDB[4], Toast.LENGTH_LONG).show()
        // } else {
        //     Toast.makeText(mContext, "時間、溫度、濕度、TVOC、CO2未滿，不新增資料庫", Toast.LENGTH_LONG).show()
        //  }
        //   Toast.makeText(mContext, "插入完成", Toast.LENGTH_LONG).show()

        Count = c!!.getCount().toLong() + 1
        //c.close();
        CountString = Count.toString()


        //   Toast.makeText(mContext, "資料庫共:" + CountString + "筆", Toast.LENGTH_LONG).show()
        //新增一筆四個測項資料到資料庫中
        //SearchSQLlite_Day()
//////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////
    }

    //20171130 Andy SQL
    private fun SearchSQLlite_Day(): BarData {
        //****************************************************************************************************************************************************
//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //查詢CO2資料
        //查詢CO2資料
        //查詢CO2資料

        // var idTTDBStr = ""
        //internal var Count: Long = 0
        //20171204   Andy 嘗試用SQL語法用時間查資料
        colum_collection_time = arrayOf("2017/11/30 , 2017/11/30 20:20:26 ")//,"CO2"};
        var Time: String = "2017/11/30 20:20:20 , "
        var TimeReuge = arrayOf(Time + colum_collection_time)
        //internal var c: Cursor? = null

        val literals = arrayOf("2017/12/4 20:20:20", "2017/11/30 20:20:26")
        val str = "collection_time >=? AND collection_time <=?"
        //ContactValues Time = new ContactValues();
        //Time = colum_collection_time[0]
        //c = dbrw.query(tablename, columT, columT[1] + ">? AND  <?", literals, null, null, null)
        c = dbrw.query(tablename, columT, str, literals, null, null, null)
        //Toast.makeText(MainActivity.this, "現在位置:"+c.getPosition(), 3000).show();
        //Toast.makeText(MainActivity.this, "現在ColumnIndex:"+ c.getString(c.getColumnIndex(columT[0])), 3000).show();


        // 排版
//        co10T += colstT[0] + "\n";
//        co11T += colstT[1] + "\n";
//        co12T += colstT[2] + "\n";
//        co13T += colstT[3] + "\n";
//        co14T += colstT[4] + "\n"
//        co15T += colstT[5] + "\n"

        if (c!!.getCount() > 0) {
            //Toast.makeText(MainActivity.this, "測試是否有進去!!  " + c.getCount() + "筆紀錄",Toast.LENGTH_LONG).show();
            c!!.moveToFirst()
            //c!!.moveToLast()
            for (i in 0 until c!!.getCount()) {
                //Toast.makeText(this@MainActivity, "測試是否進For!!  " + c!!.getCount() + "第" + i + "筆紀錄", Toast.LENGTH_LONG).show()
                //co10T += c!!.getString(c!!.getColumnIndex(columT[0])) + "\n"
                //co11T += c!!.getString(c!!.getColumnIndex(columT[1])) + "\n"
                //co12T += c!!.getString(c!!.getColumnIndex(columT[2])) + "\n"
                //co13T += c!!.getString(c!!.getColumnIndex(columT[3])) + "\n"
                //co14T += c!!.getString(c!!.getColumnIndex(columT[4])) + "\n"
                // sqlite比較不嚴僅，都用getString()取值即可
                //co10T += c!!.getString(0) + "\n"
                //co11T += c!!.getString(1) + "\n"
                //co12T += c!!.getString(2) + "\n"
                //co13T += c!!.getString(3) + "\n"
                //co14T += c!!.getString(4) + "\n"
                //Toast.makeText(this@MainActivity, "增資料庫CO2第 [ " + (i + 1) + " ]筆CO2:" + c!!.getString(0 + 1) +"ppm", Toast.LENGTH_LONG).show()

                Count = c!!.getCount().toLong()
                //c.close();
                val CountString = Count.toString()
                //Toast.makeText(mContext, "共有" + CountString + "筆紀錄，第[" + (i + 1) + "]筆資料內容", Toast.LENGTH_LONG).show()

//                Toast.makeText(mContext, "資料庫ID第 [ " + (i + 1) + " ]筆: NO" + c!!.getString(0) + "\n"
//                        + "資料庫時間第 [ " + (i + 1) + " ]筆:" + c!!.getString(1) + " \n"
//                        + "資料庫溫度第 [ " + (i + 1) + " ]筆:" + c!!.getString(2) + "C \n"
//                        + "資料庫濕度第 [ " + (i + 1) + " ]筆:" + c!!.getString(3) + "% \n"
//                        + "資料庫CO2第 [ " + (i + 1) + " ]筆:" + c!!.getString(4) + "ppm \n"
//                        + "資料庫TVOC第 [ " + (i + 1) + " ]筆:" + c!!.getString(5) + "ppb", Toast.LENGTH_LONG).show()


                var DateTimeList = ArrayList<String>()
                DateTimeList.add(c!!.getString(1))
                DateTimeList.add(c!!.getString(5))
                DateTimeList.clone()
                list.add(DateTimeList)

                c!!.moveToNext()

            }
        } else {
            Toast.makeText(mContext, "資料庫查無資料", Toast.LENGTH_LONG).show()
        }


        //
        var randTime: Int = 15 * 30 * 1000
        return Getcount(c!!.getCount().toInt(), randTime, list)
    }


    //20171201 Andy SQL取依造時間間隔中的資料筆數
    private fun Getcount(AllDataConut: Int, randTime: Int, rangeTimeData: ArrayList<ArrayList<String>>): BarData {
        var eCount: Int = 0

        //取依造時間間隔中的資料筆數
        //先行定義時間格式
        //val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        //取得現在時間
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        var nowS = sdf.format(Date()).toString()
        var now = sdf.parse(nowS).time
        var tempTime = ArrayList<String>()
        var tempTVOC = ArrayList<String>()
        //var now=nowS.time

        //var past=sdf.parse(str1).time


        //for (i in 0 until AllDataConut){

        //(0 until AllDataConut).forEach { i ->

        (AllDataConut - 1 downTo 0).forEach { j ->
            //var DataValue=ArrayList<ArrayList<String>>()

            var past = sdf.parse(rangeTimeData[j][0]).time
            //歷史資料
            var temp2: String? = null
            //Toast.makeText(mContext, "共有" + past.toString(), Toast.LENGTH_LONG).show()
            if ((now - past) <= 900000000000) {
                val formatter = SimpleDateFormat("yyyy/MM/dd hh:mm:ss", Locale.TAIWAN)
                var Date = formatter.parse(rangeTimeData[j][0])
                val sdf2 = SimpleDateFormat("HH:mm:ss", Locale.TAIWAN)

                var rangeTimeData2 = sdf2.format(Date)

                tempTime.add(rangeTimeData2)
                tempTVOC.add(rangeTimeData[j][1])
                //smallTimeData.add(temp)
                //   temp2=rangeTimeData[j][0]

                //smallTimeData[j][0]=rangeTimeData[j][0]
                //smallTimeData[j][1]=rangeTimeData[j][1]
                //var list=temp
                // Toast.makeText(mContext, "符合的資料時間" + temp2, Toast.LENGTH_SHORT).show()
            }

        }
        //}
        //}
        //val date = format.parse(rangeTime[])


        return getBarData2(tempTVOC, tempTime)
    }


    //20171201 Andy SQL取依造時間間隔中的資料筆數平均
    private fun AVGData(DataConut: Int, rangeTime: ArrayList<ArrayList<String>>): ArrayList<ArrayList<String>> {
        var DataAVGArray = ArrayList<ArrayList<String>>()

        //取依造時間間隔中的資料筆數平均

        return DataAVGArray
    }


    //    //20171130 Andy SQL
    private fun SearchSQLlite_ByTime(): BarData {
        //****************************************************************************************************************************************************
//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //查詢CO2資料
        //查詢CO2資料
        //查詢CO2資料
        c = dbrw.query(tablename, null, null, colum_collection_time, null, null, null)
        //c=dbrw.query(tablename, null, selection, selectionArgs, null, null, Data.ORDER_BY);

        //Toast.makeText(MainActivity.this, "現在位置:"+c.getPosition(), 3000).show();
        //Toast.makeText(MainActivity.this, "現在ColumnIndex:"+ c.getString(c.getColumnIndex(columT[0])), 3000).show();


        // 排版
//    co10T += colstT[0] + "\n";
//    co11T += colstT[1] + "\n";
//    co12T += colstT[2] + "\n";
//    co13T += colstT[3] + "\n";
//    co14T += colstT[4] + "\n"
//    co15T += colstT[5] + "\n"

        if (c!!.getCount() > 0) {
            //Toast.makeText(MainActivity.this, "測試是否有進去!!  " + c.getCount() + "筆紀錄",Toast.LENGTH_LONG).show();
            c!!.moveToFirst()
            //c!!.moveToLast()
            for (i in 0 until c!!.getCount()) {
                //Toast.makeText(this@MainActivity, "測試是否進For!!  " + c!!.getCount() + "第" + i + "筆紀錄", Toast.LENGTH_LONG).show()
                //co10T += c!!.getString(c!!.getColumnIndex(columT[0])) + "\n"
                //co11T += c!!.getString(c!!.getColumnIndex(columT[1])) + "\n"
                //co12T += c!!.getString(c!!.getColumnIndex(columT[2])) + "\n"
                //co13T += c!!.getString(c!!.getColumnIndex(columT[3])) + "\n"
                //co14T += c!!.getString(c!!.getColumnIndex(columT[4])) + "\n"
                // sqlite比較不嚴僅，都用getString()取值即可
                //co10T += c!!.getString(0) + "\n"
                //co11T += c!!.getString(1) + "\n"
                //co12T += c!!.getString(2) + "\n"
                //co13T += c!!.getString(3) + "\n"
                //co14T += c!!.getString(4) + "\n"
                //Toast.makeText(this@MainActivity, "增資料庫CO2第 [ " + (i + 1) + " ]筆CO2:" + c!!.getString(0 + 1) +"ppm", Toast.LENGTH_LONG).show()

                Count = c!!.getCount().toLong()
                //c.close();
                val CountString = Count.toString()
                //Toast.makeText(mContext, "共有" + CountString + "筆紀錄，第[" + (i + 1) + "]筆資料內容", Toast.LENGTH_LONG).show()

//                Toast.makeText(mContext, "資料庫ID第 [ " + (i + 1) + " ]筆: NO" + c!!.getString(0) + "\n"
//                        + "資料庫時間第 [ " + (i + 1) + " ]筆:" + c!!.getString(1) + " \n"
//                        + "資料庫溫度第 [ " + (i + 1) + " ]筆:" + c!!.getString(2) + "C \n"
//                        + "資料庫濕度第 [ " + (i + 1) + " ]筆:" + c!!.getString(3) + "% \n"
//                        + "資料庫CO2第 [ " + (i + 1) + " ]筆:" + c!!.getString(4) + "ppm \n"
//                        + "資料庫TVOC第 [ " + (i + 1) + " ]筆:" + c!!.getString(5) + "ppb", Toast.LENGTH_LONG).show()


                var DateTimeList = ArrayList<String>()
                DateTimeList.add(c!!.getString(1))
                DateTimeList.add(c!!.getString(5))
                DateTimeList.clone()
                list.add(DateTimeList)

                c!!.moveToNext()

            }
        } else {
            Toast.makeText(mContext, "資料庫查無資料", Toast.LENGTH_LONG).show()
        }


        //
        var randTime: Int = 15 * 30 * 1000
        return Getcount(c!!.getCount().toInt(), randTime, list)
    }

    //試Realm拉資料
    var time3 = ArrayList<String>()
    var tvoc3 = ArrayList<String>()

    private fun getRealmFour(hour: Int) {
        time3.clear()
        tvoc3.clear()
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
            //            when (nowHourRemainer) {
//                0 -> { endTime = ((Date().time/3600000) - nowHourRemainer - (hour*(y-1))) * 3600000 }
//                1 -> { endTime = ((Date().time/3600000) - nowHourRemainer - (hour*(y-1))) * 3600000 }
//                2 -> { endTime = ((Date().time/3600000) - nowHourRemainer - (hour*(y-1))) * 3600000 }
//                3 -> { endTime = ((Date().time/3600000) - nowHourRemainer - (hour*(y-1))) * 3600000 }
//            }
            //var endTime = Date().time - 60 * 60 * 1000 * (hour*(y-1))
            //var startTime = Date().time - 60 * 60 * 1000 * (hour*y)
            query.between("Created_time", startTime, endTime)
            //query.lessThan("Created_time",Date().time).greaterThan("Created_time",countTime)
            var result1 = query.findAll()
            if (result1.size != 0) {
                var sumTvoc = 0
                for (i in result1) {
                    sumTvoc += i.tvocValue.toInt()
                }
                var aveTvoc = (sumTvoc / result1.size)
                Log.d("getRealmFour", result1.last().toString())
                tvoc3.add(aveTvoc.toString())
                time3.add(endTime.toString())
            } else {
                tvoc3.add("0")
                time3.add(endTime.toString())
            }


            //realm.executeTransaction { result1.deleteAllFromRealm() }
        }
        //tvoc3.add(i.tvocValue.toString())
        //time3.add(i.created_time.toString())

        tvoc3.reverse()
        time3.reverse()

        //顯示手機資料庫總筆數
        /*
        var realm = Realm.getDefaultInstance()
        val query = realm.where(AsmDataModel::class.java)
        query.lessThan("Created_time", Date().time)
        var result2 = query.findAll()
        Toast.makeText(context,result2.size.toString(),Toast.LENGTH_SHORT).show()
        */
    }

    private fun nothing() {
        var realm = Realm.getDefaultInstance()
        val query = realm.where(AsmDataModel::class.java)
        for (y in 10..1) {
            var countTime = Date().time - 60 * 60 * 1000 * (y)
            //query.between("Created_time",countTime,Date().time)
            query.lessThan("Created_time", countTime)//.greaterThan("Created_time",countTime)
            val result1 = query.findAll()
            var sumTvoc = 0
            var sumTime: Long = 0
            for (i in result1) {
                sumTvoc += i.tvocValue.toInt()
                sumTime += i.created_time.toLong()
                if (result1.size != 0) {
                    tvoc3.add((sumTvoc / result1.size).toString())
                    //time3.add((sumTime / result1.size).toString())
                }
            }
        }
        //tvoc3.add(i.tvocValue.toString())
        //time3.add(i.created_time.toString())
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
        //val chartLabels = ArrayList<String>()

//        if (input.size < DATA_COUNT - 1) {
//            for (i in 0 until input.size) {
//                chartLabels.add(input[i])
//            }
//        } else {
//            for (i in 0 until DATA_COUNT - 1) {
//                chartLabels.add(input[i])
//            }
//        }

        val chartLabels = ArrayList<String>()
        val dateFormat = SimpleDateFormat("MM/dd HH:mm")
        for (i in 0 until time3.size) {
            val date = dateFormat.format(input[i].toLong())
            chartLabels.add(date)
        }
        chartLabels.removeAt(time3.size - 1)
        chartLabels.add(time3.size - 1,dateFormat.format(Date().time))
        Log.d("TVOCGETLABEL3", chartLabels.lastIndex.toString())
        return chartLabels
    }

    private fun getChartData3(input: ArrayList<String>): List<BarEntry> {
        // val DATA_COUNT = 5
        // DATA_COUNT
//        val chartData = ArrayList<BarEntry>()
//        if (input.size < DATA_COUNT - 1) {
//            for (i in 0 until input.size) {
//                chartData.add(BarEntry(input[i].toFloat(), i))
//            }
//        } else {
//            for (i in 0 until DATA_COUNT - 1) {
//                chartData.add(BarEntry(input[i].toFloat(), i))
//            }
//        }
        val chartData = ArrayList<BarEntry>()
        for (i in 0 until time3.size) {
            chartData.add(BarEntry(input[i].toFloat(), i))
        }
        return chartData
    }

    fun startUpdateDataAnimation() {
        val operatingAnim: Animation = AnimationUtils.loadAnimation(mContext, R.anim.tip)
        val lin = LinearInterpolator()
        operatingAnim.interpolator = lin
        mImageViewDataUpdate?.startAnimation(operatingAnim)
        mImageViewDataUpdate?.isEnabled = false
    }

    fun stopUpdateDataAnimation() {
        mImageViewDataUpdate?.clearAnimation()
        mImageViewDataUpdate?.isEnabled = true
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
                    setCurrentConnectStatusIcon(false)
                    stopUpdateDataAnimation()
                    setProgessBarNow(0)
                }
                BroadcastActions.ACTION_GATT_CONNECTED -> {
                    //執行連線後的事
                    counter = 0
                    setCurrentConnectStatusIcon(true)
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
                    //setBar1CurrentValue(tempVal,humiVal,tvocVal,co2Val,"0")
                    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                    val date = Date()

                    counter++
                    TVOCAVG += tvocVal.toInt()
                    if (counter % 15 == 0) {
                        counter = 0
                        TVOCAVG /= 15
                        setRealTimeBarData(TVOCAVG.toString(), BatteryLife)
                        TVOCAVG = 0

                        //   setProgressBarValue(tempVal, humiVal, tvocVal, co2Val, "0")
                    }
                }
            }
        }
    }
}


