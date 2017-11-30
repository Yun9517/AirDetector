package microjet.com.airqi2.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
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

    private var mContext : Context? = null

    private var mChart : FixBarChart? = null

    private var mButtonDate: Button? = null
    private var mButtonTimeStart: Button? = null
    private var mButtonTimeEnd: Button? = null
    private var mTextViewTimeRange: TextView? = null
    private var mTextViewValue: TextView? = null
    private var mSpinner : Spinner? = null
    private var DATA_COUNT : Int = 60
    private var mRadioGroup :RadioGroup? = null

    private var mHour: RadioButton? = null

    //20171124 Andy月曆的方法聆聽者
    var dateSetListener : DatePickerDialog.OnDateSetListener? = null
    var cal = Calendar.getInstance()
    var timeStartSetListener :TimePickerDialog.OnTimeSetListener? = null

    @Suppress("OverridingDeprecatedMember")
    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        mContext = this.context.applicationContext
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater!!.inflate(R.layout.frg_tvoc, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mRadioGroup=this.view?.findViewById(R.id.frg_radioGroup)
        mChart = this.view!!.findViewById(R.id.chart_line)

        mHour = this.view!!.findViewById(R.id.radioButton_Hour)

        mRadioGroup?.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, i ->
            mChart?.clear()
            when (i){
                R.id.radioButton_Hour->{
                    mChart?.data = getBarData()
                    mChart?.setVisibleXRangeMaximum(5.0f)//需要在设置数据源后生效
                    mChart?.animateX(20)
                    mChart?.moveViewToX((DATA_COUNT-1).toFloat())//移動視圖by x index
                }
                R.id.radioButton_Day->{
                    mChart?.data = getBarData()
                    mChart?.setVisibleXRangeMaximum(5.0f)//需要在设置数据源后生效
                    mChart?.moveViewToX((DATA_COUNT-1).toFloat())
                }
                R.id.radioButton_Week->{
                    mChart?.data = getBarData()
                    mChart?.setVisibleXRangeMaximum(5.0f)//需要在设置数据源后生效
                    mChart?.moveViewToX((DATA_COUNT-1).toFloat())
                }
                R.id.radioButton_Month->{
                    mChart?.data = getBarData()
                    mChart?.setVisibleXRangeMaximum(5.0f)//需要在设置数据源后生效
                    mChart?.moveViewToX((DATA_COUNT-1).toFloat())
                }
            }
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
        mTextViewValue= this.view!!.findViewById(R.id.textVSelectDetectionValue)

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

    }

    override fun onNothingSelected() {
       // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun onValueSelected(e: Entry?, dataSetIndex: Int, h: Highlight?) {
     //   TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        mTextViewValue!!.text= h!!.value.toString()+"ppb"
       // mTextViewTimeRange!!.text=h.toString()
        val listString:List<String> = getLabels()
        mTextViewTimeRange!!.text= listString[h.xIndex]
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



}
