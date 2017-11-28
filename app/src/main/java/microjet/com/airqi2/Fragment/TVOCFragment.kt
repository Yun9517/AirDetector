package microjet.com.airqi2.Fragment

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
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
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

    private var mChart : BarChart? = null

    private var mButtonDate: Button?=null
    private var mButtonTimeStart: Button?=null
    private var mButtonTimeEnd: Button?=null
    private var mTextViewTimeRange: TextView?=null
    private var mTextViewValue: TextView?=null
    private var mSpinner : Spinner?=null
    private var DATA_COUNT : Int = 100

    //20171124 Andy月曆的方法聆聽者
    var dateSetListener : DatePickerDialog.OnDateSetListener? = null
    var cal = Calendar.getInstance()
    var timeStartSetListener :TimePickerDialog.OnTimeSetListener?=null

    @Suppress("OverridingDeprecatedMember")
    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        mContext = this.context.applicationContext
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater!!.inflate(R.layout.frg_tvoc, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mChart = this.view!!.findViewById(R.id.chart_line)
        mChart!!.setScaleYEnabled(false)

        mChart!!.setOnChartValueSelectedListener(this)

        mSpinner=this.view!!.findViewById(R.id.spinner)
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
                    0->{ DATA_COUNT=20
                        mChart!!.data = getBarData()
                    }
                    1->{
                        DATA_COUNT=30
                        mChart!!.data = getBarData()
                    }
                    2->{
                        DATA_COUNT=40
                        mChart!!.data = getBarData()
                    }
                    3->{
                        DATA_COUNT=50
                        mChart!!.data = getBarData()
                    }
                    else -> {

                    }
                }
            }
        }
        //20171124 Andy
    //    mButtonDate = this.view!!.findViewById(R.id.btnPickDate)
    //    mButtonTimeStart = this.view!!.findViewById(R.id.btnPickTimeStart)
        mTextViewTimeRange = this.view!!.findViewById(R.id.textVSelectDetectionTime)
        mTextViewValue= this.view!!.findViewById(R.id.textVSelectDetectionValue)
        // create an OnDateSetListener
    /*    dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
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
        */
    }

    override fun onNothingSelected() {
       // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onValueSelected(e: Entry?, dataSetIndex: Int, h: Highlight?) {
     //   TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        mTextViewValue!!.text= h!!.value.toString()
    }

    override fun onResume() {
        super.onResume()

        mChart!!.data = getBarData()
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
