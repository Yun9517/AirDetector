package com.microjet.airqi2.GestureLock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.microjet.airqi2.AirMapActivity
import com.microjet.airqi2.PrefObjects
import com.microjet.airqi2.R
import kotlinx.android.synthetic.main.activity_use_password.*

class UsePasswordActivity : AppCompatActivity() {

    private lateinit var myPref: PrefObjects

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_use_password)

        myPref = PrefObjects(this)

        btnUnlock.setOnClickListener {
            val pref = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
            val userPW = pref.getString("LoginPassword", "")

            if (textPW.text!!.toString() == userPW) {
                when (actionMode) {
                    START_ACTION_MODE_DISABLE -> {
                        myPref.setSharePreferencePrivacy(false)
                    }

                    START_ACTION_MODE_NORMAL -> {
                        val i = Intent(this, AirMapActivity::class.java)
                        startActivity(i)
                    }

                    START_ACTION_MODE_CHANGE_PASSWOPRD -> DefaultPatternSettingActivity.startAction(this@UsePasswordActivity)
                }

                finish()
            }
        }

    }

    companion object {

        private var actionMode = 0

        const val START_ACTION_MODE_NORMAL = 0
        const val START_ACTION_MODE_DISABLE = 1
        const val START_ACTION_MODE_CHANGE_PASSWOPRD = 2

        fun startAction(context: Context, mode: Int) {
            val intent = Intent(context, UsePasswordActivity::class.java)
            context.startActivity(intent)

            actionMode = mode
        }
    }
}
