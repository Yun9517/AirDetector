package microjet.com.airqi2.Fragment

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.BarChart
import microjet.com.airqi2.CustomAPI.ColorArcProgressBar

import microjet.com.airqi2.R
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry





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

    private var DATA_COUNT : Int = 5

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
    }

    override fun onResume() {
        super.onResume()

        mChart!!.data = getBarData()
    }

    override fun onStop() {
        super.onStop()
    }

    private fun getBarData(): BarData {
        val dataSetA = BarDataSet(getChartData(), "LabelA")

        val dataSets = ArrayList<BarDataSet>()
        dataSets.add(dataSetA) // add the datasets

        return BarData(getLabels(), dataSets)
    }

    private fun getChartData(): List<BarEntry> {
        val DATA_COUNT = 5

        val chartData = ArrayList<BarEntry>()
        for (i in 0 until DATA_COUNT) {
            chartData.add(BarEntry((i * 2).toFloat(), i))
        }
        return chartData
    }

    private fun getLabels(): List<String> {
        val chartLabels = ArrayList<String>()
        for (i in 0 until DATA_COUNT) {
            chartLabels.add("X" + i)
        }
        return chartLabels
    }
}
