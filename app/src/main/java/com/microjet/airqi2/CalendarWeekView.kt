package com.microjet.airqi2


import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View

import com.microjet.airqi2.R


/**
 * @author airsaid
 *
 * 自定义日历顶部星期 View.
 */
class CalendarWeekView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private val mWeeks = getContext().resources.getStringArray(R.array.english_week_string_array)

    private var mTextSize: Int = 0
    private var mTextColor: Int = 0
    private var mTypeface: Typeface? = null
    /**
     * 获取 [Paint] 对象.
     * @return [Paint].
     */
    val paint: Paint
    private var mMeasureTextWidth: Float = 0.toFloat()

    init {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val a = context.obtainStyledAttributes(attrs, R.styleable.WeekView)
        val textColor = a.getColor(R.styleable.WeekView_wv_textColor, Color.BLACK)
        setTextColor(textColor)
        val textSize = a.getDimensionPixelSize(R.styleable.WeekView_wv_textSize, -1)
        setTextSize(textSize)
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        var widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        var heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        if (widthMode == View.MeasureSpec.AT_MOST) {
            widthSize = (mMeasureTextWidth * mWeeks.size).toInt() + paddingLeft + paddingRight
        }
        if (heightMode == View.MeasureSpec.AT_MOST) {
            heightSize = mMeasureTextWidth.toInt() + paddingTop + paddingBottom
        }
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mTextSize != -1) {
            paint.textSize = mTextSize.toFloat()
        }
        if (mTypeface != null) {
            paint.typeface = mTypeface
        }
        paint.style = Paint.Style.FILL
        paint.color = mTextColor
        val columnWidth = (width - paddingLeft - paddingRight) / 7
        for (i in mWeeks.indices) {
            val text = mWeeks[i]
            val fontWidth = paint.measureText(text).toInt()
            val startX = columnWidth * i + (columnWidth - fontWidth) / 2 + paddingLeft
            val startY = (height / 2 - (paint.ascent() + paint.descent()) / 2).toInt() + paddingTop
            canvas.drawText(text, startX.toFloat(), startY.toFloat(), paint)
        }
    }

    /**
     * 设置字体大小.
     *
     * @param size text size.
     */
    fun setTextSize(size: Int) {
        this.mTextSize = size
        paint.textSize = mTextSize.toFloat()
        mMeasureTextWidth = paint.measureText(mWeeks[0])
    }

    /**
     * 设置文字颜色.
     *
     * @param color 颜色.
     */
    fun setTextColor(@ColorInt color: Int) {
        this.mTextColor = color
        paint.color = mTextColor
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
}