package microjet.com.airqi2.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import microjet.com.airqi2.AndyAirDBhelper
import microjet.com.airqi2.CustomAPI.FixBarChart
//import com.github.mikephil.charting.utils.Highlight
import microjet.com.airqi2.CustomAPI.MyBarDataSet
import microjet.com.airqi2.R
import java.text.SimpleDateFormat
import java.util.*


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
    internal var co10T = ""
    internal var co11T = ""
    internal var co12T = ""
    internal var co13T = ""
    internal var co14T = ""
    internal var co15T = ""

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

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater!!.inflate(R.layout.frg_tvoc, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dbhelper = AndyAirDBhelper(mContext)
        dbrw = dbhelper.writableDatabase
        Toast.makeText(mContext, AndyAirDBhelper.database18 + "資料庫是否建立?" + dbrw.isOpen + "版本" + dbrw.version, Toast.LENGTH_LONG).show()
        //AddedSQLlite(60000)
        //SearchSQLlite()

        mRadioGroup = this.view?.findViewById(R.id.frg_radioGroup)
        mChart = this.view!!.findViewById(R.id.chart_line)

        mHour = this.view!!.findViewById(R.id.radioButton_Hour)

        mRadioGroup?.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, i ->
            mChart?.clear()
            when (i) {
                R.id.radioButton_Hour -> {
                    mChart?.data = getBarData()
                    //20171130   Andy使用傳統SQL語法新增資料
                    val dbHelper = AndyAirDBhelper(mContext, tablename, null, 1)
                    //得到一个可写的数据库
                    val db = dbHelper.getReadableDatabase()
                    insertDB(db)
                    //SearchSQLlite()
                }
                R.id.radioButton_Day -> {
                    mChart?.data = getBarData()
                }
                R.id.radioButton_Week -> {
                    mChart?.data = getBarData()
                }
                R.id.radioButton_Month -> {

                    mChart?.data = getBarData()

                }
            }
            mChart?.setVisibleXRangeMinimum(5.0f)
            mChart?.setVisibleXRangeMaximum(5.0f)//需要在设置数据源后生效
            mChart?.moveViewToX((DATA_COUNT - 1).toFloat())//移動視圖by x index
            //   Toast.makeText(mContext,i.toString(),Toast.LENGTH_SHORT).show()
        })

        mHour!!.isChecked = true

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
        Toast.makeText(mContext, AndyAirDBhelper.database18 + "資料庫是否建立?" + dbrw.isOpen + "版本" + dbrw.version, Toast.LENGTH_LONG).show()
        //SearchSQLlite()


        //20171128 Andy SQL
        //*********************************************************************************************
// ------------------------------------------------------------------------------------------------------------------------------------------------


    }

    override fun onNothingSelected() {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun onValueSelected(e: Entry?, dataSetIndex: Int, h: Highlight?) {
        //   TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        mTextViewValue!!.text = h!!.value.toString() + "ppb"
        // mTextViewTimeRange!!.text=h.toString()
        val listString: List<String> = getLabels()
        mTextViewTimeRange!!.text = listString[h.xIndex]
    }

    override fun onResume() {
        super.onResume()

        mChart!!.data = getBarData()
        mChart?.setVisibleXRangeMaximum(5.0f)
    }

    override fun onStop() {
        super.onStop()
    }

    private fun getBarData(): BarData {
        val dataSetA = MyBarDataSet(getChartData(), "LabelA")
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

    // 20171128 Added by Raymond
    private fun configChartView() {
        val xAxis: XAxis = mChart!!.xAxis
        val leftAxis: YAxis = mChart!!.axisLeft
        val rightAxis: YAxis = mChart!!.axisRight

        mChart!!.isScaleXEnabled = false
        mChart!!.isScaleYEnabled = false

        xAxis.position = XAxis.XAxisPosition.BOTTOM

        leftAxis.setDrawLabels(true) // no axis labels
        leftAxis.setDrawAxisLine(false) // no axis line
        leftAxis.setDrawGridLines(false) // no grid lines

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
                Toast.makeText(mContext, "共有" + CountString + "筆紀錄，第[" + (i + 1) + "]筆資料內容", Toast.LENGTH_LONG).show()

                Toast.makeText(mContext, "資料庫ID第 [ " + (i + 1) + " ]筆: NO" + c!!.getString(0) + "\n"
                        + "資料庫時間第 [ " + (i + 1) + " ]筆:" + c!!.getString(1) + " \n"
                        + "資料庫溫度第 [ " + (i + 1) + " ]筆:" + c!!.getString(2) + "C \n"
                        + "資料庫濕度第 [ " + (i + 1) + " ]筆:" + c!!.getString(3) + "% \n"
                        + "資料庫CO2第 [ " + (i + 1) + " ]筆:" + c!!.getString(4) + "ppm \n"
                        + "資料庫TVOC第 [ " + (i + 1) + " ]筆:" + c!!.getString(5) + "ppb", Toast.LENGTH_LONG).show()

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

        AddedSQLlite(6000)
    }
    //20171130 Andy 傳統SQL寫法ADD


    //20171128 Andy SQL
    private fun AddedSQLlite(intData: Int) {
        //////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        values = ContentValues()
        c = dbrw.query(tablename, columT, null, null, null, null, null)

        //先行定義時間格式

        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

        //取得現在時間

        val dt = Date()

        //透過SimpleDateFormat的format方法將Date轉為字串

        val dts = sdf.format(dt)


        Count = c!!.getCount().toLong()
        //c.close();
        var CountString = Count.toString()

        Toast.makeText(mContext, "資料庫共:" + CountString + "筆", Toast.LENGTH_LONG).show()

        //c!!.getCount()
        //idTTDB = c!!.getCount().toLong()
        //Toast.makeText(this,"我要查比數:"+idTTDB,Toast.LENGTH_LONG).show()

        SaveToDB[0]=dts
        if (SaveToDB[0] !== "" && SaveToDB[1] !== "" && SaveToDB[2] !== "" && SaveToDB[3] !== "" && SaveToDB[4] !== "" && idTTDB >= 0) {//****************************************************************************
                Toast.makeText(mContext, "資料滿5筆，我將要存到資料庫去!!!!!", Toast.LENGTH_LONG).show()
                //cv.put(columT[0],c.getPosition());
                values!!.put(columT[1], SaveToDB[0])
                values!!.put(columT[2], SaveToDB[1])
                values!!.put(columT[3], SaveToDB[2])
                values!!.put(columT[4], SaveToDB[3])
                values!!.put(columT[5], SaveToDB[4])
            //新增一筆五個測項資料到資料庫中
            idTTDB = dbrw.insert(tablename, null, values)
            Toast.makeText(mContext, "資料滿5，這筆資料內容:" + SaveToDB[0] + "," + SaveToDB[1] + "," + SaveToDB[2] + "," + SaveToDB[3] + "," + "," + SaveToDB[4], Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(mContext, "時間、溫度、濕度、TVOC、CO2未滿，不新增資料庫", Toast.LENGTH_LONG).show()
        }
        Toast.makeText(mContext, "插入完成", Toast.LENGTH_LONG).show()

        Count = c!!.getCount().toLong()+1
        //c.close();
        CountString = Count.toString()


        Toast.makeText(mContext, "資料庫共:" + CountString + "筆", Toast.LENGTH_LONG).show()
        //新增一筆四個測項資料到資料庫中
        SearchSQLlite()
//////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////
    }

    //20171130 Andy SQL
    private fun SearchSQLlite_Day() {
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
            //c!!.moveToFirst()
            c!!.moveToLast()
            for (i in c!!.getCount() downTo 0) {
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
                Toast.makeText(mContext, "共有" + CountString + "筆紀錄，第[" + (i + 1) + "]筆資料內容", Toast.LENGTH_LONG).show()

                Toast.makeText(mContext, "資料庫ID第 [ " + (i + 1) + " ]筆: NO" + c!!.getString(0) + "\n"
                        + "資料庫時間第 [ " + (i + 1) + " ]筆:" + c!!.getString(1) + " \n"
                        + "資料庫溫度第 [ " + (i + 1) + " ]筆:" + c!!.getString(2) + "C \n"
                        + "資料庫濕度第 [ " + (i + 1) + " ]筆:" + c!!.getString(3) + "% \n"
                        + "資料庫CO2第 [ " + (i + 1) + " ]筆:" + c!!.getString(4) + "ppm \n"
                        + "資料庫TVOC第 [ " + (i + 1) + " ]筆:" + c!!.getString(5) + "ppb", Toast.LENGTH_LONG).show()


                c!!.moveToPrevious()
            }

        } else {
            Toast.makeText(mContext, "資料庫查無資料", Toast.LENGTH_LONG).show()
        }
    }

}
