package com.microjet.airqi2.GestureLock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.github.ihsg.patternlocker.OnPatternChangeListener
import com.github.ihsg.patternlocker.PatternLockerView
import com.microjet.airqi2.AirMapActivity
import com.microjet.airqi2.GoldenMapActivity
import com.microjet.airqi2.PrefObjects
import com.microjet.airqi2.R
import kotlinx.android.synthetic.main.activity_default_pattern_checking.*
import java.util.*


class DefaultPatternCheckingActivity : AppCompatActivity() {

    private var patternHelper: PatternHelper? = null

    private lateinit var myPref: PrefObjects

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_default_pattern_checking)

        myPref = PrefObjects(this)

        val pref = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
        val userPW = pref.getString("LoginPassword", "")

        if(userPW == "") {
            btnUsePW.visibility = View.GONE
        } else {
            btnUsePW.visibility = View.VISIBLE
        }

        btnUsePW.setOnClickListener {
            UsePasswordActivity.startAction(this@DefaultPatternCheckingActivity, actionMode)
            finish()
        }

        patternLockerView.setOnPatternChangedListener(object : OnPatternChangeListener {
            override fun onStart(view: PatternLockerView) {}

            override fun onChange(view: PatternLockerView, hitList: List<Int>) {}

            override fun onComplete(view: PatternLockerView, hitList: List<Int>) {
                val isError = !isPatternOk(hitList)
                view.updateStatus(isError)
                //patternIndicatorView!!.updateState(hitList, isError)
                updateMsg()

                if (!isError) {
                    when (actionMode) {
                        START_ACTION_MODE_DISABLE -> {
                            myPref.setSharePreferencePrivacy(false)
                        }

                        START_ACTION_MODE_NORMAL -> callCompletePage()

                        START_ACTION_MODE_CHANGE_PASSWOPRD -> DefaultPatternSettingActivity.startAction(this@DefaultPatternCheckingActivity)
                    }
                }
                Log.e("Pattern", "Error: $isError")
            }

            override fun onClear(view: PatternLockerView) {
                finishIfNeeded()
            }
        })

        text_msg.text = resources.getText(R.string.text_draw_pattern)
        this.patternHelper = PatternHelper()
    }

    private fun isPatternOk(hitList: List<Int>): Boolean {
        this.patternHelper!!.validateForChecking(hitList)
        return this.patternHelper!!.isOk
    }

    private fun updateMsg() {
        text_msg.text = this.patternHelper!!.message
        text_msg.setTextColor(if (this.patternHelper!!.isOk)
            ContextCompat.getColor(this, R.color.Main_textResult_Good)
        else
            ContextCompat.getColor(this, R.color.Main_textResult_Bad))
    }

    private fun finishIfNeeded() {
        if (this.patternHelper!!.isFinish) {
            finish()
        }
    }

    private fun callCompletePage() {
        val mLang = Locale.getDefault().language + "-" + Locale.getDefault().country
        val i: Intent? = Intent(this, if (mLang == "zh-CN") {
            GoldenMapActivity::class.java
        } else {
            AirMapActivity::class.java
        })
        startActivity(i)
    }

    companion object {

        private var actionMode = 0

        const val START_ACTION_MODE_NORMAL = 0
        const val START_ACTION_MODE_DISABLE = 1
        const val START_ACTION_MODE_CHANGE_PASSWOPRD = 2

        fun startAction(context: Context, mode: Int) {
            val intent = Intent(context, DefaultPatternCheckingActivity::class.java)
            context.startActivity(intent)

            actionMode = mode
        }
    }
}
