package microjet.com.airqi2.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.AutoSizeableTextView
import android.support.v4.widget.TextViewCompat
import android.text.AutoText
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.frg_main.*
import microjet.com.airqi2.CustomAPI.ColorArcProgressBar
import microjet.com.airqi2.MainActivity
import microjet.com.airqi2.R

/**
 * Created by ray650128 on 2017/11/23.
 */

class MainFragment : Fragment() {

    private var mContext : Context? = null

    private var bar1 : ColorArcProgressBar? = null

    private var imgPanel: ImageView? = null

    //private var tvocValue2: TextView?=null
    @Suppress("OverridingDeprecatedMember")
    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        mContext = this.context.applicationContext
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        return inflater!!.inflate(R.layout.frg_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?){
        super.onActivityCreated(savedInstanceState)
        bar1 = this.view!!.findViewById(R.id.tvocBar)
        imgPanel = this.view!!.findViewById(R.id.imgPanel)

        imgPanel!!.bringToFront()
     //   tvocValue2=this.view?.findViewById(R.id.tvocValue2)
    }

    override fun onResume() {
        super.onResume()
        bar1?.setMaxValues(1000f)
       // bar1!!.setCurrentValues(10f)
    }

    override fun onStop() {
        super.onStop()

    }
     @SuppressLint("SetTextI18n")
     fun setBar1CurrentValue(currentValue: String) {
         bar1?.setCurrentValues(currentValue.toFloat())
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
         var temp = ""

         temp = currentValue + " ppb "

         val textSpan = SpannableStringBuilder(temp)

         textSpan.setSpan( 30,0,temp.indexOf(" ") +1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
         textSpan.setSpan(AbsoluteSizeSpan(50), temp.indexOf(" ") + 1, temp.length - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
         textSpan.setSpan(30,temp.indexOf(" ") - 1,temp.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

         tvocValue2?.text = textSpan
         //tvocValue?.text = textSpan
     // *********************************************************************************** //
    }
}
