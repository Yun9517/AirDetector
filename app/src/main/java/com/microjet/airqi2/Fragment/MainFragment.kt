package com.microjet.airqi2.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.*
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Vibrator
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.frg_main.*
import com.microjet.airqi2.CustomAPI.ColorArcProgressBar
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.Definition.SavePreferences
import com.microjet.airqi2.R
import java.text.SimpleDateFormat
import java.util.*
import android.media.AudioManager
import android.media.SoundPool
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan


/**
 * Created by ray650128 on 2017/11/23.
 */


class MainFragment : Fragment() {

    enum class DetectionData(val range1: Long,val range2: Long) {
        TVOC(220,660),
        CO2(800,1500)
    }

    private var mContext : Context? = null
    private var bar1 : ColorArcProgressBar? = null
    private var tvThreadHold1:TextView?=null
    private var tvThreadHold2:TextView?=null
    private var tvInCycleTitle:TextView?=null
    private var tvInCycleValue:TextView?=null
    private var tvInCycleState:TextView?=null
    private var tvNotify:TextView?=null

    private var tvBtmTVOCValue:TextView?=null
    private var tvBtmPmValue:TextView?=null
    private var tvBtmCarbonValue:TextView?=null
    private var tvBtmTempValue:TextView?=null
    private var tvBtmHUMIValue:TextView?=null

    private var tvLastDetecteTime:TextView?=null
    //private var pressed="TVOC"//0=temperature 1=humidity 2=TVOC 3=CO2

    private var tvocDataFloat = 0f
    private var tempDataFloat = 0f
    private var humiDataFloat = 0f
    private var co2DataFloat = 0f

    private var dataForState = DetectionData.TVOC
    private var mConnState = false


    @Suppress("OverridingDeprecatedMember")

    //20171219   Andy
    private var mp = MediaPlayer()
    private var mVibrator: Vibrator? = null

    //20171220   Andy
    private var alertId: Int = 0
    private var soundPool: SoundPool? = null

    private var sourceid: Int = 0
    private var spool: SoundPool? = null

    private var countsound660:Int?=0
    private var countsound220:Int?=0
    private var countsound800:Int?=0
    private var countsound1500:Int?=0

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        mContext = this.context.applicationContext
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(mContext!!).registerReceiver(MyBroadcastReceiver, makeMainFragmentUpdateIntentFilter())
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        return inflater!!.inflate(R.layout.frg_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?){
        super.onActivityCreated(savedInstanceState)
        bar1 = this.view!!.findViewById(R.id.tvocBar)
        tvThreadHold1=this.view!!.findViewById(R.id.tvRange1)
        tvThreadHold2=this.view!!.findViewById(R.id.tvRange2)
        tvInCycleTitle=this.view!!.findViewById(R.id.inCycleTitle)
        tvInCycleValue= this.view!!.findViewById(R.id.inCycleValue)
        tvInCycleState= this.view!!.findViewById(R.id.inCycleState)
        tvNotify = this.view!!.findViewById(R.id.tvNotify)
        tvLastDetecteTime=this.view!!.findViewById(R.id.tvLastDetectTime)

        tvBtmTVOCValue =this.view!!.findViewById(R.id.tvBtmTVOCValue)
        tvBtmPmValue=this.view!!.findViewById(R.id.tvBtmPMValue)
        tvBtmCarbonValue =this.view!!.findViewById(R.id.tvBtmCarbonValue)
        tvBtmTempValue =this.view!!.findViewById(R.id.tvBtmTEMPValue)
        tvBtmHUMIValue=this.view!!.findViewById(R.id.tvBtmHUMIValue)

        show_TVOC?.setOnClickListener {
            dataForState = DetectionData.TVOC
            checkUIState()

        }
        show_eCO2?.setOnClickListener {
            dataForState = DetectionData.CO2
            checkUIState()
        }

    /*    show_Temp?.setOnClickListener { pressed="temperature"
            SetThresholdValue()
            SetbarMaxValue()
            bar1?.setCurrentValues(DetectorValue[0].toFloat())
            textView2.text=getString(R.string.text_label_temperature)
            val temp=DetectorValue[0]+" °C "
            val textSpan= SpannableStringBuilder(temp)
            textSpan.setSpan( 30,0,temp.indexOf(" ") +1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            textSpan.setSpan(AbsoluteSizeSpan(50), temp.indexOf(" ") + 1, temp.length - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            textSpan.setSpan(30,temp.indexOf(" ") - 1,temp.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            tvocValue2?.text = textSpan
        }
        textView7Humidity=this.view?.findViewById(R.id.textView7)
        show_RH?.setOnClickListener { pressed="humidity"
            SetThresholdValue()
            SetbarMaxValue()
            bar1?.setCurrentValues(DetectorValue[1].toFloat())
            textView2.text=getString(R.string.text_label_humidity)
            val temp=DetectorValue[1]+" ％ "
            val textSpan= SpannableStringBuilder(temp)
            textSpan.setSpan( 30,0,temp.indexOf(" ") +1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            textSpan.setSpan(AbsoluteSizeSpan(50), temp.indexOf(" ") + 1, temp.length - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            textSpan.setSpan(30,temp.indexOf(" ") - 1,temp.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            tvocValue2?.text = textSpan
        }
        */
    //    imgPanel = this.view!!.findViewById(R.id.imgPanel)

     //   imgPanel!!.bringToFront()
     //   tvocValue2=this.view?.findViewById(R.id.tvocValue2)
        //20171219   Andy
        mp = MediaPlayer.create (mContext, R.raw.pixiedust)
        // 初始化震动通知
        if (isInitVibratorNotify()) {
            mVibrator = mContext!!.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator?
        }


        //20171220   Andy
        soundPool = SoundPool(1, AudioManager.STREAM_MUSIC, 1)
        //alertId = soundPool!!.load(mContext, R.raw.tvoc_over660, 1)
        //sourceid = spool!!.load(mContext, R.raw.tvoc_over220, 1)

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
            LocalBroadcastManager.getInstance(mContext!!).unregisterReceiver(MyBroadcastReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDetach() {
        super.onDetach()
    }

    private fun SetThresholdValue(dataForState: DetectionData){
        tvThreadHold1!!.text= dataForState.range1.toString()
        tvThreadHold2!!.text= dataForState.range2.toString()
    }

    private fun SetbarMaxValue(state: DetectionData){
        bar1?.setThreadholdValue(floatArrayOf(state.range1.toFloat(), state.range2.toFloat()))
        when (state){
            /*
            "temperature"->{
                bar1?.setMaxValues(100f)

            }
            "humidity"->{
                bar1?.setMaxValues(100f)
            }
            */
            DetectionData.TVOC ->{
                bar1?.setMaxValues(1000f)
            }
            DetectionData.CO2 ->{
                bar1?.setMaxValues(2000f)
            }
        }
    }
//    var m_flagTemp=0
//    private fun setGetTimeFlag(flag:Int) {
//        if (flag == m_flagTemp) {
//            //no change
//        } else {//change
//            if (flag>m_flagTemp)//jude 0 to 1 or 1 to 0
//            {
//                tvLastDetecterTime?.text = getDateTime()
//
//            }
//            m_flagTemp = flag
//        }
//    }

//    fun getDateTime(): String {
//        val sdFormat = SimpleDateFormat("yyyy/MM/dd hh:mm:ss", Locale.TAIWAN)
//        val date = Date().time
//        return sdFormat.format(date)
//    }

    fun setBtmCurrentValue(){
        //DetectorValue=currentValue
        tvBtmTVOCValue?.text=tvocDataFloat.toInt().toString() + " ppb"
        tvBtmPMValue?.text="Coming soon"
        tvBtmCarbonValue?.text=co2DataFloat.toInt().toString()+ " ppm"
        tvBtmTEMPValue?.text="Coming soon"/*currentValue[0] + " ℃"*/
        tvBtmHUMIValue?.text="Coming soon"/*currentValue[1] + " %"*/
    }

     @SuppressLint("SetTextI18n")
//     fun setBar1CurrentValue() {
         //fun setBar1CurrentValue(tempVal: String, humiVal: String, tvocVal: String, co2Val: String, pm25Val: String) {

//         val stringArray=ArrayList<String>()
//         stringArray.add(tempVal)
//         stringArray.add(humiVal)
//         stringArray.add(tvocVal)
//         stringArray.add(co2Val)
         //setCurrentValue(stringArray)
/*

         if (currentValue.toFloat() < 221){
             textView?.text = getString(R.string.text_message_air_good)
             tvocStatus?.text = getString(R.string.text_label_ststus_good)

             tvocValue2.setTextColor(resources.getColor(R.color.Main_textResult_Good))
             tvocStatus.setTextColor(resources.getColor(R.color.Main_textResult_Good))
         }
         else if (currentValue.toFloat() > 661) {
             textView?.text = getString(R.string.text_message_air_bad)
             tvocStatus?.text = getString(R.string.text_label_ststus_bad)
             tvocValue2.setTextColor(resources.getColor(R.color.Main_textResult_Bad))
             tvocStatus.setTextColor(resources.getColor(R.color.Main_textResult_Bad))
         }
         else{
             textView?.text = getString(R.string.text_message_air_mid)
             tvocStatus?.text = getString(R.string.text_label_ststus_mid)

             tvocValue2.setTextColor(resources.getColor(R.color.Main_textResult_Moderate))
             tvocStatus.setTextColor(resources.getColor(R.color.Main_textResult_Moderate))
         }
     // ********* 2017/12/05 主頁面大小字 ************************************************* //

         */
         //SetThresholdValue(DetectionData.TVOC)
         //SetbarMaxValue(DetectionData.TVOC)
         //var temp = ""

//         when (dataForState){
//             DetectionData.TVOC-> {
//                 bar1?.setCurrentValues(tvocDataFloat)
//                 TVOCStatusTextShow(tvocDataFloat.toString())
//             }
//             DetectionData.CO2 -> {
//                 bar1?.setCurrentValues(co2DataFloat)
//                 CO2tatusTextShow(co2DataFloat.toString())
//
//             }
//             "temperature"->{
//                 temp = DetectorValue[0] + " °C "
//                 bar1?.setCurrentValues(DetectorValue[0].toFloat())
//             }
//             "humidity"->{
//                 temp = DetectorValue[1] + " ％ "
//                 bar1?.setCurrentValues(DetectorValue[1].toFloat())
//             }
//             "TVOC"->{
//                 temp = DetectorValue[2] +" ppb "
//                 bar1?.setCurrentValues(DetectorValue[2].toFloat())
//                 TVOCStatusTextShow(DetectorValue[2])
//             }
//             "CO2"->{
//                 temp = DetectorValue[3] +" ppm "
//                 bar1?.setCurrentValues(DetectorValue[3].toFloat())
//                 CO2tatusTextShow(DetectorValue[3])
//             }
//             else ->{
//                 temp = DetectorValue[3] +" ppb "
//                 bar1?.setCurrentValues(DetectorValue[2].toFloat())
//             }
//         }

//         val textSpan = SpannableStringBuilder(temp)
//         textSpan.setSpan( 30,0,temp.indexOf(" ") +1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
//         textSpan.setSpan(AbsoluteSizeSpan(50), temp.indexOf(" ") + 1, temp.length - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
//         textSpan.setSpan(30,temp.indexOf(" ") - 1,temp.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
//
//         inCycleValue?.text = textSpan
         //tvocValue?.text = textSpan

//         val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
//         val date = Date()
//         tvLastDetectTime?.text = dateFormat.format(date).toString()

     // *********************************************************************************** //
//    }
     private fun TVOCStatusTextShow(currentValue:Float){
        if (currentValue < 221){
            tvNotify?.text = getString(R.string.text_message_air_good)
            tvInCycleState?.text = getString(R.string.text_label_ststus_good)
            tvInCycleValue?.setTextColor(resources.getColor(R.color.Main_textResult_Good))
            tvInCycleState?.setTextColor(resources.getColor(R.color.Main_textResult_Good))
        }
        else if (currentValue  > 661) {
            tvNotify?.text = getString(R.string.text_message_air_bad)
            tvInCycleState?.text = getString(R.string.text_label_ststus_bad)
            tvInCycleValue?.setTextColor(resources.getColor(R.color.Main_textResult_Bad))
            tvInCycleState?.setTextColor(resources.getColor(R.color.Main_textResult_Bad))
            var mPreference: SharedPreferences = this.activity.getSharedPreferences(SavePreferences.SETTING_KEY, 0)
            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_SOUND, false))//&& (countsound660==5||countsound660==0)) {
            {

                if ((countsound660 == 5 || countsound660 == 0)) {
                    //20171220   Andy
                    try {
                        alertId = soundPool!!.load(mContext, R.raw.tvoc_over660, 1)
                        Thread.sleep(500)
                        soundPool!!.play(alertId, 1F, 1F, 0, 0, 1F)
                        //20171219   Andy
                        //mp.start()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                }
            }

            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_VIBERATION, false))//&& (countsound660==5||countsound660==0)) {
            {
                if ((countsound660 == 5|| countsound660 == 0)) {
                    if (mVibrator == null) {
                    } else {
                        // 震动 1s
                        mVibrator!!.vibrate(2000)
                    }
                }
            }

            if (countsound660 == 5) {
                countsound660 = 0
            }
            countsound660 = countsound660!! + 1
        }
        else{
            tvNotify?.text = getString(R.string.text_message_air_mid)
            tvInCycleState?.text = getString(R.string.text_label_ststus_mid)

            tvInCycleValue?.setTextColor(resources.getColor(R.color.Main_textResult_Moderate))
            tvInCycleState?.setTextColor(resources.getColor(R.color.Main_textResult_Moderate))

            var mPreference: SharedPreferences= this.activity.getSharedPreferences(SavePreferences.SETTING_KEY,0)
            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_SOUND,false))//&&(countsound220==5||countsound220==0))
            {

                if ((countsound220 == 5 || countsound220 == 0)) {
                    //20171219   Andy
                    //mp.start()
                    //20171220   Andy
                    try {
                        alertId = soundPool!!.load(mContext, R.raw.tvoc_over220, 1)
                        Thread.sleep(500)
                        soundPool!!.play(alertId, 1F, 1F, 0, 0, 1F)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                }
            }
            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_VIBERATION, false))//&& (countsound660==5||countsound660==0)) {
            {
                if ((countsound220 == 5|| countsound220 == 0)) {
                    if (mVibrator == null) {
                    } else {
                        // 震动 1s
                        mVibrator!!.vibrate(1000)
                    }
                }

            }

            if (countsound220 == 5) {
                countsound220 = 0
            }
            countsound220 = countsound220!! + 1

        }
    }
    private fun CO2tatusTextShow(currentValue:Float){
        if (currentValue < 800){
            tvNotify?.text = getString(R.string.text_message_air_good)
            tvInCycleState?.text = getString(R.string.text_label_ststus_good)
            tvInCycleValue?.setTextColor(resources.getColor(R.color.Main_textResult_Good))
            tvInCycleState?.setTextColor(resources.getColor(R.color.Main_textResult_Good))
        }
        else if (currentValue > 1500) {
            tvNotify?.text = getString(R.string.text_message_air_bad)
            tvInCycleState?.text = getString(R.string.text_label_ststus_bad)
            tvInCycleValue?.setTextColor(resources.getColor(R.color.Main_textResult_Bad))
            tvInCycleState?.setTextColor(resources.getColor(R.color.Main_textResult_Bad))
            var mPreference: SharedPreferences = this.activity.getSharedPreferences(SavePreferences.SETTING_KEY, 0)
            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_SOUND, false))//&& (countsound660==5||countsound660==0)) {
            {

                if ((countsound1500 == 5 || countsound1500 == 0)) {
                    //20171220   Andy
                    try {
                        alertId = soundPool!!.load(mContext, R.raw.tvoc_over660, 1)
                        Thread.sleep(500)
                        soundPool!!.play(alertId, 1F, 1F, 0, 0, 1F)
                        //20171219   Andy
                        //mp.start()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                }
            }

            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_VIBERATION, false))//&& (countsound660==5||countsound660==0)) {
            {
                if ((countsound1500 == 5|| countsound1500 == 0)) {
                    if (mVibrator == null) {
                    } else {
                        // 震动 1s
                        mVibrator!!.vibrate(2000)
                    }
                }
            }

            if (countsound1500 == 5) {
                countsound1500 = 0
            }
            countsound1500 = countsound1500!! + 1
        }
        else{
            tvNotify?.text = getString(R.string.text_message_air_mid)
            tvInCycleState?.text = getString(R.string.text_label_ststus_mid)

            tvInCycleValue?.setTextColor(resources.getColor(R.color.Main_textResult_Moderate))
            tvInCycleState?.setTextColor(resources.getColor(R.color.Main_textResult_Moderate))

            var mPreference: SharedPreferences= this.activity.getSharedPreferences(SavePreferences.SETTING_KEY,0)
            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_SOUND,false))//&&(countsound220==5||countsound220==0))
            {

                if ((countsound800 == 5 || countsound800 == 0)) {
                    //20171219   Andy
                    //mp.start()
                    //20171220   Andy
                    try {
                        alertId = soundPool!!.load(mContext, R.raw.tvoc_over220, 1)
                        Thread.sleep(500)
                        soundPool!!.play(alertId, 1F, 1F, 0, 0, 1F)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                }
            }
            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_VIBERATION, false))//&& (countsound660==5||countsound660==0)) {
            {
                if ((countsound800 == 5|| countsound800 == 0)) {
                    if (mVibrator == null) {
                    } else {
                        // 震动 1s
                        mVibrator!!.vibrate(1000)
                    }
                }

            }

            if (countsound800 == 5) {
                countsound800 = 0
            }
            countsound800 = countsound220!! + 1

        }
    }
    protected fun isInitVibratorNotify(): Boolean {
        return true
    }

    private fun makeMainFragmentUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BroadcastActions.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BroadcastActions.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BroadcastActions.ACTION_GET_NEW_DATA)
        return intentFilter
    }

    @Synchronized private fun checkUIState() {
        if (mConnState) {
            //SetThresholdValue(dataForState)
            //SetbarMaxValue(dataForState)
            when (dataForState) {
                DetectionData.TVOC -> {
                    tvInCycleTitle!!.text = getString(R.string.text_label_tvoc_detect)
                    SetThresholdValue(dataForState)
                    bar1?.setCurrentValues(tvocDataFloat)
                    SetbarMaxValue(dataForState)
                    TVOCStatusTextShow(tvocDataFloat)
                    val temp = tvocDataFloat.toInt().toString() + " ppb "
                    textSpannble(temp)
                }
                DetectionData.CO2 -> {
                    tvInCycleTitle!!.text = getString(R.string.text_label_co2)
                    SetThresholdValue(dataForState)
                    bar1?.setCurrentValues(co2DataFloat)
                    SetbarMaxValue(dataForState)
                    CO2tatusTextShow(co2DataFloat)
                    val temp = co2DataFloat.toInt().toString() + " ppm "
                    textSpannble(temp)
                }
            }
            setBtmCurrentValue()
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            val date = Date()
            tvLastDetecteTime?.text = dateFormat.format(date).toString()
        } else {
            tvInCycleValue?.text = "---"
            tvInCycleState?.text = "---"
            tvBtmTVOCValue?.text= "---"
            tvBtmPmValue?.text= "---"
            tvBtmCarbonValue?.text= "---"
            tvBtmTempValue?.text= "---"/*currentValue[0] + " ℃"*/
            tvBtmHUMIValue?.text= "---"/*currentValue[1] + " %"*/
            tvLastDetecteTime?.text = "---"
            bar1?.setCurrentValues(0f)
        }
    }

    private fun textSpannble (temp : String) {
        val textSpan= SpannableStringBuilder(temp)
        textSpan.setSpan( 30,0,temp.indexOf(" ") +1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        textSpan.setSpan(AbsoluteSizeSpan(50), temp.indexOf(" ") + 1, temp.length - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        textSpan.setSpan(30,temp.indexOf(" ") - 1,temp.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        tvInCycleValue!!.text = textSpan
    }


    private val MyBroadcastReceiver = object: BroadcastReceiver() {
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
                    //setBar1CurrentValue(tempVal,humiVal,tvocVal,co2Val,"0")
                    //val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                    //val date = Date()
                    //tvLastDetectTime?.text = dateFormat.format(date).toString()
                    //   setProgressBarValue(tempVal, humiVal, tvocVal, co2Val, "0")
                }
            }
            checkUIState()
        }
    }
}

