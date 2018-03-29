package com.microjet.airqi2.Account

//import com.github.angads25.filepicker.controller.DialogSelectionListener
//import com.github.angads25.filepicker.model.DialogConfigs
//import com.github.angads25.filepicker.model.DialogProperties
//import com.github.angads25.filepicker.view.FilePickerDialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract.Directory.PACKAGE_NAME
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.util.Log
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import com.microjet.airqi2.AsmDataModel
import com.microjet.airqi2.DownloadTask
import com.microjet.airqi2.MyApplication
import com.microjet.airqi2.R
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_account_active.*
import kotlinx.android.synthetic.main.drawer_header.*
import org.json.JSONException
import java.io.Closeable
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


@Suppress("NAME_SHADOWING")
/**
 * Created by B00170 on 2018/3/8.
 */
class AccountActiveActivity : AppCompatActivity() {
    private var mContext: Context? = null
    private val ASSET_NAME = "BLE_Data.csv"

    private var File: File? = null
    private var cacheFile: File? = null
    //試Realm拉資料
    private var arrTime = ArrayList<String>()
    private var arrData = ArrayList<String>()
    var useFor = 0
    var calObject = Calendar.getInstance()

    @SuppressLint("SdCardPath", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_active)
        mContext = this@AccountActiveActivity.applicationContext

        logout.setOnClickListener {
            val shareToKen = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
            shareToKen.edit().putString("token", "").apply()
//            shareToKen.edit().putString("name","") .apply()
//            shareToKen.edit().putString("email","") .apply()
            //shareToKen.edit().clear().apply()
            finish()
        }

        initActionBar()

        //20180310
        val shareMSG = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)

        val myName = shareMSG.getString("name", "")
        val myEmail = shareMSG.getString("email", "")
        //val myPassword= shareMSG.getString("password","")
        Log.e("登入後我的資訊", "登入中:" + myName + "信箱:" + myEmail) //+ "密碼:" + myPassword)
        showMail.text = myEmail
        show_Name.text = myName
        //text_Account_status.text = myName
        // get reference to all views

        var change_password = findViewById<TextView>(R.id.change_password)

        // 03/14 edit ID
        var editName = findViewById<TextView>(R.id.show_Name)
        editName.text = myName
        // 03/16 InputFilter max 20
        editName.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(20))
        change_password.setOnClickListener {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            val intent = Intent()
            intent.setClass(this@AccountActiveActivity.mContext, AccountResetPasswordActivity::class.java)
            //startActivityForResult(intent,1)
            startActivity(intent)
            //finish()
        }
        // 03/14 edit ID

        var cal = Calendar.getInstance()
        rename.setOnClickListener {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            val intent = Intent()
            intent.setClass(this@AccountActiveActivity.mContext, AccountNameReplaceActivity::class.java)
            startActivityForResult(intent, 1)
        }





            // create an OnDateSetListener
            val dateSetListener = object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                       dayOfMonth: Int) {
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    calObject.set(year, monthOfYear, dayOfMonth)
                    updateDateInView()
                }
            }
            // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        shareData!!.setOnClickListener {
            DatePickerDialog(this@AccountActiveActivity,
                    dateSetListener,
                    // set DatePickerDialog to point to today's date when it loads up
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        downloadData.setOnClickListener {
            val share_token = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
            val token = share_token.getString("token", "")
            val share = getSharedPreferences("MACADDRESS", Activity.MODE_PRIVATE)
            val macAddressForDB = share.getString("mac", "noValue")
            DownloadTask().execute(macAddressForDB, token)
        }

    }

    private fun updateDateInView() {
        val myFormat = "MM/dd/yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.TAIWAN)
        dbData2CVSAsyncTasks()//sdf)
        // if(this.checkLineInstalled()!!) {
        file_Provider()
        System.currentTimeMillis()
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

    override fun onStart() {
        super.onStart()
        val share_token = getSharedPreferences("TOKEN", MODE_PRIVATE)
        val _token = share_token.getString("token", "")
        Log.e("登入後onStart偷肯:", _token)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    //    private inner class dbData2CVSAsyncTasks(){ //: AsyncTask<Void, Void, String>() {
    private fun dbData2CVSAsyncTasks( ){//TS: TvocNoseData) {
        try {
            val AllData = getDbData(Date().day, Date().day )//Date().day, Date().day)
            writeDataToFile(AllData, this@AccountActiveActivity)
        } catch (e: Exception) {
            Log.e("return_body_erro", e.toString())
        }
    }

    //20180321
    val DataArrayListOne = ArrayList<String>()

    //val DataArrayListAll= ArrayList< ArrayList<String>>()
    private fun getDbData( startTimeZone: Int, EntTime: Int): ArrayList<String> {
        //很重要同區域才可以叫到同一個東西
//        val realm1 = Realm.getDefaultInstance()
//        val query1 = realm1.where(AsmDataModel::class.java)
//        val result1 = query1.findAll()

        arrTime.clear()
        arrData.clear()
        //現在時間實體毫秒
        //var touchTime = Calendar.getInstance().timeInMillis
        val touchTime = calObject.timeInMillis// + calObject.timeZone.rawOffset
        Log.d("TVOCbtncallRealm" + useFor.toString(), calObject.get(Calendar.DAY_OF_MONTH).toString())
        //將日期設為今天日子加一天減1秒
        val endDay = touchTime / (3600000 * 24) * (3600000 * 24)// - calObject.timeZone.rawOffset
        val endDayLast = endDay + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
        val realm = Realm.getDefaultInstance()
        val query = realm.where(AsmDataModel::class.java)
        //一天共有2880筆
        val dataCount = (endDayLast - endDay) / (60 * 1000)
        Log.d("TimePeriod" + useFor.toString(), (dataCount.toString() + "thirtySecondsCount"))
        query.between("Created_time", endDay, endDayLast).sort("Created_time", Sort.ASCENDING)
        val result1 = query.findAll()
        Log.e("資料筆數", result1.size.toString())
        Log.e("所有資料筆數", result1.toString().toString())
        //MyApplication getUUID=new MyApplication();
        val UUID = MyApplication.getPsuedoUniqueID()
        val timestampTEMP: Long? = null
        try {
            if (result1.size > 0) {
                val dateLabelFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                for (i in result1.indices) {
                    //toltoSize++;
                    //hasBeenUpLoaded.add(result1[i]!!.dataId)
                    Log.i("text", "i=" + i + "\n")
                    DataArrayListOne.add(result1[i]?.tempValue.toString())
                    DataArrayListOne.add(result1[i]?.humiValue.toString())
                    DataArrayListOne.add(result1[i]?.tvocValue.toString())
                    DataArrayListOne.add(result1[i]?.ecO2Value.toString())
                    DataArrayListOne.add(result1[i]?.pM25Value.toString())
                    DataArrayListOne.add(result1[i]?.longitude!!.toString())
                    DataArrayListOne.add(result1[i]?.latitude!!.toString())
                    val date = dateLabelFormat.format(result1[i]?.created_time!!.toLong())
                    DataArrayListOne.add(date)
                }
            } else {
                Log.e("未上傳資料筆數", result1.size.toString())
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        realm.close()

        return DataArrayListOne
    }

    //20180321
    private fun writeDataToFile(data: ArrayList<String>, context: Context) {
        try {

            val kk = context.getFileStreamPath("BLE_Data.csv")
            Log.e("", "")
//            if (!kk.exists()) {
//                val outputStreamWriterData = OutputStreamWriter(context.openFileOutput("BLE_Data.csv", Context.MODE_APPEND))
//                outputStreamWriterData.write("tempValue,humiValue,tvocValue,ecO2Value,pM25Value,longitude,latitude,created_time \r\n")
//                outputStreamWriterData.close()
//            }
            var mSDFile: File? = null

            //檢查有沒有SD卡裝置
            if (Environment.getExternalStorageState() == Environment.MEDIA_REMOVED) {
                Toast.makeText(applicationContext, "沒有SD卡!!!", Toast.LENGTH_SHORT).show()
                return
            } else {
                //取得SD卡儲存路徑
                mSDFile = Environment.getExternalStorageDirectory()
                mSDFile = context.getFileStreamPath("BLE_Data.csv")
                mSDFile.delete()
                //mSDFile = context.getFileStreamPath("BLEaddressData.txt");
            }

            val mFileWriter = FileWriter(mSDFile!!, true)
            mFileWriter.write("tempValue,humiValue,tvocValue,ecO2Value,pM25Value,longitude,latitude,created_time \r\n")
            for (l in 0..data.size) {
                //for (k in 0..7) {
                if (l != 0 && l.mod(8) == 0) {
                    mFileWriter.write("\r\n")
                }
                mFileWriter.write(data[l].toString() + ",")
                //}
            }
            mFileWriter.close()
            Log.e("Excel檔完成!!路徑為:", mSDFile.path)
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: " + e.toString())
        }
    }

    private fun close(closeable: Closeable?) {
        if (closeable == null) {
            return
        }
        try {
            closeable.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
    //20180329
    private fun  checkLineInstalled(): Boolean? {


        val lineInstallFlag: Boolean = false


        val pm = packageManager
        val m_appList = pm.getInstalledApplications(0)
        val ai:ApplicationInfo

        m_appList?.forEachIndexed { _, ai ->
            if (ai.packageName.equals(PACKAGE_NAME)) {
                val lineInstallFlag = true
            }
        }
        return lineInstallFlag;
    }




    private fun file_Provider() {
        var mSDFile: File? = null
        //mSDFile = this.getFilesDir()
        mSDFile = this@AccountActiveActivity.getFileStreamPath("BLE_Data.csv")
        val uri = FileProvider.getUriForFile(this, packageName, mSDFile)

        Log.e("#抓取檔案路徑為:", uri.path + "#packageName=" + packageName + "#mSDFile=" + mSDFile)
        val intent = Intent(Intent.ACTION_SEND)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.data = uri
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.setType("csv/plain")
        intent.type = "Application/csv"
        val chooser = Intent.createChooser(intent, title)

        //給目錄臨時的權限
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        // Verify the intent will resolve to at least one activity
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(chooser,0)
        }


////        //PO文字
//        val PACKAGE_NAME = "jp.naver.line.android"
//        val CLASS_NAME = "jp.naver.line.android.activity.selectchat.SelectChatActivity"
//        val Lineintent = Intent(Intent.ACTION_SEND)
//        Lineintent.setClassName(PACKAGE_NAME, CLASS_NAME)
//        intent.data = uri
//        Lineintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        Lineintent.setType("csv/plain")
//        intent.type = "Application/csv"
//        Lineintent.putExtra(Intent.EXTRA_STREAM, uri)
//        //Lineintent.data = uri
//        //Lineintent.putExtra(Intent.EXTRA_TEXT, uri)
//        startActivity(intent)
    }
}













