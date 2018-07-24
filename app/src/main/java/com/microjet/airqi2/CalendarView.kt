package com.microjet.airqi2

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.Toast
import com.microjet.airqi2.CustomAPI.Utils


import com.microjet.airqi2.DateUtils

import java.text.SimpleDateFormat
import java.util.*

/**
 * @author airsaid
 *
 * 自定义可多选日历 View.
 */
class CalendarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                             defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    /** 默认的日期格式化格式 */
    private var DATE_FORMAT_PATTERN:String = "yyyyMMdd"
    var getSelectDate: Objects? = null

    /** 默认文字颜色  */
    private var mTextColor: Int = 0
    /** 选中后文字颜色  */
    private var mSelectTextColor: Int = 0
    /** 默认文字大小  */
    private var mTextSize: Float = 0.toFloat()
    /** 选中后文字大小  */
    private var mSelectTextSize: Float = 0.toFloat()
    /** 默认天的背景  */
    private var mDayBackground: Drawable? = null
    /** 选中后天的背景  */
    private var mSelectDayBackground: Drawable? = null
    /** */
    private var mMaxDayValue: Int = 30

    /** 日期格式化格式  */
    /**
     * 获取日期格式化格式.
     *
     * @return 格式化格式.
     */
    /**
     * 设置日期格式化格式.
     *
     * @param pattern 格式化格式, 如: yyyy-MM-dd.
     */
    var dateFormatPattern: String? = null
        set(pattern) {
            if (!TextUtils.isEmpty(pattern)) {
                field = pattern
            } else {
                field = DATE_FORMAT_PATTERN
            }
            this.mDateFormat = SimpleDateFormat(dateFormatPattern!!, Locale.CHINA)
        }
    /** 字体  */
    private var mTypeface: Typeface? = null
    /** 日期状态是否能够改变  */
    /**
     * 获取是否能改变日期状态.
     *
     * @return [.mIsChangeDateStatus].
     */
    /**
     * 设置点击是否能够改变日期状态 (默认或选中状态).
     *
     * 默认是 false, 即点击只会响应点击事件 [OnDataClickListener], 日期状态而不会做出任何改变.
     *
     * @param isChanged 是否能改变日期状态.
     */

    /** 日期状态是否能够改变  */
    private var mIsChangeDateStatus: Boolean = false

    /** 每列宽度  */
    private var mColumnWidth: Int = 0
    /** 每行高度  */
    private var mRowHeight: Int = 0
    /** 已选择日期数据  */
    private var mSelectDate: MutableList<String>? = null
    /** 存储对应列行处的天  */
    private val mDays = Array(6) { IntArray(7) }

    private var mOnDataClickListener: OnDataClickListener? = null
    private var mChangeListener: OnDateChangeListener? = null
    private var mDateFormat: SimpleDateFormat? = null
    private val mSelectCalendar: Calendar
    private var mCalendar: Calendar? = null
    /**
     * 获取 [Paint] 对象.
     * @return [Paint].
     */
    val paint: Paint
    private val mSlop: Int

    private var mDownX = 0
    private var mDownY = 0

    /**
     * 获取选中的日期数据.
     *
     * @return 日期数据.
     */
    /**
     * 设置选中的日期数据.
     *
     * @param days 日期数据, 日期格式为 [.setDateFormatPattern] 方法所指定,
     * 如果没有设置则以默认的格式 [.DATE_FORMAT_PATTERN] 进行格式化.
     */

    fun setSelectDate(days: MutableList<String>?) {
        this.mSelectDate = days
        invalidate()
    }
    /**
     * 获取当前年份.
     *
     * @return year.
     */
    val year: Int
        get() = mCalendar!!.get(Calendar.YEAR)

    /**
     * 获取当前月份.
     *
     * @return month. (思考后, 决定这里直接按 Calendar 的 API 进行返回, 不进行 +1 处理)
     */
    val month: Int
        get() = mCalendar!!.get(Calendar.MONTH)

//    fun setCalendar(calendar: Calendar) {
//        this.mCalendar = calendar
//        invalidate()
//    }


    /**
     * 获取当前显示的 Calendar 对象.
     *
     * @return Calendar 对象.
     */
    /**
     * 设置当前显示的 Calendar 对象.
     *
     * @param calendar 对象.
     */
    var calendar: Calendar?
        get() = mCalendar
        set(calendar) {
            this.mCalendar = calendar
            invalidate()
        }

    interface OnDataClickListener {

        /**
         * 日期点击监听.
         * @param view     与次监听器相关联的 View.
         * @param year     对应的年.
         * @param month    对应的月.
         * @param day      对应的日.
         */
        fun onDataClick(view: CalendarView, year: Int, month: Int, day: Int)
    }

    interface OnDateChangeListener {

        /**
         * 选中的天发生了改变监听回调, 改变有 2 种, 分别是选中和取消选中.
         * @param view     与次监听器相关联的 View.
         * @param select   true 表示是选中改变, false 是取消改变.
         * @param year     对应的年.
         * @param month    对应的月.
         * @param day      对应的日.
         */
        fun onSelectedDayChange(view: CalendarView, select: Boolean, year: Int, month: Int, day: Int)
    }

    init {
        mSlop = ViewConfiguration.get(context).scaledTouchSlop
        mSelectCalendar = Calendar.getInstance(Locale.CHINA)
        mCalendar = Calendar.getInstance(Locale.CHINA)
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        mSelectDate = ArrayList()
        isClickable = true
        initAttrs(attrs)
        Log.d("barney","CalendarView start")
    }

    private fun initAttrs(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CalendarView)

        val textColor = a.getColor(R.styleable.CalendarView_cv_textColor, Color.BLACK)
        setTextColor(textColor)

        val selectTextColor = a.getColor(R.styleable.CalendarView_cv_selectTextColor, Color.BLACK)
        setSelectTextColor(selectTextColor)

        val textSize = a.getDimension(R.styleable.CalendarView_cv_textSize, sp2px(14f).toFloat())
        setTextSize(textSize)

        val selectTextSize = a.getDimension(R.styleable.CalendarView_cv_selectTextSize, sp2px(14f).toFloat())
        setSelectTextSize(selectTextSize)

        val dayBackground = a.getDrawable(R.styleable.CalendarView_cv_dayBackground)
        setDayBackground(dayBackground)

        val selectDayBackground = a.getDrawable(R.styleable.CalendarView_cv_selectDayBackground)
        setSelectDayBackground(selectDayBackground)

        val pattern = a.getString(R.styleable.CalendarView_cv_dateFormatPattern)
        dateFormatPattern = pattern

        val isChange = a.getBoolean(R.styleable.CalendarView_cv_isChangeDateStatus, false)

        setChangeDateStatus(isChange)

        a.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mColumnWidth = width / 7
        mRowHeight = height / 6
        paint.textSize = mTextSize

        val year = mCalendar!!.get(Calendar.YEAR)
        // 获取的月份要少一月, 所以这里 + 1
        val month = mCalendar!!.get(Calendar.MONTH) + 1
        // 获取当月的天数
        val days = DateUtils.getMonthDays(year, month)
        // 获取当月第一天位于周几
        val week = DateUtils.getFirstDayWeek(year, month)
        // 绘制每天
        for (day in 1..days) {
            // 获取天在行、列的位置
            val column = (day + week - 1) % 7
            val row = (day + week - 1) / 7
            // 存储对应天
            mDays[row][column] = day

            val dayStr = day.toString()
            val textWidth = paint.measureText(dayStr)
            val x = (mColumnWidth * column + (mColumnWidth - textWidth) / 2).toInt()
            val y = (mRowHeight * row + mRowHeight / 2 - (paint.ascent() + paint.descent()) / 2).toInt()

            // 判断 day 是否在选择日期内

                if (mSelectDate == null || mSelectDate!!.size == 0 ||
                        !mSelectDate!!.contains(getFormatDate(year, month - 1, day))) {
                    // 没有则绘制默认背景和文字颜色
                    drawBackground(canvas, mDayBackground, column, row)
                    drawText(canvas, dayStr, mTextColor, mTextSize, x, y)
                } else {
                    // 否则绘制选择后的背景和文字颜色
                    drawBackground(canvas, mSelectDayBackground, column, row)
                    drawText(canvas, dayStr, mSelectTextColor, mSelectTextSize, x, y)
                }

        }
    }

    private fun drawBackground(canvas: Canvas, background: Drawable?, column: Int, row: Int) {
        if (background != null) {
            canvas.save()
            val dx = mColumnWidth * column + mColumnWidth / 2 - background.intrinsicWidth / 2
            val dy = mRowHeight * row + mRowHeight / 2 - background.intrinsicHeight / 2
            canvas.translate(dx.toFloat(), dy.toFloat())
            background.draw(canvas)
            canvas.restore()
        }
    }

    private fun drawText(canvas: Canvas, text: String, @ColorInt color: Int, size: Float, x: Int, y: Int) {
        paint.color = color
        paint.textSize = size
        if (mTypeface != null) {
            paint.typeface = mTypeface
        }
        canvas.drawText(text, x.toFloat(), y.toFloat(), paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isClickable) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

                mDownX = event.x.toInt()
                mDownY = event.y.toInt()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP -> {
                    val upX:Int = event.x.toInt()
                    val upY:Int = event.y.toInt()
                    val diffX:Int = Math.abs(upX - mDownX)
                    val diffY:Int = Math.abs(upY - mDownY)
                    if (diffX < mSlop && diffY < mSlop) {
                        val column:Int = upX / mColumnWidth
                        val row:Int = upY / mRowHeight
                        onClick(mDays[row][column])
                    }
                }
        }

        return super.onTouchEvent(event)
    }

    private fun onClick(day: Int) {
        if (day < 1) {
            return
        }

        val year:Int = mCalendar!!.get(Calendar.YEAR)
        val month:Int = mCalendar!!.get(Calendar.MONTH)
        if (mOnDataClickListener != null) {
            mOnDataClickListener!!.onDataClick(this, year, month, day)
        }

        if (mIsChangeDateStatus) {
            // 如果选中的天已经选择则取消选中
            if(mSelectDate!!.size <= 3){
                Log.d("Barney", "mIsChangeDateStatus ::: mSelectDate =  " + (mSelectDate) + "  " + isClickable)
            }
            val date:String = getFormatDate(year, month, day)
            if (mSelectDate != null && (mSelectDate!!.contains(date) || mSelectDate!!.size >= mMaxDayValue)) {
                mSelectDate!!.remove(date)
                if (mChangeListener != null) {
                    mChangeListener!!.onSelectedDayChange(this, false, year, month, day)
                }
                if(mSelectDate!!.size >= mMaxDayValue) {
//                    Utils.toastMakeTextAndShow(this@CalendarView, resources.getString(R.string.maximum_number_of_date),Toast.LENGTH_SHORT)
                }
            } else {
                if (mSelectDate == null) {
                    mSelectDate = ArrayList()
                }
                mSelectDate!!.add(date)
                if (mChangeListener != null) {
                    mChangeListener!!.onSelectedDayChange(this, true, year, month, day)
                }
            }
            invalidate()
        }
    }


    /**
     * 获取选中的日期数据.
     *
     * @return 日期数据.
     */

    fun getSelectDate(): MutableList<String>? {
        return mSelectDate
    }

    /**
     * 切换到下一个月.
     */
    fun nextMonth() {
        mCalendar!!.add(Calendar.MONTH, 1)
        invalidate()
    }

    /**
     * 切换到上一个月.
     */
    fun lastMonth() {
        mCalendar!!.add(Calendar.MONTH, -1)
        invalidate()
    }

    /**
     * 设置文字颜色.
     *
     * @param textColor 文字颜色 [ColorInt].
     */
    fun setTextColor(@ColorInt textColor: Int) {
        this.mTextColor = textColor
    }

    /**
     * 设置选中后的的文字颜色.
     *
     * @param textColor 文字颜色 [ColorInt].
     */
    fun setSelectTextColor(@ColorInt textColor: Int) {
        this.mSelectTextColor = textColor
    }

    /**
     * 设置文字大小.
     *
     * @param textSize 文字大小 (sp).
     */
    fun setTextSize(textSize: Float) {
        this.mTextSize = textSize
    }

    /**
     * 设置选中后的的文字大小.
     *
     * @param textSize 文字大小 (sp).
     */
    fun setSelectTextSize(textSize: Float) {
        this.mSelectTextSize = textSize
    }

    /**
     * 设置天的背景.
     *
     * @param background 背景 drawable.
     */
    fun setDayBackground(background: Drawable?) {
        if (background != null && mDayBackground !== background) {
            this.mDayBackground = background
            setCompoundDrawablesWithIntrinsicBounds(mDayBackground)
        }
    }

    /**
     * 设置选择后天的背景.
     *
     * @param background 背景 drawable.
     */
    fun setSelectDayBackground(background: Drawable?) {
        if (background != null && mSelectDayBackground !== background) {
            this.mSelectDayBackground = background
            setCompoundDrawablesWithIntrinsicBounds(mSelectDayBackground)
        }
    }

    /**
     * 设置字体.
     *
     * @param typeface [Typeface].
     */
    fun setTypeface(typeface: Typeface) {
        this.mTypeface = typeface
        invalidate()
    }

    /**
     * 设置日期点击监听.
     *
     * @param listener 被通知的监听器.
     */
    fun setOnDataClickListener(listener: OnDataClickListener) {
        this.mOnDataClickListener = listener
    }

    /**
     * 设置选中日期改变监听器.
     *
     * @param listener 被通知的监听器.
     */
    fun setOnDateChangeListener(listener: OnDateChangeListener) {
        this.mChangeListener = listener
    }

    /**
     * 设置点击是否能够改变日期状态 (默认或选中状态).
     *
     * 默认是 false, 即点击只会响应点击事件 [OnDataClickListener], 日期状态而不会做出任何改变.
     *
     * @param isChanged 是否能改变日期状态.
     */
    fun setChangeDateStatus(isChanged: Boolean) {
        this.mIsChangeDateStatus = isChanged
    }
    /**
     * 根据指定的年月日按当前日历的格式格式化后返回.
     *
     * @param year  年.
     * @param month 月.
     * @param day   日.
     * @return 格式化后的日期.
     */
    fun getFormatDate(year: Int, month: Int, day: Int): String {
        mSelectCalendar.set(year, month, day)
        return mDateFormat!!.format(mSelectCalendar.time)
    }

    private fun setCompoundDrawablesWithIntrinsicBounds(drawable: Drawable?) {
        drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    }

    private fun sp2px(spVal: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.resources.displayMetrics).toInt()
    }

    companion object {

        /** 默认的日期格式化格式  */
        private val DATE_FORMAT_PATTERN = "yyyyMMdd"
        var getSelectDate: Any? = null
    }
}


