package com.microjet.airqi2


import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast


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
    private val calObject = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Barney "," CalendarMain Start =")
        setContentView(R.layout.calendar_main)
        mTxtDate = findViewById(R.id.txt_date) as TextView
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

                Log.d("Barney str = ", str.toString())
//                if (select) {
//                    Toast.makeText(applicationContext, "选了：" + year + "年" + (month + 1) + "月" + day + "日", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(applicationContext, "取消了：" + year + "年" + (month + 1) + "月" + day + "日", Toast.LENGTH_SHORT).show()
//                }
                Collections.sort(str, icc)
                Log.d("Barney", " sort " + str.toString())
                Log.d("Barney", " str = " + str?.size)

                if((str?.size)!! != 0){
                    Log.d("Barney"," str min  = " + str?.get(0))
                    Log.d("Barney", " str max = " + str?.get(str?.size -1))
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
