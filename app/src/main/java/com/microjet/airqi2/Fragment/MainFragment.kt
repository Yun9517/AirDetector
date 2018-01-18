package com.microjet.airqi2.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.frg_main.*
import com.microjet.airqi2.CustomAPI.ColorArcProgressBar
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.R
import java.text.SimpleDateFormat
import java.util.*
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan

class MainFragment : Fragment() {

    enum class DetectionData(val range1: Long,val range2: Long) {
        TVOC(220,660),
        CO2(700,1000),
        Temp(18,27),
        Humi(40,60)
    }

    private var mContext: Context? = null
    private var bar1: ColorArcProgressBar? = null
    private var tvThreadHold1: TextView? = null
    private var tvThreadHold2: TextView? = null
    private var tvInCycleTitle: TextView? = null
    private var tvInCycleValue: TextView? = null
    private var tvInCycleState: TextView? = null
    private var tvNotify: TextView? = null

    private var tvLastDetecteTime:TextView? = null
    //private var pressed="TVOC"//0=temperature 1=humidity 2=TVOC 3=CO2

    private var tvocDataFloat = 0f
    private var tempDataFloat = 0f
    private var humiDataFloat = 0f
    private var co2DataFloat = 0f
    private var preHeat = "0"

    private var dataForState = DetectionData.TVOC
    private var mConnState = false


    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        mContext = this.context.applicationContext
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(mContext!!).registerReceiver(myBroadcastReceiver,
                makeMainFragmentUpdateIntentFilter())
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?{
        return inflater!!.inflate(R.layout.frg_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?){
        super.onActivityCreated(savedInstanceState)
        bar1 = this.view!!.findViewById(R.id.tvocBar)
        tvThreadHold1 = this.view!!.findViewById(R.id.tvRange1)
        tvThreadHold2 = this.view!!.findViewById(R.id.tvRange2)
        tvInCycleTitle = this.view!!.findViewById(R.id.inCircleTitle)
        tvInCycleValue = this.view!!.findViewById(R.id.inCircleValue)
        tvInCycleState = this.view!!.findViewById(R.id.inCircleState)
        tvNotify = this.view!!.findViewById(R.id.tvNotify)
        tvLastDetecteTime = this.view!!.findViewById(R.id.tvLastDetectTime)

        show_TVOC?.setOnClickListener {
            dataForState = DetectionData.TVOC
            checkUIState()

        }
//        show_eCO2?.setOnClickListener {
//            dataForState = DetectionData.CO2
//            checkUIState()
//        }
        show_Temp?.setOnClickListener{
            dataForState = DetectionData.Temp
            checkUIState()
        }
        show_RH?.setOnClickListener {
            dataForState = DetectionData.Humi
            checkUIState()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        checkUIState()
    }
    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            LocalBroadcastManager.getInstance(mContext!!).unregisterReceiver(myBroadcastReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDetach() {
        super.onDetach()
    }

    private fun setThresholdValue(dataForState: DetectionData){
        tvThreadHold1!!.text= dataForState.range1.toString()
        tvThreadHold2!!.text= dataForState.range2.toString()
    }

    private fun setBarMaxValue(state: DetectionData){
        bar1?.setThreadholdValue(floatArrayOf(state.range1.toFloat(), state.range2.toFloat()))
        when (state){

            DetectionData.TVOC ->{
                bar1?.setMaxValues(1000f)
            }
            DetectionData.CO2 ->{
                bar1?.setMaxValues(2000f)
            }
            DetectionData.Temp ->{
                bar1?.setMaxValues(100f)
            }
            DetectionData.Humi ->{
                bar1?.setMaxValues(100f)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setBtmCurrentValue() {
        //DetectorValue=currentValue
        tvBtmTVOCValue.text = tvocDataFloat.toInt().toString() + " ppb"
        tvBtmPM25Value.text = "Coming soon"
        tvBtmCO2Value.text = "Coming soon"//co2DataFloat.toInt().toString()+ " ppm"
        tvBtmTEMPValue.text = tempDataFloat.toInt().toString() + " ℃"/*currentValue[0] + " ℃"*/
        tvBtmHUMIValue.text = humiDataFloat.toInt().toString() + " %"/*currentValue[1] + " %"*/
    }

     @SuppressLint("SetTextI18n")
     private fun tvocStatusTextShow(currentValue: Float) {
        when(currentValue) {
            in 0..219 -> {
                tvNotify?.text = getString(R.string.text_message_air_good)
                tvInCycleState?.text = getString(R.string.text_label_status_good)
                tvInCycleValue?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
                tvInCycleState?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
            }
            in 220..659 -> {
                tvNotify?.text = getString(R.string.text_message_air_mid)
                tvInCycleState?.text = getString(R.string.text_label_status_mid)
                tvInCycleValue?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Moderate))
                tvInCycleState?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Moderate))
            }
            in 660..2199 -> {
                tvNotify?.text = getString(R.string.text_message_air_Medium_Orange)
                tvInCycleState?.text = getString(R.string.text_label_status_medium_Orange)
                tvInCycleValue?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Orange))
                tvInCycleState?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Orange))
            }
            in 2200..5499 -> {
                tvNotify?.text = getString(R.string.text_message_air_bad)
                tvInCycleState?.text = getString(R.string.text_label_status_bad)
                tvInCycleValue?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
                tvInCycleState?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
            }
            in 5500..19999 -> {
                tvNotify?.text = getString(R.string.text_message_air_Serious_Purple)
                tvInCycleState?.text = getString(R.string.text_label_status_Serious_Purple)
                tvInCycleValue?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Purple))
                tvInCycleState?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Purple))
            }
            else -> {
                tvNotify?.text = getString(R.string.text_message_air_Extreme_Dark_Purple)
                tvInCycleState?.text = getString(R.string.text_label_status_Extreme_Dark_Purple)
                tvInCycleValue?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Test_Unhealthy))
                tvInCycleState?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Test_Unhealthy))
            }
        }
    }

    private fun eco2StatusTextShow(currentValue:Float){
        when(currentValue) {
            in 0..699 -> {
                //tvNotify?.text = getString(R.string.text_message_air_good)
                //tvInCycleState?.text = getString(R.string.text_label_status_good)
                tvInCycleValue?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
                tvInCycleState?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
            }
            in 700..999 -> {
                //tvNotify?.text = getString(R.string.text_message_air_bad)
                //tvInCycleState?.text = getString(R.string.text_label_status_bad)
                tvInCycleValue?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Moderate))
                tvInCycleState?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Moderate))
            }
            in 1000..1499 -> {
                //tvNotify?.text = getString(R.string.text_message_air_bad)
                //tvInCycleState?.text = getString(R.string.text_label_status_bad)
                tvInCycleValue?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Orange))
                tvInCycleState?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Orange))
            }
            in 1500..2499 -> {
                //tvNotify?.text = getString(R.string.text_message_air_bad)
                //tvInCycleState?.text = getString(R.string.text_label_status_bad)
                tvInCycleValue?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
                tvInCycleState?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
            }
            in 2500..4999 -> {
                //tvNotify?.text = getString(R.string.text_message_air_bad)
                //tvInCycleState?.text = getString(R.string.text_label_status_bad)
                tvInCycleValue?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Purple))
                tvInCycleState?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Purple))
            }
            else -> {
                //tvNotify?.text = getString(R.string.text_message_air_mid)
                //tvInCycleState?.text = getString(R.string.text_label_status_mid)
                tvInCycleValue?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Test_Unhealthy))
                tvInCycleState?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Test_Unhealthy))
            }
        }
    }

    private fun tempStatusTextShow(currentValue:Float){
        when(currentValue) {
            in 0..18 -> {
                tvNotify?.text = getString(R.string.text_message_temperature)
                tvInCycleState?.text = " "
                tvInCycleValue?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.progressBarMiddleBlue))
                tvInCycleState?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.progressBarMiddleBlue))
            }
            in 19..27 -> {
                tvNotify?.text = getString(R.string.text_message_temperature)
                tvInCycleState?.text = " "
                tvInCycleValue?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.progressBarMidColor))
                tvInCycleState?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.progressBarMidColor))
            }
            else -> {
                tvNotify?.text = getString(R.string.text_message_temperature)
                tvInCycleState?.text = " "
                tvInCycleValue?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.progressBarEndColor))
                tvInCycleState?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.progressBarEndColor))
            }
        }
    }

    private fun humiStatusTextShow(currentValue:Float){
        when(currentValue) {
            in 0..40 -> {
                tvNotify?.text = getString(R.string.text_message_humidity)
                tvInCycleState?.text = " "
                tvInCycleValue?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.progressBarMiddleBlue))
                tvInCycleState?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.progressBarMiddleBlue))
            }
            in 41..60 -> {
                tvNotify?.text = getString(R.string.text_message_humidity)
                tvInCycleState?.text = " "
                tvInCycleValue?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.progressBarStartColor))
                tvInCycleState?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.progressBarStartColor))
            }
            else -> {

                tvNotify?.text = getString(R.string.text_message_humidity)
                tvInCycleState?.text = " "
                tvInCycleValue?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.progressBarEndColor))
                tvInCycleState?.setTextColor(
                        ContextCompat.getColor(mContext, R.color.progressBarEndColor))
            }
        }
    }

    private fun isInitVibratorNotify(): Boolean {
        return true
    }

    private fun makeMainFragmentUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BroadcastActions.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BroadcastActions.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BroadcastActions.ACTION_GET_NEW_DATA)
        return intentFilter
    }

    @SuppressLint("SimpleDateFormat")
    @Synchronized private fun checkUIState() {
        if (mConnState && preHeat == "255") {
            //setThresholdValue(dataForState)
            //setBarMaxValue(dataForState)
            when (dataForState) {
                DetectionData.TVOC -> {
                    tvInCycleTitle!!.text = getString(R.string.text_label_tvoc_detect)
                    setThresholdValue(dataForState)
                    setBarMaxValue(dataForState)
                    bar1?.setTvocCo2Color()
                    //bar1?.setCurrentValues(tvocDataFloat)
                    bar1?.setCurrentValues(60000f)
                    tvocStatusTextShow(tvocDataFloat)
                    val temp = tvocDataFloat.toInt().toString() + " ppb "
                    textSpannable(temp)
                }
                DetectionData.CO2 -> {
                    tvInCycleTitle!!.text = getString(R.string.text_label_co2)
                    setThresholdValue(dataForState)
                    setBarMaxValue(dataForState)
                    bar1?.setTvocCo2Color()
                    bar1?.setCurrentValues(co2DataFloat)
                    eco2StatusTextShow(co2DataFloat)
                    val temp = co2DataFloat.toInt().toString() + " ppm "
                    textSpannable(temp)
                }
                DetectionData.Temp -> {
                    tvInCycleTitle!!.text = getString(R.string.text_label_temperature)
                    setThresholdValue(dataForState)
                    setBarMaxValue(dataForState)
                    bar1?.setTemperaterColor()
                    bar1?.setCurrentValues(tempDataFloat)
                    tempStatusTextShow(tempDataFloat)
                    val temp = tempDataFloat.toInt().toString() + " ℃"
                    textSpannable(temp)
                }

                DetectionData.Humi -> {
                    tvInCycleTitle!!.text = getString(R.string.text_label_humidity)
                    setThresholdValue(dataForState)
                    setBarMaxValue(dataForState)
                    bar1?.setHumidityColor()
                    bar1?.setCurrentValues(humiDataFloat)
                    humiStatusTextShow(humiDataFloat)
                    val temp = humiDataFloat.toInt().toString() + " % "
                    textSpannable(temp)
                }
            }

            setBtmCurrentValue()
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            val date = Date()
            tvLastDetecteTime?.text = dateFormat.format(date).toString()
            //20171228 ANDY增加
        } else {
            tvInCycleValue?.text = " "
            tvInCycleState?.text = " "
            tvBtmTVOCValue.text= "---"
            tvBtmPM25Value.text= "---"
            tvBtmCO2Value?.text= "---"
            tvBtmTEMPValue.text= "---"/*currentValue[0] + " ℃"*/
            tvBtmHUMIValue.text= "---"/*currentValue[1] + " %"*/
            tvNotify?.text = " "
            tvLastDetecteTime?.text = " "
            bar1?.setCurrentValues(0f)
        }
    }

    private fun textSpannable(temp : String) {
        val textSpan = SpannableStringBuilder(temp)
        textSpan.setSpan( 30,0,temp.indexOf(" ") +1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        textSpan.setSpan(AbsoluteSizeSpan(50), temp.indexOf(" ") + 1, temp.length - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        textSpan.setSpan(30,temp.indexOf(" ") - 1,temp.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        tvInCycleValue!!.text = textSpan
    }

    private val myBroadcastReceiver = object: BroadcastReceiver() {
        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                BroadcastActions.ACTION_GATT_DISCONNECTED -> {
                    mConnState = false
                    //setBar1CurrentValue("0","0","0","0","0")
                }

                BroadcastActions.ACTION_GATT_CONNECTED -> {
                    mConnState = true
                }

                BroadcastActions.ACTION_GET_NEW_DATA -> {
                    val bundle = intent.extras
                    tempDataFloat = bundle.getString(BroadcastActions.INTENT_KEY_TEMP_VALUE).toFloat()
                    humiDataFloat = bundle.getString(BroadcastActions.INTENT_KEY_HUMI_VALUE).toFloat()
                    tvocDataFloat = bundle.getString(BroadcastActions.INTENT_KEY_TVOC_VALUE).toFloat()
                    co2DataFloat = bundle.getString(BroadcastActions.INTENT_KEY_CO2_VALUE).toFloat()
                    preHeat = bundle.getString(BroadcastActions.INTENT_KEY_PREHEAT_COUNT)
                }
            }
            checkUIState()
        }
    }
}

