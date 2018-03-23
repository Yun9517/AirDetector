package com.microjet.airqi2.Account

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.nfc.Tag
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract.Directory.PACKAGE_NAME
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import com.microjet.airqi2.AsmDataModel
import com.microjet.airqi2.R
import com.microjet.airqi2.TvocNoseData
import kotlinx.android.synthetic.main.activity_account_active.*
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_account_active.*
import kotlinx.android.synthetic.main.drawer_header.*
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import android.text.InputFilter
import android.os.Environment.getExternalStorageDirectory
import android.widget.Toast
import com.github.angads25.filepicker.controller.DialogSelectionListener
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.view.FilePickerDialog
import java.io.File


/**
 * Created by B00170 on 2018/3/8.
 */
class AccountActiveActivity : AppCompatActivity() {
    private var mContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_active)
        mContext = this@AccountActiveActivity.applicationContext


        logout.setOnClickListener {
            val shareToKen = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
            shareToKen.edit().putString("token","") .apply()
//            shareToKen.edit().putString("name","") .apply()
//            shareToKen.edit().putString("email","") .apply()
            //shareToKen.edit().clear().apply()
            finish()
        }

        initActionBar()
        
        //20180310
        val shareMSG = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)

        val myName = shareMSG.getString("name", "")
        val myEmail= shareMSG.getString("email","")
        //val myPassword= shareMSG.getString("password","")
        Log.e("登入後我的資訊","登入中:"+myName + "信箱:" + myEmail) //+ "密碼:" + myPassword)
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
        rename.setOnClickListener {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            val intent = Intent()
            intent.setClass(this@AccountActiveActivity.mContext, AccountNameReplaceActivity::class.java)
            startActivityForResult(intent,1)
//            startActivity(intent)
            //finish()
        }
        //DownloadTask().execute()

        // 03/19 Share to Line
        shareData.setOnClickListener {
         /*   val intent = Intent(Intent.ACTION_VIEW)
            intent.action = Intent.ACTION_SEND

            //20180319
            intent.putExtra(Intent.EXTRA_TEXT, "幹出來!")
            intent.data
            intent.type = "text/plain"
            startActivity(intent)
            */


           /*
            val filename = "read.css"
            val fileContents = "Hello world!"
            this.mContext?.openFileOutput(filename, Context.MODE_PRIVATE).use {
                it!!.write(fileContents.toByteArray())
            }
            val file = File(Environment.getExternalStorageDirectory(), "read.css")

            val uri = Uri.fromFile(file)
            val auxFile = File(uri.getPath())
            val intent = Intent(Intent.ACTION_VIEW)
            intent.action = Intent.ACTION_SEND

            //20180319
            Log.v("uri.path:",uri.path)
            intent.putExtra(Intent.EXTRA_STREAM, uri.path)
        //    intent.data
        */
    //        intent.type = "text/**"
     //       startActivity(intent)
            //PO文字
//            val PACKAGE_NAME = "jp.naver.line.android"
//            val CLASS_NAME = "jp.naver.line.android.activity.selectchat.SelectChatActivity"
//            val intent = Intent(Intent.ACTION_SEND)
//            intent.setClassName(PACKAGE_NAME, CLASS_NAME)
//            intent.type = "text/plain"
//            intent.putExtra(Intent.EXTRA_TEXT, "123321")
//            startActivity(intent)


        ///    assertEquals(file.getAbsolutePath(), auxFile.getAbsolutePath())


          //  NOTE: url.toString() return a String in the format: "file:///mnt/sdcard/myPicture.jpg", whereas url.getPath() returns a String in the format: "/mnt/sdcard/myPicture.jpg"

            val properties = DialogProperties()
            properties.selection_type = DialogConfigs.FILE_SELECT
            properties.root = File(DialogConfigs.DEFAULT_DIR)
            properties.error_dir = File(DialogConfigs.DEFAULT_DIR)
            properties.extensions = null

           var  dialog = FilePickerDialog(this, properties)
            dialog.setTitle("Select files to share")

            dialog.setDialogSelectionListener(DialogSelectionListener { files ->
                if (null == files || files.size == 0) {
                    Toast.makeText(mContext, "Select at least one file to start Share Mode", Toast.LENGTH_SHORT).show()
                    return@DialogSelectionListener
                }
                val intent = Intent(Intent.ACTION_VIEW)
                val uri = Uri.parse(files[0])
                intent.action = Intent.ACTION_SEND
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.type = "image/jpeg"
                startActivity(intent)
            })
            dialog.show()


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

    override fun onStart() {
        super.onStart()
        val share_token = getSharedPreferences("TOKEN", MODE_PRIVATE)
        val _token = share_token.getString("token","")
        Log.e("登入後onStart偷肯:",_token)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onDestroy() {
        super.onDestroy()
//        val share_token = getSharedPreferences("TOKEN", MODE_PRIVATE)
//        val _token = share_token.getString("token","")
//        Log.e("登出後onDestroy偷肯:",_token)
    }

    inner class DownloadTask : AsyncTask<Void, Void, String>() {

        //取MAC
        private val share = getSharedPreferences("MACADDRESS", Context.MODE_PRIVATE)
        private val mDeviceAddress = share.getString("mac", "noValue")
        private val share_token = getSharedPreferences("TOKEN", MODE_PRIVATE)
        private val token = share_token.getString("token","")
        private val phpToken = "Bearer " + token

        private val client = OkHttpClient()
        private val urlBuilder = HttpUrl.parse("http://api.mjairql.com/api/v1/getUserData")!!.newBuilder()
                .addQueryParameter("mac_address", mDeviceAddress)
                //.addQueryParameter("start_time", "0")
                //.addQueryParameter("end_time", "1520941868267")
        private val url = urlBuilder.build().toString()
        private val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("authorization",phpToken)
                .build()

        //jsonBack KEY
        private val TEMPValue = "temperature"
        private val HUMIValue = "humidity"
        private val TVOCValue = "tvoc"
        private val ECO2Value = "eco2"
        private val PM25Value = "pm25"
        private val Created_time = "timestamp"
        private val Longitude = "longitude"
        private val Latitude = "latitude"
        //private val UpLoaded = "UpLoaded"
        //private val MACAddress = "MACAddress"

        override fun doInBackground(vararg params: Void?): String? {
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    return null
                } else {
                    val res = response.body()?.string()
                    val jsonObj = JSONObject(res)
                    val returnResult = jsonObj.get("userData")
                    if (returnResult != "connect info error") {
                        val jsonArr: JSONArray = jsonObj.getJSONArray("userData")
                        val jsonArrSize = jsonArr.length()
                        Log.d("DownloadSize", jsonArrSize.toString())
                        val timeStampArr = arrayListOf<Long>()
                        for (i in 0 until jsonArr.length()) {
                            val timeStamp = jsonArr.getJSONObject(i).getString("timestamp").toLong()
                            timeStampArr.add(timeStamp)
                        }
                        Log.d("Download", timeStampArr.toString())

                        val realm = Realm.getDefaultInstance()
                        for (i in 0 until timeStampArr.size) {
                            val query = realm.where(AsmDataModel::class.java).equalTo("Created_time", timeStampArr[i]).findAll()
                            if (query.isEmpty()) {
                                realm.executeTransaction {
                                    val asmData = realm.createObject(AsmDataModel::class.java, TvocNoseData.getMaxID())
                                    asmData.tvocValue = jsonArr.getJSONObject(i).getString(TVOCValue)
                                    asmData.ecO2Value = jsonArr.getJSONObject(i).getString(ECO2Value)
                                    asmData.tempValue = jsonArr.getJSONObject(i).getString(TEMPValue)
                                    asmData.humiValue = jsonArr.getJSONObject(i).getString(HUMIValue)
                                    asmData.pM25Value = jsonArr.getJSONObject(i).getString(PM25Value)
                                    asmData.created_time = jsonArr.getJSONObject(i).getString(Created_time).toLong()
                                    asmData.latitude = jsonArr.getJSONObject(i).getString(Latitude).toFloat()
                                    asmData.longitude = jsonArr.getJSONObject(i).getString(Longitude).toFloat()
                                    asmData.upLoaded = "1"
                                    asmData.macAddress = mDeviceAddress
                                    Log.d("Download", asmData.toString())
                                }
                            }
                        }
                        realm.close()
                        //Log.d("Download",timeStamp)
                    }
                    Log.d("Download",res.toString())
                    return res
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        override fun onPreExecute() {
            super.onPreExecute()
            // ...
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result != null) {
            }
        }
    }
}