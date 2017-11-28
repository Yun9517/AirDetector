package microjet.com.airqi2.CustomAPI

/**
 * Created by ray650128 on 2017/11/28.
 */

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PaintFlagsDrawFilter
import android.graphics.RectF
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import microjet.com.airqi2.R

/**
 * colorful arc progress bar
 * Created by shinelw on 12/4/15.
 */

class ColorArcProgressBar : View {
    private val DEGREE_PROGRESS_DISTANCE = dipToPx(8f)//弧形与外层刻度的距离

    private var mWidth: Int = 0
    private var mHeight: Int = 0

    private var diameter = 500  //直径
    private var centerX: Float = 0.toFloat()  //圆心X坐标
    private var centerY: Float = 0.toFloat()  //圆心Y坐标

    private var allArcPaint: Paint? = null
    private var progressPaint: Paint? = null
    private var vTextPaint: Paint? = null
    private var hintPaint: Paint? = null
    private var degreePaint: Paint? = null
    private var curSpeedPaint: Paint? = null

    private var bgRect: RectF? = null

    private var progressAnimator: ValueAnimator? = null
    private var mDrawFilter: PaintFlagsDrawFilter? = null
    private var sweepGradient: SweepGradient? = null//颜色渲染
    private var rotateMatrix: Matrix? = null

    private var colors = intArrayOf(Color.GREEN, Color.YELLOW, Color.RED, Color.RED)

    private var mTouchInvalidateRadius: Float = 0.toFloat()//触摸失效半径,控件外层都可触摸,当触摸区域小于这个值的时候触摸失效

    private val startAngle = 135f//开始角度(0°与控件X轴平行)
    private var sweepAngle = 270f//弧形扫过的区域
    private var currentAngle = 0f
    private var lastAngle: Float = 0.toFloat()

    private var maxValues = 60f
    private var currentValues = 0f
    private var bgArcWidth = dipToPx(10f).toFloat()
    private var progressWidth = dipToPx(10f).toFloat()
    private var textSize = dipToPx(60f).toFloat()
    private var hintSize = dipToPx(15f).toFloat()
    private var curSpeedSize = dipToPx(13f).toFloat()
    private val aniSpeed = 200//动画时长
    private val longDegree = dipToPx(13f).toFloat()//长刻度
    private val shortDegree = dipToPx(5f).toFloat()//短刻度


    private var longDegreeColor = -0xeeeeef
    private var shortDegreeColor = -0xeeeeef
    private var hintColor = -0x989899
    private var bgArcColor = -0xeeeeef

    private var titleString: String? = null
    private var hintString: String? = null

    private val isShowCurrentSpeed = true

    private var isNeedTitle: Boolean = false
    private var isNeedUnit: Boolean = false
    private var isNeedDial: Boolean = false
    private var isNeedContent: Boolean = false
    private val isAutoTextSize = true

    // sweepAngle / maxValues 的值
    private var k: Float = 0.toFloat()

    private var listener: OnSeekArcChangeListener? = null

    private var seekEnable: Boolean = false

    /**
     * 得到屏幕宽度
     *
     * @return
     */
    private val screenWidth: Int
        get() {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.widthPixels
        }

    constructor(context: Context) : super(context, null) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs, 0) {
        initConfig(context, attrs)
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initConfig(context, attrs)
        initView()
    }

    /**
     * 初始化布局配置
     *
     * @param context
     * @param attrs
     */
    private fun initConfig(context: Context, attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ColorArcProgressBar)

        val color1 = a.getColor(R.styleable.ColorArcProgressBar_front_color1, Color.GREEN)
        val color2 = a.getColor(R.styleable.ColorArcProgressBar_front_color2, color1)
        val color3 = a.getColor(R.styleable.ColorArcProgressBar_front_color3, color1)

        bgArcColor = a.getColor(R.styleable.ColorArcProgressBar_bg_arc_color, -0xeeeeef)
        longDegreeColor = a.getColor(R.styleable.ColorArcProgressBar_degree_color, -0xeeeeef)
        shortDegreeColor = a.getColor(R.styleable.ColorArcProgressBar_degree_color, -0xeeeeef)
        hintColor = a.getColor(R.styleable.ColorArcProgressBar_hint_color, -0x989899)

        colors = intArrayOf(color1, color2, color3, color3)

        sweepAngle = a.getInteger(R.styleable.ColorArcProgressBar_sweep_angle, 270).toFloat()
        bgArcWidth = a.getDimension(R.styleable.ColorArcProgressBar_bg_arc_width, dipToPx(10f).toFloat())
        progressWidth = a.getDimension(R.styleable.ColorArcProgressBar_front_width, dipToPx(10f).toFloat())

        seekEnable = a.getBoolean(R.styleable.ColorArcProgressBar_is_seek_enable, false)
        isNeedTitle = a.getBoolean(R.styleable.ColorArcProgressBar_is_need_title, false)
        isNeedContent = a.getBoolean(R.styleable.ColorArcProgressBar_is_need_content, false)
        isNeedUnit = a.getBoolean(R.styleable.ColorArcProgressBar_is_need_unit, false)
        isNeedDial = a.getBoolean(R.styleable.ColorArcProgressBar_is_need_dial, false)

        hintString = a.getString(R.styleable.ColorArcProgressBar_string_unit)
        titleString = a.getString(R.styleable.ColorArcProgressBar_string_title)

        currentValues = a.getFloat(R.styleable.ColorArcProgressBar_current_value, 0f)
        maxValues = a.getFloat(R.styleable.ColorArcProgressBar_max_value, 60f)

        setCurrentValues(currentValues)
        setMaxValues(maxValues)

        a.recycle()

    }

    //    @Override
    //    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    //    {
    //        int width  = (int) (2 * (longDegree + DEGREE_PROGRESS_DISTANCE) + progressWidth + diameter);
    //        int height = (int) (2 * (longDegree + DEGREE_PROGRESS_DISTANCE) + progressWidth + diameter);
    //        Log.v("ColorArcProgressBar", "onMeasure: width:"+width+" height:"+height);
    //        setMeasuredDimension(width, height);
    //    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        Log.v("ColorArcProgressBar", "onSizeChanged: mWidth:$mWidth mHeight:$mHeight")

        diameter = (Math.min(mWidth, mHeight) - 2 * (longDegree + DEGREE_PROGRESS_DISTANCE.toFloat() + progressWidth / 2)).toInt()

        Log.v("ColorArcProgressBar", "onSizeChanged: diameter:" + diameter)

        //弧形的矩阵区域
        bgRect = RectF()
        bgRect!!.top = longDegree + DEGREE_PROGRESS_DISTANCE.toFloat() + progressWidth / 2
        bgRect!!.left = longDegree + DEGREE_PROGRESS_DISTANCE.toFloat() + progressWidth / 2
        bgRect!!.right = diameter + (longDegree + progressWidth / 2 + DEGREE_PROGRESS_DISTANCE.toFloat())
        bgRect!!.bottom = diameter + (longDegree + progressWidth / 2 + DEGREE_PROGRESS_DISTANCE.toFloat())

        Log.v("ColorArcProgressBar", "initView: " + diameter)

        //圆心
        centerX = (2 * (longDegree + DEGREE_PROGRESS_DISTANCE.toFloat() + progressWidth / 2) + diameter) / 2
        centerY = (2 * (longDegree + DEGREE_PROGRESS_DISTANCE.toFloat() + progressWidth / 2) + diameter) / 2

        sweepGradient = SweepGradient(centerX, centerY, colors, null)

        mTouchInvalidateRadius = (Math.max(mWidth, mHeight) / 2).toFloat() - longDegree - DEGREE_PROGRESS_DISTANCE.toFloat() - progressWidth * 2

        if (isAutoTextSize) {
            textSize = (diameter * 0.3).toFloat()
            hintSize = (diameter * 0.1).toFloat()
            curSpeedSize = (diameter * 0.1).toFloat()

            vTextPaint!!.textSize = textSize
            hintPaint!!.textSize = hintSize
            curSpeedPaint!!.textSize = curSpeedSize
        }

    }

    private fun initView() {

        //        diameter = 3 * getScreenWidth() / 5;

        //外部刻度线画笔
        degreePaint = Paint()
        degreePaint!!.color = longDegreeColor

        //整个弧形画笔
        allArcPaint = Paint()
        allArcPaint!!.isAntiAlias = true
        allArcPaint!!.style = Paint.Style.STROKE
        allArcPaint!!.strokeWidth = bgArcWidth
        allArcPaint!!.color = bgArcColor
        allArcPaint!!.strokeCap = Paint.Cap.ROUND

        //当前进度的弧形画笔
        progressPaint = Paint()
        progressPaint!!.isAntiAlias = true
        progressPaint!!.style = Paint.Style.STROKE
        progressPaint!!.strokeCap = Paint.Cap.ROUND
        progressPaint!!.strokeWidth = progressWidth
        progressPaint!!.color = Color.GREEN

        //内容显示文字
        vTextPaint = Paint()
        //        vTextPaint.setTextSize(textSize);
        vTextPaint!!.color = Color.BLACK
        vTextPaint!!.textAlign = Paint.Align.CENTER

        //显示单位文字
        hintPaint = Paint()
        //        hintPaint.setTextSize(hintSize);
        hintPaint!!.color = hintColor
        hintPaint!!.textAlign = Paint.Align.CENTER

        //显示标题文字
        curSpeedPaint = Paint()
        //        curSpeedPaint.setTextSize(curSpeedSize);
        curSpeedPaint!!.color = hintColor
        curSpeedPaint!!.textAlign = Paint.Align.CENTER

        mDrawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        rotateMatrix = Matrix()

    }

    override fun onDraw(canvas: Canvas) {
        //抗锯齿
        canvas.drawFilter = mDrawFilter

        if (isNeedDial) {
            //画刻度线
            for (i in 0..39)
            //把整个圆划分成8大份40小份
            {
                if (i > 15 && i < 25)
                //15~25小份不需要显示
                {
                    canvas.rotate(9f, centerX, centerY)//40小份,每份9°
                    continue
                }
                if (i % 5 == 0)
                //画长刻度
                {
                    degreePaint!!.strokeWidth = dipToPx(2f).toFloat()
                    degreePaint!!.color = longDegreeColor
                    canvas.drawLine(centerX, centerY - (diameter / 2).toFloat() - progressWidth / 2 - DEGREE_PROGRESS_DISTANCE.toFloat(), centerX, centerY - (diameter / 2).toFloat() - progressWidth / 2 - DEGREE_PROGRESS_DISTANCE.toFloat() - longDegree, degreePaint!!)
                } else
                //画短刻度
                {
                    degreePaint!!.strokeWidth = dipToPx(1.4f).toFloat()
                    degreePaint!!.color = shortDegreeColor
                    canvas.drawLine(centerX, centerY - (diameter / 2).toFloat() - progressWidth / 2 - DEGREE_PROGRESS_DISTANCE.toFloat() - (longDegree - shortDegree) / 2, centerX, centerY - (diameter / 2).toFloat() - progressWidth / 2 - DEGREE_PROGRESS_DISTANCE.toFloat() - (longDegree - shortDegree) / 2 - shortDegree, degreePaint!!)
                }
                //每绘制一个小刻度,旋转1/40
                canvas.rotate(9f, centerX, centerY)
            }
        }

        //整个弧
        canvas.drawArc(bgRect!!, startAngle, sweepAngle, false, allArcPaint!!)

        //设置渐变色
        rotateMatrix!!.setRotate(130f, centerX, centerY)
        sweepGradient!!.setLocalMatrix(rotateMatrix)
        progressPaint!!.shader = sweepGradient

        //当前进度
        canvas.drawArc(bgRect!!, startAngle, currentAngle, false, progressPaint!!)

        if (isNeedContent) {
            //drawText的第三个参数代表的是基线坐标,只要x坐标、基线位置、文字大小确定以后，文字的位置就是确定的了。
            canvas.drawText(String.format("%.0f", currentValues), centerX, centerY + textSize / 4, vTextPaint!!)
        }
        if (isNeedUnit) {
            canvas.drawText(hintString!!, centerX, centerY + textSize, hintPaint!!)
        }
        if (isNeedTitle) {
            canvas.drawText(titleString!!, centerX, centerY - textSize, curSpeedPaint!!)
        }

        invalidate()

    }

    /**
     * 设置最大值
     *
     * @param maxValues
     */
    fun setMaxValues(maxValues: Float) {
        this.maxValues = maxValues
        k = sweepAngle / maxValues
    }

    /**
     * 设置当前值
     *
     * @param currentValues
     */
    fun setCurrentValues(currentValues: Float) {
        var currentValues = currentValues
        if (currentValues > maxValues) {
            currentValues = maxValues
        }
        if (currentValues < 0) {
            currentValues = 0f
        }
        this.currentValues = currentValues
        lastAngle = currentAngle
        setAnimation(lastAngle, currentValues * k, aniSpeed)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (seekEnable) {
            this.parent.requestDisallowInterceptTouchEvent(true)//一旦底层View收到touch的action后调用这个方法那么父层View就不会再调用onInterceptTouchEvent了，也无法截获以后的action

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    onStartTrackingTouch()
                    updateOnTouch(event)
                }
                MotionEvent.ACTION_MOVE -> updateOnTouch(event)
                MotionEvent.ACTION_UP -> {
                    onStopTrackingTouch()
                    isPressed = false
                    this.parent.requestDisallowInterceptTouchEvent(false)
                }
                MotionEvent.ACTION_CANCEL -> {
                    onStopTrackingTouch()
                    isPressed = false
                    this.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            return true
        }
        return false
    }


    private fun onStartTrackingTouch() {
        if (listener != null) {
            listener!!.onStartTrackingTouch(this)
        }
    }

    private fun onStopTrackingTouch() {
        if (listener != null) {
            listener!!.onStopTrackingTouch(this)
        }
    }


    private fun updateOnTouch(event: MotionEvent) {
        val validateTouch = validateTouch(event.x, event.y)
        if (!validateTouch) {
            return
        }
        isPressed = true
        val mTouchAngle = getTouchDegrees(event.x, event.y)

        val progress = angleToProgress(mTouchAngle)
        Log.v("ColorArcProgressBar", "updateOnTouch: " + progress)
        onProgressRefresh(progress, true)
    }

    /**
     * 判断触摸是否有效
     *
     * @param xPos x
     * @param yPos y
     * @return is validate touch
     */
    private fun validateTouch(xPos: Float, yPos: Float): Boolean {
        var validate = false

        val x = xPos - centerX
        val y = yPos - centerY

        val touchRadius = Math.sqrt((x * x + y * y).toDouble()).toFloat()//触摸半径

        var angle = Math.toDegrees(Math.atan2(y.toDouble(), x.toDouble()) + Math.PI / 2 - Math.toRadians(225.0))

        if (angle < 0) {
            angle = 360 + angle
        }
        //

        if (touchRadius > mTouchInvalidateRadius && angle >= 0 && angle <= 280)
        //其实角度小于270就够了,但是弧度换成角度是不精确的,所以需要适当放大范围,不然有时候滑动不到最大值
        {
            validate = true
        }

        Log.v("ColorArcProgressBar", "validateTouch: " + angle)
        return validate
    }

    private fun getTouchDegrees(xPos: Float, yPos: Float): Double {
        val x = xPos - centerX//触摸点X坐标与圆心X坐标的距离
        val y = yPos - centerY//触摸点Y坐标与圆心Y坐标的距离
        // Math.toDegrees convert to arc Angle

        //Math.atan2(y, x)以弧度为单位计算并返回点 y /x 的夹角，该角度从圆的 x 轴（0 点在其上，0 表示圆心）沿逆时针方向测量。返回值介于正 pi 和负 pi 之间。
        //触摸点与圆心的夹角- Math.toRadians(225)是因为我们希望0°从圆弧的起点开始,默认角度从穿过圆心的X轴开始
        var angle = Math.toDegrees(Math.atan2(y.toDouble(), x.toDouble()) + Math.PI / 2 - Math.toRadians(225.0))

        if (angle < 0) {
            angle = 360 + angle
        }
        Log.v("ColorArcProgressBar", "getTouchDegrees: " + angle)
        //        angle -= mStartAngle;
        return angle
    }

    private fun angleToProgress(angle: Double): Int {

        var progress = Math.round(valuePerDegree() * angle).toInt()

        progress = if (progress < 0) 0 else progress
        progress = if (progress > maxValues) maxValues.toInt() else progress
        return progress
    }

    private fun valuePerDegree(): Float {
        return maxValues / sweepAngle
    }

    private fun onProgressRefresh(progress: Int, fromUser: Boolean) {
        updateProgress(progress, fromUser)
    }

    private fun updateProgress(progress: Int, fromUser: Boolean) {

        currentValues = progress.toFloat()

        if (listener != null) {
            listener!!.onProgressChanged(this, progress, fromUser)
        }

        currentAngle = progress.toFloat() / maxValues * sweepAngle//计算划过当前的角度

        lastAngle = currentAngle

        invalidate()
    }


    /**
     * 设置整个圆弧宽度
     *
     * @param bgArcWidth
     */
    fun setArcWidth(bgArcWidth: Int) {
        this.bgArcWidth = bgArcWidth.toFloat()
    }

    /**
     * 设置进度宽度
     *
     * @param progressWidth
     */
    fun setProgressWidth(progressWidth: Int) {
        this.progressWidth = progressWidth.toFloat()
    }

    /**
     * 设置速度文字大小
     *
     * @param textSize
     */
    fun setTextSize(textSize: Int) {
        this.textSize = textSize.toFloat()
    }

    /**
     * 设置单位文字大小
     *
     * @param hintSize
     */
    fun setHintSize(hintSize: Int) {
        this.hintSize = hintSize.toFloat()
    }

    /**
     * 设置单位文字
     *
     * @param hintString
     */
    fun setUnit(hintString: String) {
        this.hintString = hintString
        invalidate()
    }

    /**
     * 设置直径大小
     *
     * @param diameter
     */
    fun setDiameter(diameter: Int) {
        this.diameter = dipToPx(diameter.toFloat())
    }

    /**
     * 设置标题
     *
     * @param title
     */
    private fun setTitle(title: String) {
        this.titleString = title
    }

    /**
     * 设置是否显示标题
     *
     * @param isNeedTitle
     */
    private fun setIsNeedTitle(isNeedTitle: Boolean) {
        this.isNeedTitle = isNeedTitle
    }

    /**
     * 设置是否显示单位文字
     *
     * @param isNeedUnit
     */
    private fun setIsNeedUnit(isNeedUnit: Boolean) {
        this.isNeedUnit = isNeedUnit
    }

    /**
     * 设置是否显示外部刻度盘
     *
     * @param isNeedDial
     */
    private fun setIsNeedDial(isNeedDial: Boolean) {
        this.isNeedDial = isNeedDial
    }

    /**
     * 为进度设置动画
     *
     * @param last
     * @param current
     */
    private fun setAnimation(last: Float, current: Float, length: Int) {
        progressAnimator = ValueAnimator.ofFloat(last, current)
        progressAnimator!!.duration = length.toLong()
        progressAnimator!!.setTarget(currentAngle)
        progressAnimator!!.addUpdateListener { animation ->
            currentAngle = animation.animatedValue as Float
            currentValues = currentAngle / k
        }
        progressAnimator!!.start()
    }

    fun setSeekEnable(seekEnable: Boolean) {
        this.seekEnable = seekEnable
    }

    fun setOnSeekArcChangeListener(listener: OnSeekArcChangeListener) {
        this.listener = listener
    }

    /**
     * dip 转换成px
     *
     * @param dip
     * @return
     */
    private fun dipToPx(dip: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dip * density + 0.5f * if (dip >= 0) 1 else -1).toInt()
    }

    interface OnSeekArcChangeListener {

        /**
         * Notification that the progress level has changed. Clients can use the
         * fromUser parameter to distinguish user-initiated changes from those
         * that occurred programmatically.
         *
         * @param seekArc  The SeekArc whose progress has changed
         * @param progress The current progress level. This will be in the range
         * 0..max where max was set by
         * [ColorArcProgressBar.setMaxValues] . (The default value for
         * max is 100.)
         * @param fromUser True if the progress change was initiated by the user.
         */
        fun onProgressChanged(seekArc: ColorArcProgressBar, progress: Int, fromUser: Boolean)

        /**
         * Notification that the user has started a touch gesture. Clients may
         * want to use this to disable advancing the SeekBar.
         *
         * @param seekArc The SeekArc in which the touch gesture began
         */
        fun onStartTrackingTouch(seekArc: ColorArcProgressBar)

        /**
         * Notification that the user has finished a touch gesture. Clients may
         * want to use this to re-enable advancing the SeekBar.
         *
         * @param seekArc The SeekArc in which the touch gesture began
         */
        fun onStopTrackingTouch(seekArc: ColorArcProgressBar)
    }
}
