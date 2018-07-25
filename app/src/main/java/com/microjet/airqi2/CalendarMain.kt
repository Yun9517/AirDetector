package com.microjet.airqi2


import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.microjet.airqi2.CustomAPI.CSVWriter
import com.microjet.airqi2.CustomAPI.Utils
import com.microjet.airqi2.Definition.CalendarParameter
import com.microjet.airqi2.Definition.CalendarParameter.mDayTimeStampValue
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort


import java.util.Collections
import java.util.Comparator
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale



/**
 * @author airsaid
 *
 * [CalendarView] 演示例子, 以下方法都是可选操作, 更多方法请查看 [CalendarView].
 */
class CalendarMain : AppCompatActivity() {

    private var mCalendarView: CalendarView? = null
    private var mTxtDate: TextView? = null
    private var mExport_CSV: Button? = null
    private var str_min:String = ""
    private var str_max:String = ""
    private lateinit var realm: Realm
    private lateinit var result: RealmResults<AsmDataModel>
    private lateinit var listener: RealmChangeListener<RealmResults<AsmDataModel>>
    private lateinit var filter: List<AsmDataModel>
    private lateinit var myPref: PrefObjects
    private val reqCodeWriteStorage = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.calendar_main)
        mTxtDate = findViewById(R.id.txt_date) as TextView
        mExport_CSV = findViewById(R.id.export_bt) as Button
        mCalendarView = findViewById(R.id.calendarView) as CalendarView

        myPref = PrefObjects(this)
        // 设置已选的日期
        mCalendarView!!.setSelectDate(initData())

        // 指定显示的日期, 如当前月的下个月
        var calendar:Calendar = mCalendarView!!.calendar!!
        calendar.add(Calendar.MONTH, 0)
        mCalendarView!!.calendar

        // 设置字体
        mCalendarView!!.setTypeface(Typeface.SERIF)

        // 设置日期状态改变监听
        mCalendarView!!.setOnDateChangeListener(object : CalendarView.OnDateChangeListener {
            override fun onSelectedDayChange(view: CalendarView, select: Boolean, year: Int, month: Int, day: Int) {
                Log.e(TAG, "select: $select")
                Log.e(TAG, "year: $year")
                Log.e(TAG, "month,: " + (month + 1))
                Log.e(TAG, "day: $day")

                val str: List<String>? = mCalendarView!!.getSelectDate()
                val icc = IgnoreCaseComparator()
                Collections.sort(str, icc)

                if((str?.size)!! != 0){
                    str_min = str?.get(0)
                    str_max = str?.get(str?.size -1)
                    Log.d(TAG," Date min = " + str_min)
                    Log.d(TAG," Date max = " + str_max)
                }
            }
        })
        // 设置是否能够改变日期状态
        mCalendarView!!.setChangeDateStatus(true)

        // 设置日期点击监听
        mCalendarView!!.setOnDataClickListener(object : CalendarView.OnDataClickListener {
            override fun onDataClick(view: CalendarView, year: Int, month: Int, day: Int) {
                Log.e(TAG, "year: $year")
                Log.e(TAG, "month,: " + (month + 1))
                Log.e(TAG, "day: $day")
            }
        })
        // 设置是否能够点击
        mCalendarView!!.setClickable(true)

        setCurDate()

        mExport_CSV?.setOnClickListener(View.OnClickListener {
//            Log.d(TAG," Date min = " + str_min)
//            Log.d(TAG," Date max = " + str_max)
            if (!str_min.equals("") || !str_max.equals("") ){
                if(str_max.equals(str_min)){
                    Utils.toastMakeTextAndShow(this@CalendarMain, String.format(getString(R.string.number_of_date_null)), Toast.LENGTH_SHORT)
                }
            val MintimeStamp: Int = Date2TimeStamp(str_min, "yyyyMMdd").toInt() / mDayTimeStampValue
            val MaxtimeStamp: Int = Date2TimeStamp(str_max, "yyyyMMdd").toInt() / mDayTimeStampValue
            var licit_Date: Boolean = CalendarParameter.mDayRangeValue >= (MaxtimeStamp - MintimeStamp + 1)

//            Log.d(TAG, " Date Value = " + (MaxtimeStamp - MintimeStamp + 1) + " Date lici :" + licit_Date)

                if (!licit_Date) {
                    Utils.toastMakeTextAndShow(this@CalendarMain, String.format(getString(R.string.maximum_number_of_date)), Toast.LENGTH_SHORT)
                    }else{
                        val startTime:Long = Date2TimeStamp(str_min, "yyyyMMdd").toLong() *1000
                        val endTime:Long = Date2TimeStamp(str_max, "yyyyMMdd").toLong() *1000
                    if (checkPermissions()){
                    runRealmQueryData(startTime,endTime)
                    }
                 }

            }else{
                Utils.toastMakeTextAndShow(this@CalendarMain, String.format(getString(R.string.number_of_date_null)), Toast.LENGTH_SHORT)
            }

        })


    }

    fun Date2TimeStamp(dateStr: String, format: String): String {
        try {
            val sdf = SimpleDateFormat(format)
            return (sdf.parse(dateStr).time / 1000).toString()

        } catch (e: Exception) {
            e.printStackTrace()
        }


        return ""
    }

    internal inner class IgnoreCaseComparator : Comparator<String> {
        override fun compare(strA: String, strB: String): Int {
            return strA.compareTo(strB, ignoreCase = true)
        }
    }

    private fun initData(): MutableList<String> {
        val dates = ArrayList<String>()
        val calendar = Calendar.getInstance(Locale.CHINESE)
        val sdf = SimpleDateFormat(mCalendarView!!.dateFormatPattern, Locale.CHINESE)
        sdf.format(calendar.time)
        dates.add(sdf.format(calendar.time))
        return dates
    }

    fun next(v: View) {
        mCalendarView!!.nextMonth()
        setCurDate()
    }

    fun last(v: View) {
        mCalendarView!!.lastMonth()
        setCurDate()
    }

    private fun setCurDate() {

        mTxtDate!!.setText((mCalendarView!!.year).toString() + getString(R.string.year) + " " + (mCalendarView!!.month + 1) + " " + getString(R.string.month))
    }

    // 查詢資料庫
    private fun runRealmQueryData(startTime: Long, endTime: Long) {
        realm = Realm.getDefaultInstance()
        val query = realm.where(AsmDataModel::class.java)
        //一天共有1440筆
        val dataCount = (endTime - startTime) / (60 * 1000)
        Log.d("TimePeriod", (dataCount.toString() + "Count"))
        query.between("Created_time", startTime, endTime).sort("Created_time", Sort.ASCENDING)
        val Result = query.findAll()
        Log.d("Result", Result.toString())
        listener = RealmChangeListener {
            filter = it.filter { it.macAddress == myPref.getSharePreferenceMAC() }

            parseDataToCsv(filter)
            Log.e("Realm Listener", "Update Database...")
        }

        result = realm.where(AsmDataModel::class.java)
                .between("Created_time", startTime, endTime)
                .sort("Created_time", Sort.ASCENDING).findAllAsync()

        result.addChangeListener(listener)
    }

    @SuppressLint("SimpleDateFormat")
    private fun parseDataToCsv(results: List<AsmDataModel>) {
        if (results.isNotEmpty()) {

            val folderName = "ADDWII Mobile Nose"
            val date = SimpleDateFormat("yyyyMMdd")
            val fileName = str_min+"-"+str_max+"_Mobile_Nose"
            val writeCSV = CSVWriter(folderName, fileName, CSVWriter.COMMA_SEPARATOR)

            val timeFormat = SimpleDateFormat("HH:mm")

            val header = arrayOf("id", "Time", "TVOC", "eCO2", "Temperature", "Humidity", "PM2.5")

            writeCSV.writeLine(header)

            for (i in 0 until results.size) {
                val time = results[i].created_time
                val tvocVal = if (results[i].tvocValue == "65538") "No Data" else "${results[i].tvocValue} ppb"
                val eco2Val = if (results[i].ecO2Value == "65538") "No Data" else "${results[i].ecO2Value} ppm"
                val tempVal = if (results[i].tempValue == "65538") "No Data" else "${results[i].tempValue} °C"
                val humiVal = if (results[i].humiValue == "65538") "No Data" else "${results[i].humiValue} %"
                val pm25Val = if (results[i].pM25Value == "65538") "No Data" else "${results[i].pM25Value} μg/m³"

                val textCSV = arrayOf((i + 1).toString(),timeFormat.format(time), tvocVal, eco2Val, tempVal, humiVal, pm25Val)

                writeCSV.writeLine(textCSV).toString()
            }

            writeCSV.close()
            result.removeAllChangeListeners()

            val directoryPath = android.os.Environment.getExternalStorageDirectory().toString() + "/" + folderName
            Utils.toastMakeTextAndShow(this@CalendarMain, String.format(getString(R.string.text_export_success_msg), directoryPath), Toast.LENGTH_SHORT)
        }
    }

    private fun checkPermissions(): Boolean {

        if (ActivityCompat.checkSelfPermission(this@CalendarMain, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@CalendarMain,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), reqCodeWriteStorage)
        } else {
//            runRealmQueryData()
            Log.e("CheckPerm", "Permission Granted. Starting export data...")
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            reqCodeWriteStorage -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("CheckPerm----", "Permission Granted. Starting export data...")
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }

    companion object {

        private val TAG = CalendarMain::class.java.simpleName
    }


}
