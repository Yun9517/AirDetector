package com.microjet.airqi2.settingPage

import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.microjet.airqi2.PrefObjects
import com.microjet.airqi2.R
import kotlinx.android.synthetic.main.activity_setting4.*

/**
 * Created by B00174 on 2017/11/29.
 *
 */

class CloudSyncSettingActivity : AppCompatActivity() {

    //20180227
    private var swCloudVal: Boolean = false
    private var swCloud3GVal: Boolean = false

    private lateinit var myPref: PrefObjects

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting4)

        myPref = PrefObjects(this)

        readPreferences()   // 載入設定值
        uiSetListener()
        initActionBar()
    }

    private fun readPreferences() {
        getCloudSettings()
    }

    private fun uiSetListener() {
        //20180227  CloudFun
        swCloudFunc.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                cgAllow3G.visibility = View.VISIBLE

                showEnable3GDialog()

                swCloud3GVal = myPref.getSharePreferenceCloudUpload3GStat()

                /*if(swCloud3GVal) {
                    swAllow3G.isChecked = swCloud3GVal
                }*/
            } else {
                cgAllow3G.visibility = View.GONE
            }

            myPref.setSharePreferenceCloudUploadStat(isChecked)
        }

        swAllow3G.setOnCheckedChangeListener { _, isChecked ->

            myPref.setSharePreferenceCloudUpload3GStat(isChecked)
        }
    }

    private fun getCloudSettings() {
        swCloudVal = myPref.getSharePreferenceCloudUploadStat()
        swCloud3GVal = myPref.getSharePreferenceCloudUpload3GStat()

        swCloudFunc.isChecked = swCloudVal

        if (swCloudVal) {
            cgAllow3G.visibility = View.VISIBLE

            if (swCloud3GVal) {
                swAllow3G.isChecked = swCloud3GVal
            }
        } else {
            cgAllow3G.visibility = View.GONE
        }

    }

    private fun initActionBar() {
        // 取得 actionBar
        val actionBar = supportActionBar
        // 設定顯示左上角的按鈕
        actionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home //對用戶按home icon的處理，本例只需關閉activity，就可返回上一activity，即主activity。
            -> {
                finish()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // 2018/05/30 show enable 3G dialog
    private fun showEnable3GDialog() {
        val dlg = android.app.AlertDialog.Builder(this).create()
        dlg.setTitle(getString(R.string.allow_3G))
        dlg.setMessage(getString(R.string.text_Enable3GDialog))
        dlg.setCancelable(false)//讓返回鍵與空白無效
        //Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")

        dlg.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.text_close))//否
        { dialog, _ ->
            swAllow3G.isChecked = false
            dialog.dismiss()
        }
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.text_open))//是
        { dialog, _ ->
            swAllow3G.isChecked = true
            dialog.dismiss()
        }
        dlg.show()
    }
}
