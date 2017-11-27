package microjet.com.airqi2.Fragment

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.Toast
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
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
class TVOCFragment : Fragment() {
    private var mContext : Context? = null

    private var mChart : BarChart? = null

    private var mButtonDate: Button?=null
    private var mButtonTimeStart: Button?=null
    private var mButtonTimeEnd: Button?=null
    private var mTextViewTimeRange: TextView?=null
    private var mTextViewValue: TextView?=null

    private var DATA_COUNT : Int = 5

    //20171124 Andy月曆的方法聆聽者
    private var dateSetListener : DatePickerDialog.OnDateSetListener? = null
    private var cal = Calendar.getInstance()
    private var timeStartSetListener :TimePickerDialog.OnTimeSetListener?=null
    private var timeEndSetListener :TimePickerDialog.OnTimeSetListener?=null
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
        //20171124 Andy
        mButtonDate = this.view!!.findViewById(R.id.btnPickDate)
        mButtonTimeStart = this.view!!.findViewById(R.id.btnPickTimeStart)
        mButtonTimeEnd= this.view!!.findViewById(R.id.btnPickTimeEnd)
        mTextViewTimeRange = this.view!!.findViewById(R.id.textVSelectDetectionTime)
        mTextViewValue= this.view!!.findViewById(R.id.textVSelectDetectionValue)
        // create an OnDateSetListener
        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        timeStartSetListener =TimePickerDialog.OnTimeSetListener{_,HOUR,minute->
            cal.set(Calendar.HOUR_OF_DAY,HOUR)
            cal.set(Calendar.MINUTE,minute)
            updateStartTimeInView()
        }
        timeEndSetListener =TimePickerDialog.OnTimeSetListener{_,HOUR,minute->
            cal.set(Calendar.HOUR_OF_DAY,HOUR)
            cal.set(Calendar.MINUTE,minute)
            updateEndTimeInView()
        }
        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        mButtonDate!!.setOnClickListener {
            DatePickerDialog(activity, R.style.MyDatePickerDialogTheme,
                    dateSetListener,
                    // set DatePickerDialog to point to today's date when it loads up
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
        //    updateDateInView()
        }
        mButtonTimeStart!!.setOnClickListener{
            TimePickerDialog(activity,R.style.MyTimePickerDialogTheme,
                    timeStartSetListener,
                    cal.get(Calendar.AM_PM),
                    cal.get(Calendar.MINUTE),
                    true).show()
        }
        mButtonTimeEnd!!.setOnClickListener{
            TimePickerDialog(activity,R.style.MyTimePickerDialogTheme,
                    timeEndSetListener,
                    cal.get(Calendar.AM_PM),
                    cal.get(Calendar.MINUTE),
                    true).show()
        }

    }

    private fun updateDateInView() {
        val myFormat = "yyyy/MM/dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        mButtonDate!!.setText(sdf.format(cal.getTime()).toString())
        //  Toast.makeText(mContext,sdf.format(cal.getTime()), Toast.LENGTH_LONG).show()
    }
    private fun updateStartTimeInView() {
        val myFormat = "HH:mm" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        //Toast.makeText(mContext,sdf.format(cal.getTime()), Toast.LENGTH_LONG).show()
        mButtonTimeStart!!.setText(sdf.format(cal.getTime()).toString())
    }
    private fun updateEndTimeInView() {
        val myFormat = "HH:mm" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        //Toast.makeText(mContext,sdf.format(cal.getTime()), Toast.LENGTH_LONG).show()
        mButtonTimeEnd!!.setText(sdf.format(cal.getTime()).toString())
    }
    override fun onResume() {
        super.onResume()
        mChart!!.data = getBarData()
    }

    override fun onStop() {
        super.onStop()
    }

    private fun getBarData(): BarData {
        val dataSetA = BarDataSet(setChartData(), "LabelA")
        val dataSets = ArrayList<BarDataSet>()
        dataSets.add(dataSetA) // add the datasets
        return BarData(setLabels(), dataSets)
    }

    private fun setChartData(): List<BarEntry> {
        val DATA_COUNT = 5

        val chartData = ArrayList<BarEntry>()
        for (i in 0 until DATA_COUNT) {
            chartData.add(BarEntry((i * 2).toFloat(), i))
        }
        return chartData
    }

    private fun setLabels(): List<String> {
        val chartLabels = ArrayList<String>()
        for (i in 0 until DATA_COUNT) {
            chartLabels.add("X" + i)
        }
        return chartLabels
    }


}
