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
    //private var inCycleValueTvoc:TextView?=null
    //private var textView3CO2:TextView?=null
    //private var textView5Temperature:TextView?=null
    //private var textView7Humidity:TextView?=null

    private var imgPanel: ImageView? = null

    private var tvLastDetecterTime:TextView?=null
    //private var pressed="TVOC"//0=temperature 1=humidity 2=TVOC 3=CO2
    //private var DetectorValue=ArrayList<String>()
    //private var tvocValue2: TextView?=null

    private var tvocDataFloat = 0f
    private var tempDataFloat = 0f
    private var humiDataFloat = 0f
    private var co2DataFloat = 0f

    private var dataForState = DetectionData.TVOC


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

    //private val range1:Float = 0f
    //private val range2:Float = 0f


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
        tvLastDetecterTime=this.view!!.findViewById(R.id.tvLastDetectTime)

        tvBtmTVOCValue =this.view!!.findViewById(R.id.tvocValue)
        tvBtmPmValue=this.view!!.findViewById(R.id.tvBtmPMValue)
        tvBtmCarbonValue =this.view!!.findViewById(R.id.carbonValue)
        tvBtmTempValue =this.view!!.findViewById(R.id.tvBtmTEMPValue)
        tvBtmHUMIValue=this.view!!.findViewById(R.id.wetValue)

        show_TVOC?.setOnClickListener {
            //pressed="TVOC"
            dataForState = DetectionData.TVOC
            //SetThresholdValue(dataForState)
            //SetbarMaxValue(dataForState)
            //bar1?.setCurrentValues(tvocDataFloat)
            //textView2.text=getString(R.string.text_label_auto_detect)
            //val temp=DetectorValue[2]+" ppb "
            //val textSpan= SpannableStringBuilder(temp)
            //textSpan.setSpan( 30,0,temp.indexOf(" ") +1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            //textSpan.setSpan(AbsoluteSizeSpan(50), temp.indexOf(" ") + 1, temp.length - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            //textSpan.setSpan(30,temp.indexOf(" ") - 1,temp.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            //tvocValue2?.text = textSpan
            checkUIState()

        }
        show_eCO2?.setOnClickListener {
            //pressed="CO2"
            dataForState = DetectionData.CO2
            //SetThresholdValue(dataForState)
            //SetbarMaxValue(dataForState)
            //bar1?.setCurrentValues(DetectorValue[3].toFloat())
            //textView2.text=getString(R.string.text_label_co2)
            //val temp=DetectorValue[3]+" ppm "
            //val textSpan= SpannableStringBuilder(temp)
            //textSpan.setSpan( 30,0,temp.indexOf(" ") +1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            //textSpan.setSpan(AbsoluteSizeSpan(50), temp.indexOf(" ") + 1, temp.length - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            //textSpan.setSpan(30,temp.indexOf(" ") - 1,temp.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            //tvocValue2?.text = textSpan
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
        //SetThresholdValue(DetectorData)
        //val range1:Float=tvThreadHold1?.text.toString().toFloat()
        //val range2:Float=tvThreadHold2?.text.toString().toFloat()
        //bar1?.setThreadholdValue(floatArrayOf(range1,range2))
    //    pressed="temperature"

        /*when (pressed){/*
            "temperature"->{
                bar1?.setMaxValues(100f)
            }
            "humidity"->{
                bar1?.setMaxValues(100f)
            }
            */
            "TVOC"->{
                bar1?.setMaxValues(1000f)
            }
            "CO2"->{
                bar1?.setMaxValues(2000f)
            }
            else ->{pressed="TVOC"
                bar1?.setMaxValues(1000f)
            }
        }*/

       // bar1!!.setCurrentValues(10f)
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
        /*
        when (pressed){/*
            "temperature"->{
                ThreadHold1?.text="20"
                ThreadHold2?.text="50"
            }
            "humidity"->{
                ThreadHold1?.text="20"
                ThreadHold2?.text="50"
            }*/
            "TVOC"->{
                tvThreadHold1?.text= DetectionData.range1
                tvThreadHold2?.text= DetectionData.range2
            }
            "CO2"->{
                tvThreadHold1?.text="800"
                tvThreadHold2?.text="1500"
            }
            else ->{pressed="TVOC"
                tvThreadHold1?.text="220"
                tvThreadHold2?.text="660"
            }
        }
        */
        tvThreadHold1!!.text= dataForState.range1.toString()
        tvThreadHold2!!.text= dataForState.range2.toString()
    }
    private fun SetbarMaxValue(state: DetectionData){
        //val range1:Float=tvThreadHold1?.text.toString().toFloat()
        //val range2:Float=tvThreadHold2?.text.toString().toFloat()
        bar1?.setThreadholdValue(floatArrayOf(state.range1.toFloat(), state.range2.toFloat()))
        //    pressed="temperature"
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
    private fun CO2tatusTextShow(currentValue:String){
        if (currentValue.toFloat() < 800){
            tvNotify?.text = getString(R.string.text_message_air_good)
            tvInCycleState?.text = getString(R.string.text_label_ststus_good)
            tvInCycleValue?.setTextColor(resources.getColor(R.color.Main_textResult_Good))
            tvInCycleState?.setTextColor(resources.getColor(R.color.Main_textResult_Good))
        }
        else if (currentValue.toFloat() > 1500) {
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
        intentFilter.addAction(BroadcastActions.ACTION_GET_NEW_DATA)
        return intentFilter
    }

    @Synchronized private fun checkUIState() {
        SetThresholdValue(dataForState)
        SetbarMaxValue(dataForState)
        setBtmCurrentValue()
        when (dataForState) {
            DetectionData.TVOC -> {
                tvInCycleTitle!!.text = getString(R.string.text_label_tvoc)
                tvInCycleValue!!.text = tvocDataFloat.toInt().toString() + " ppb"
                bar1?.setCurrentValues(tvocDataFloat)
                TVOCStatusTextShow(tvocDataFloat)
            }
            DetectionData.CO2 -> {
                tvInCycleTitle!!.text = getString(R.string.text_label_co2)
                tvInCycleValue!!.text = co2DataFloat.toInt().toString() + " ppm"
                bar1?.setCurrentValues(co2DataFloat)
                CO2tatusTextShow(co2DataFloat.toString())
            }
        }
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val date = Date()
        tvLastDetectTime?.text = dateFormat.format(date).toString()
    }


    private val MyBroadcastReceiver = object: BroadcastReceiver() {
        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                BroadcastActions.ACTION_GATT_DISCONNECTED -> {
                    tvBtmCarbonValue!!.text = "---"
                    tvBtmHUMIValue!!.text = "---"
                    tvBtmPmValue!!.text = "---"
                    tvBtmTVOCValue!!.text = "---"
                    tvBtmTempValue!!.text = "---"
                    //setBar1CurrentValue("0","0","0","0","0")
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
