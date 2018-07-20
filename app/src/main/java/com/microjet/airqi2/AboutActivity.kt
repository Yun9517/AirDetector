package com.microjet.airqi2

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.microjet.airqi2.BlueTooth.DFU.DFUActivity
import java.util.HashMap
import com.microjet.airqi2.CustomAPI.OnMultipleClickListener
import com.microjet.airqi2.CustomAPI.Utils
import com.microjet.airqi2.Definition.SavePreferences
import com.microjet.airqi2.URL.AirActionTask
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.drawer_header.*
import org.greenrobot.eventbus.Subscribe
import java.io.File


@Suppress("DEPRECATION")
class AboutActivity : AppCompatActivity() {

    private var soundPool: SoundPool? = null
    private var soundsMap: HashMap<Int, Int>? = null

    private var SOUND1 = 1
    private var clickNumber=0
    private var mContext: Context? = null
    var myDeviceName:String?=null
    var myDeviceAddress:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        myDeviceAddress = intent.extras.getString("ADDRESS")
        myDeviceName = intent.extras.getString("DEVICE_NAME")
        mContext = this@AboutActivity

        val string: String? = getString(R.string.show_app_version, BuildConfig.VERSION_NAME)
        aboutAppVersion!!.text = string

        // Create sound pool
        soundPool = SoundPool(4, AudioManager.STREAM_ALARM, 100)
        soundsMap = HashMap()
        soundsMap!!.put(SOUND1, soundPool!!.load(this, R.raw.pixiedust, 1))

        initActionBar()

        app_logo!!.setOnClickListener(object : OnMultipleClickListener(10, 500) {
            override fun onMultipleClick(v: View) {
                soundPool!!.play(soundsMap!![SOUND1]!!, 10f, 10f, 1, 0, 1f)
                Utils.toastMakeTextAndShow(mContext!!, "你為什麼要點我QAQ", Toast.LENGTH_SHORT)
            }
        })
    }

    private fun initActionBar() {
        // 取得 actionBar
        val actionBar = supportActionBar
        // 設定顯示左上角的按鈕
        actionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> { //對用戶按home icon的處理，本例只需關閉activity，就可返回上一activity，即主activity。
                finish()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }
    fun onUploadClicked(view: View){
        clickNumber++
        when (clickNumber) {
            in 1..6->{
                Utils.toastMakeTextAndShow(mContext!!, "再按"+(7-clickNumber).toString()+"次即可成為工程模式" ,Toast.LENGTH_SHORT)
            }
            7-> {
                val file = File(cacheDir, "FWupdate.zip")
                if (myDeviceAddress != "" && file.exists()) {
                    val intent = Intent(this, DFUActivity::class.java)
                            .putExtra("ADDRESS", myDeviceAddress)
                            .putExtra("DEVICE_NAME", myDeviceName)
                    startActivity(intent)
                } else {
                //    Utils.toastMakeTextAndShow(mContext!!, "沒有完成特定條件是無法成為唉滴喂大師的唷!!!", Toast.LENGTH_SHORT)
                    clickNumber--
                    if (!file.exists())
                        showDownloadDialog()
                }
            }
            else ->{Utils.toastMakeTextAndShow(mContext!!, "唉滴喂大師你好!!!" ,Toast.LENGTH_SHORT)}
        }
        //Utils.toastMakeTextAndShow(mContext!!, "你為什麼要點我QAQ", Toast.LENGTH_SHORT)
    }

    private fun showDownloadDialog() {
        val Dialog = android.app.AlertDialog.Builder(this).create()
        //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
        //Dialog.setTitle("提示")
        Dialog.setTitle(getString(R.string.remind))
        Dialog.setMessage("完成此任務須耗費流量，須要幫您完成任務嗎？")
        Dialog.setCancelable(false)//讓返回鍵與空白無效
        //Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")
        Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.Reject))//否
        { dialog, _ ->
            dialog.dismiss()
        }
        Dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.Accept))//是
        { dialog, _ ->
            dialog.dismiss()
            val mPreference: SharedPreferences = this.mContext!!.getSharedPreferences(SavePreferences.AirActionTask_KEY, Context.MODE_PRIVATE)
            val fileUrl= mPreference?.getString("FilePath", "")

            if (fileUrl.isNullOrEmpty()) {
                Utils.toastMakeTextAndShow(mContext!!, "沒有下載網址!!!", Toast.LENGTH_SHORT)
            }
            else {
                val aat = AirActionTask(this.mContext!!)
                aat.execute("downloadFWFile")

            }
        }
        Dialog.show()
    }

    @Subscribe
    fun onEvent(bleEvent: BleEvent ){
        /* 處理事件 */
        Log.d("AirAction", bleEvent.message)
        when (bleEvent.message) {
            "New FW Arrival"->{
            //    showDownloadDialog(bleEvent.message!!)
            }
            "Download Success"->{
                findViewById<View>(R.id.app_logo)
                onUploadClicked(findViewById<View>(R.id.app_logo))
            }
        }
    }
}
