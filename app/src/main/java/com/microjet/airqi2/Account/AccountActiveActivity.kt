package com.microjet.airqi2.Account

//import com.github.angads25.filepicker.controller.DialogSelectionListener
//import com.github.angads25.filepicker.model.DialogConfigs
//import com.github.angads25.filepicker.model.DialogProperties
//import com.github.angads25.filepicker.view.FilePickerDialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract.Directory.PACKAGE_NAME
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.microjet.airqi2.*
import com.microjet.airqi2.CustomAPI.Utils
import com.microjet.airqi2.Fragment.CheckFragment
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_account_active.*
import org.json.JSONArray
import org.json.JSONException
import java.io.Closeable
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


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
    var dialog: Dialog? =null
    var download_Bar: ProgressBar? = null
    var download_min: TextView? = null
    var download_text: TextView? = null
    private lateinit var myPref: PrefObjects

    //20180530
    private var cloudDeviceListItem: String? =""

    //20180310
    private var shareMSG: SharedPreferences? = null
    @SuppressLint("SdCardPath", "SimpleDateFormat")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_active)
        mContext = this@AccountActiveActivity.applicationContext
        download_Bar = this.findViewById(R.id.progressBar2)
        download_min = this.findViewById(R.id.download_min)
        download_text = this.findViewById(R.id.download_text)
        myPref = PrefObjects(this)
        AccountObject.activityAccountActive = this

        logout.setOnClickListener {
            val shareToKen = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
            //shareToKen.edit().putString("token", "").apply()
            //shareToKen.edit().putString("LoginPassword", "").apply()
//            shareToKen.edit().putString("name","") .apply()
//            shareToKen.edit().putString("email","") .apply()
            //shareToKen.edit().clear().apply()
            val token: String = "Bearer " + shareToKen.getString("token", "")
            Log.e("clickLogOut", token)
            SignOutTask(this).execute(token)
            myPref.setSharePreferenceCloudUploadStat(false)
            myPref.setSharePreferenceCloudUpload3GStat(false)
            intent.setClass(this@AccountActiveActivity.mContext, AccountManagementActivity::class.java)
            startActivity(intent)
            finish()
        }

        initActionBar()
        //barney ++
        fetchData.setOnClickListener{
            Log.d("click action","-- BT fetchData click --")
            val intent = Intent()
            intent.setClass(this@AccountActiveActivity.mContext, FetchDataMain::class.java)
//            startActivityForResult(intent,1)
            startActivity(intent)
        }



        // 03/30
        change_password.setOnClickListener {
            if(isConnected()) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                val intent = Intent()
                intent.setClass(this@AccountActiveActivity.mContext, AccountResetPasswordActivity::class.java)
                startActivity(intent)
                //finish()
            } else {
                showDialog(getString(R.string.checkConnection))
            }

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
//            val dateSetListener = object : DatePickerDialog.OnDateSetListener {
//                override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
//                                       dayOfMonth: Int) {
//                    cal.set(Calendar.YEAR, year)
//                    cal.set(Calendar.MONTH, monthOfYear)
//                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
//                    calObject.set(year, monthOfYear, dayOfMonth)
//                    updateDateInView()
//                }
//            }

        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        /*
        shareData!!.setOnClickListener {
            var cal = Calendar.getInstance()
            val dpd = DatePickerDialog(this!!, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                cal.set(year, month, dayOfMonth)
                calObject.set(year, month, dayOfMonth)
                updateDateInView()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            dpd.setMessage(getString(R.string.select_Date))//請選擇日期
            dpd.show()
        }
        */

        //雲端DATA DOWNLOAD 按鈕事件
        downloadData.setOnClickListener {
            val share_token = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
            val token = share_token.getString("token", "")
            //取得裝置資料清單下載
            val arr = JSONArray(shareMSG?.getString("deviceLi",""))
            //新帳號會拿不到deviceList，要加上安全判斷
            if (arr.length() != 0) {
                val list = ArrayList<String>()
                for (i in 0 until arr!!.length()) {
                    list.add(arr.getJSONObject(i).getString("mac_address"))
                }
                selectDeviceDownload(list, token)
            } else {
                Utils.toastMakeTextAndShow(this@AccountActiveActivity, String.format(getString(R.string.NoDataAvailableForDownload)), Toast.LENGTH_SHORT)
            }
        }
        shareMSG = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
    }

    private fun updateDateInView() {
        dbData2CVSAsyncTasks()//sdf)
        file_Provider()
    }


    //20180311
    fun showDialog (msg:String) {
        val Dialog = android.app.AlertDialog.Builder(this@AccountActiveActivity).create()
        //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
        //Dialog.setTitle("提示")
        //Dialog.setTitle(getString(R.string.remind))
        Dialog.setMessage(msg.toString())
        Dialog.setCancelable(false)//讓返回鍵與空白無效
        //Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")
        Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.confirm))
        { dialog, _ ->
            dialog.dismiss()
            //finish()
        }
        Dialog.show()
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
        val myName = shareMSG?.getString("name", "")
        // ****** 2018/04/10 Remember ID *******************************************************//
        val myEmail = shareMSG?.getString("email", "")
        //val myPassword= shareMSG.getString("password","")
        Log.e("登入後我的資訊", "登入中:" + myName + "信箱:" + myEmail) //+ "密碼:" + myPassword)
        showMail.text = myEmail
        show_Name.text = myName

        // 03/14 edit ID
        var editName = findViewById<TextView>(R.id.show_Name)
        editName.text = myName

        // 03/16 InputFilter max 20
        editName.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(20))

    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onStop() {
        super.onStop()
        dialog?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun dbData2CVSAsyncTasks( ){//TS: TvocNoseData) {
        try {
            val AllData = getDbData(Date().day, Date().day )//Date().day, Date().day)
            writeDataToFile(AllData, this@AccountActiveActivity)
        } catch (e: Exception) {
            Log.e("return_body_erro", e.toString())
        }
    }

    //20180321
    private fun getDbData( startTimeZone: Int, EntTime: Int): ArrayList<String> {
        val dataArrayListOnee = ArrayList<String>()
        val touchTime = if (calObject.get(Calendar.HOUR) >= 8) calObject.timeInMillis else calObject.timeInMillis + calObject.timeZone.rawOffset
        //val touchTime = calObject.timeInMillis// + calObject.timeZone.rawOffset
        Log.d("TVOCbtncallRealm" + useFor.toString(), calObject.get(Calendar.DAY_OF_MONTH).toString())
        val endDay = touchTime / (3600000 * 24) * (3600000 * 24) - calObject.timeZone.rawOffset
        val endDayLast = endDay + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
        val realm = Realm.getDefaultInstance()
        val query = realm.where(AsmDataModel::class.java)
        //一天共有2880筆
        val dataCount = (endDayLast - endDay) / (60 * 1000)
        Log.d("TimePeriod" + useFor.toString(), (dataCount.toString() + "thirtySecondsCount"))
        query.between("Created_time", endDay, endDayLast).sort("Created_time", Sort.ASCENDING)
        val result1 = query.findAll()
        Log.e("資料筆數", result1.size.toString())
        Log.e("所有資料筆數", result1.toString())
        try {
            if (result1.size > 0) {
                val dateLabelFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                for (i in result1.indices) {
                    Log.i("text", "i=" + i + "\n")
                    dataArrayListOnee.add(result1[i]?.tempValue.toString()+",")
                    dataArrayListOnee.add(result1[i]?.humiValue.toString()+",")
                    dataArrayListOnee.add(result1[i]?.tvocValue.toString()+",")
                    dataArrayListOnee.add(result1[i]?.ecO2Value.toString()+",")
                    dataArrayListOnee.add(result1[i]?.pM25Value.toString()+",")
                    dataArrayListOnee.add(result1[i]?.longitude!!.toString()+",")
                    dataArrayListOnee.add(result1[i]?.latitude!!.toString()+",")
                    val date = dateLabelFormat.format(result1[i]?.created_time!!.toLong())
                    dataArrayListOnee.add(date+"\r\n" )
                }
            } else {
                Log.e("未上傳資料筆數", result1.size.toString())
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        realm.close()
        return dataArrayListOnee
    }

    //20180321
    private fun writeDataToFile(data: ArrayList<String>, context: Context) {
        try {
            var mSDFile: File? = null
            //檢查有沒有SD卡裝置
            if (Environment.getExternalStorageState() == Environment.MEDIA_REMOVED) {
                Utils.toastMakeTextAndShow(this@AccountActiveActivity, "沒有SD卡!!!", Toast.LENGTH_SHORT)
                return
            } else {
                //取得SD卡儲存路徑
                mSDFile = Environment.getExternalStorageDirectory()
                mSDFile = context.getFileStreamPath("BLE_Data.csv")
                mSDFile.delete()
            }
            val mFileWriter = FileWriter(mSDFile!!, true)
            mFileWriter.write("tempValue,humiValue,tvocValue,ecO2Value,pM25Value,longitude,latitude,created_time \r\n")
            for (l in 0..data.size) {
                mFileWriter.write(data[l])//data[l])
                mFileWriter.flush()
            }
            Log.e("全給我進去!!",data.last())
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

    // 2018/03/30
    private fun isConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    //20180329
    private fun  checkLineInstalled(): Boolean? {
        val lineInstallFlag: Boolean = false
        val pm = packageManager
        val m_appList = pm.getInstalledApplications(0)
        val ai: ApplicationInfo

        m_appList?.forEachIndexed { _, ai ->
            if (ai.packageName == PACKAGE_NAME) {
                val lineInstallFlag = true
            }
        }
        return lineInstallFlag

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
    }

    // 04/18 雲端視窗顯示
    private fun selectDeviceDownload(list: ArrayList<String>, token: String) {
        Log.e("list的內容", list.toString())
        val builder = AlertDialog.Builder(this)
        val inflater: LayoutInflater=LayoutInflater.from(this)
        val view: View = inflater.inflate(R.layout.app_downloaddata_select,null)
        dialog = builder.create()
        dialog?.show()
        dialog?.getWindow()?.setContentView(view)
        val bt_cancel = view.findViewById<Button>(R.id.bt_cancel_download)//使用app_downloaddata_select頁面的元件
        val bt_listview = view.findViewById<ListView>(R.id.bt_listview)
        val adapter=ArrayAdapter(this,android.R.layout.simple_list_item_1, list)
        bt_listview.adapter = adapter //listview.setAdapter(adapter)
        bt_listview.setVerticalScrollBarEnabled(true)//滾動條存在->true
        bt_listview.setScrollbarFadingEnabled(false)//滾動條不活動時候，依舊顯示
        bt_listview.setOnItemClickListener { parent, view, position, id ->
            if (TvocNoseData.download_AsynTask?.status == AsyncTask.Status.RUNNING) {
                val newFrage = CheckFragment().newInstance(R.string.remind,R.string.text_check_fragment,this,2,"doPositiveClick")
                newFrage.show(fragmentManager,"dialog")
                cloudDeviceListItem = list[position]
            } else {
                TvocNoseData.download_AsynTask = DownloadTask(this, download_Bar!!, download_min!!, download_text!!).execute(list[position], token)
                dialog?.dismiss()//結束小視窗
            }
        }
        bt_cancel.setOnClickListener {
            Log.e("download_AsynTask",TvocNoseData.download_AsynTask?.status.toString())
            dialog?.dismiss()//結束小視窗
        }
    }

    fun doPositiveClick() {
        TvocNoseData.download_AsynTask?.cancel(true)
        val share_token = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
        val token = share_token.getString("token", "")
        TvocNoseData.download_AsynTask = DownloadTask(this, download_Bar!!, download_min!!, download_text!!).execute(cloudDeviceListItem, token)
        dialog?.dismiss()//結束小視窗
    }
}





