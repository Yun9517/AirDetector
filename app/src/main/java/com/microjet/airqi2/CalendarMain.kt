package com.microjet.airqi2


import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.microjet.airqi2.CustomAPI.Utils
import com.microjet.airqi2.Definition.CalendarParameter
import com.microjet.airqi2.Definition.CalendarParameter.mDayTimeStampValue



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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.calendar_main)
        mTxtDate = findViewById(R.id.txt_date) as TextView
        mExport_CSV = findViewById(R.id.export_bt) as Button
        mCalendarView = findViewById(R.id.calendarView) as CalendarView


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

            val MintimeStamp:Int = Date2TimeStamp(str_min, "yyyyMMdd").toInt()/mDayTimeStampValue
            val MaxtimeStamp:Int = Date2TimeStamp(str_max, "yyyyMMdd").toInt()/mDayTimeStampValue
            var licit_Date:Boolean = CalendarParameter.mDayRangeValue >= (MaxtimeStamp - MintimeStamp + 1 )

            Log.d(TAG," Date Value = " + (MaxtimeStamp - MintimeStamp + 1) + " Date lici :"+licit_Date)

            if (!licit_Date){
                Utils.toastMakeTextAndShow(this@CalendarMain, String.format(getString(R.string.maximum_number_of_date)), Toast.LENGTH_SHORT)
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
        //        Log.e(TAG, "Date: " + mCalendarView.getYear() +" "+ getString(R.string.year) +" "+ (mCalendarView.getMonth() + 1) +" "+ getString(R.string.month));
        mTxtDate!!.setText((mCalendarView!!.year).toString() + getString(R.string.year) + " " + (mCalendarView!!.month + 1) + " " + getString(R.string.month))
    }

    companion object {

        private val TAG = CalendarMain::class.java.simpleName
    }


}
