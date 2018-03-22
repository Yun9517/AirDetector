package com.microjet.airqi2.Account

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.microjet.airqi2.AsmDataModel
import com.microjet.airqi2.MyApplication
import com.microjet.airqi2.R
import com.microjet.airqi2.TvocNoseData
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_account_active.*
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.*


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

            //20180321
//            dbData2CVSAsyncTasks().execute()
//
//
//
//            val intent = Intent(Intent.ACTION_VIEW)
//            intent.action = Intent.ACTION_SEND
//            //val uri = Uri.fromFile(File(param))
//            //intent.setDataAndType(uri, "application/vnd.ms-excel")
//            //20180319
//            intent.putExtra(Intent.EXTRA_TEXT, "幹出來!")
//            intent.type = "text/plain"
//            startActivity(intent)

            //getExcelFileIntent()



            //PO文字
//            val PACKAGE_NAME = "jp.naver.line.android"
//            val CLASS_NAME = "jp.naver.line.android.activity.selectchat.SelectChatActivity"
//            val intent = Intent(Intent.ACTION_SEND)
//            intent.setClassName(PACKAGE_NAME, CLASS_NAME)
//            intent.type = "text/plain"
//            intent.putExtra(Intent.EXTRA_TEXT, "123321")
//            startActivity(intent)
            val cachePath = applicationContext.externalCacheDir!!.path
            val picFile = File(cachePath, "BLE_Data.csv")
            val intent = Intent(Intent.ACTION_SEND)

            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            //intent.addCategory("android.intent.category.DEFAULT")
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            //val uri = Uri.fromFile(File("/data/user/0/com.microjet.airqi2/files/BLE_Data.csv"))//   /data/data/com.microjet.airqi2/files/BLE_Data.csv
            intent.setType("*/*")
            //intent.setDataAndType(uri, "|application/vnd.ms-excel|xls")
            intent.putExtra(Intent.EXTRA_STREAM,picFile)
            //intent.type = "|application/vnd.ms-excel|csv"
            startActivity(intent)
            Log.e("幹","幹")

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


    private inner class dbData2CVSAsyncTasks : AsyncTask<Void, Void, String>() {
        //@RequiresApi(api = Build.VERSION_CODES.N)
        override fun doInBackground(vararg params: Void): String? {
            var return_body: RequestBody? = null
            var getResponeResult = java.lang.Boolean.parseBoolean(null)
            try {

//                //取得getRequestBody
//                return_body = getRequestBody()
//                //呼叫getResponse取得結果
//                if (return_body!!.contentLength() > 0) {
//                    getResponeResult = getResponse(return_body)
//
//                    if (getResponeResult) {
//                        //呼叫updateDB_UpLoaded方法更改此次傳輸的資料庫資料欄位UpLoaded
//                        val DBSucess = updateDB_UpLoaded()
//                        if (DBSucess) {
//                            Log.e("幹改進去", DBSucess.toString())
//                        }
//                        hasBeenUpLoaded.clear()
//                    } else {
//                        Log.e("幹改失敗拉!!", getResponeResult.toString())
//                    }
//                } else {
//                    Log.e("幹太少筆啦!", return_body.contentLength().toString())
//                }
                try {
                    val AllData = getDbData(Date().day, Date().day)
                    writeDataToFile(AllData!!, this@AccountActiveActivity)
                } catch (e: Exception) {
                    Log.e("寫EXCLE檔失敗", e.toString())
                }


            } catch (e: Exception) {
                Log.e("return_body_erro", e.toString())
            }

            return "已經按下分享"
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            val Dialog = android.app.AlertDialog.Builder(this@AccountActiveActivity).create()
            //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
            //Dialog.setTitle("提示")
            Dialog.setTitle(getString(R.string.remind))
            Dialog.setMessage(result.toString())
            Dialog.setCancelable(false)//讓返回鍵與空白無效
            //Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")
            Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.confirm))
            { dialog, _ ->
                dialog.dismiss()
            }
            Dialog.show()
            //finish()

        }
    }

    //20180321
    val DataArrayListOne = ArrayList<String>()
    val DataArrayListAll= ArrayList< ArrayList<String>>()
    private fun getDbData(startTimeZone: Int, EntTime: Int): ArrayList<String> {
        //很重要同區域才可以叫到同一個東西
        val realm1 = Realm.getDefaultInstance()
        val query1 = realm1.where(AsmDataModel::class.java)
        val result1 = query1.findAll()

        Log.e("未上傳資料筆數", result1.size.toString())
        Log.e("未上傳資料", result1.toString().toString())
        //MyApplication getUUID=new MyApplication();
        val UUID = MyApplication.getPsuedoUniqueID()
        val timestampTEMP: Long? = null
        try {
            if (result1.size > 0) {
                for (i in result1.indices) {
                    //toltoSize++;
                    //hasBeenUpLoaded.add(result1[i]!!.dataId)
                    Log.i("text", "i=" + i + "\n")
                    DataArrayListOne!!.add(result1[i]?.tempValue.toString())
                    DataArrayListOne!!.add(result1[i]?.humiValue.toString())
                    DataArrayListOne!!.add(result1[i]?.tvocValue.toString())
                    DataArrayListOne!!.add(result1[i]?.ecO2Value.toString())
                    DataArrayListOne!!.add(result1[i]?.pM25Value.toString())
                    DataArrayListOne!!.add(result1[i]?.longitude!!.toString())
                    DataArrayListOne!!.add(result1[i]?.latitude!!.toString())
                    DataArrayListOne!!.add(result1[i]?.created_time.toString())
                    //DataArrayListAll!!.add(DataArrayListOne)
                    //Log.e("timestamp", "i=" + i + "timestamp=" + result1[i]!!.created_time!!.toString())
                    //json_arr.put(json_obj_weather)
                    //Log.e("下一筆資料","這筆資料:"+result1.get(i).getCreated_time().toString()+"下一筆資料:"+result1.get(i+1).getCreated_time().toString());

                }
            }else {
                Log.e("未上傳資料筆數", result1.size.toString())
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        realm1.close()

        return DataArrayListOne
    }




    //20180321
    fun writeDataToFile(data: ArrayList<String>, context: Context) {
        try {

            val kk = context.getFileStreamPath("BLE_Data.csv")
            if (!kk.exists()) {
                val outputStreamWriterData = OutputStreamWriter(context.openFileOutput("BLE_Data.csv", Context.MODE_PRIVATE))
                outputStreamWriterData.write("tempValue,humiValue,tvocValue,ecO2Value,pM25Value,longitude,latitude,created_time \r\n")
                outputStreamWriterData.close()
            }
            var mSDFile: File? = null

            //檢查有沒有SD卡裝置
            if (Environment.getExternalStorageState() == Environment.MEDIA_REMOVED) {
                Toast.makeText(applicationContext, "沒有SD卡!!!", Toast.LENGTH_SHORT).show()
                return
            } else {
                //取得SD卡儲存路徑
                mSDFile = Environment.getExternalStorageDirectory()
                mSDFile = context.getFileStreamPath("BLE_Data.csv")
                //mSDFile = context.getFileStreamPath("BLEaddressData.txt");
            }
            //取得SD卡儲存路徑
            //mSDFile = Environment.getExternalStorageDirectory();
            //建立文件檔儲存路徑
            //File mFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/Andy123");
            val mFileWriter = FileWriter(mSDFile!!, true)
            //若沒有檔案儲存路徑時則建立此檔案路徑
            //if(!mSDFile.exists())
            //{
            //    mSDFile.mkdirs();
            //}
            //true新增 false為覆蓋

            //String data03 = "Hello! This is Data02!!";
            //mFileWriter.write(data03);
            //String data02 = "\r\n";
            //mFileWriter.write(data02);
            //String data01 = "This is OutputStream Data01!";
            //mFileWriter.write(data01);
            //mFileWriter.write(data02);
            //String data04 = "This is OutputStream DataTESTANDY!";



            for (k in 0..7) {
                mFileWriter.write(data[k])
            }

            mFileWriter.write("\r\n")


            mFileWriter.close()
            //outputStreamWriterData.close();
            //Toast.makeText(getApplicationContext(), "手動模式已儲存文字"+data, Toast.LENGTH_SHORT).show();
            Log.e("data write to failed: ", data.toString())
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: " + e.toString())
        }
    }

    @SuppressLint("SdCardPath")
//android获取一个用于打开Excel文件的intent
    fun getExcelFileIntent() {
        val intent = Intent("android.intent.action.VIEW")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri = Uri.fromFile(File("/data/data/com.microjet.airqi2/files/BLE_Data.csv"))//   /data/data/com.microjet.airqi2/files/BLE_Data.csv
        intent.setDataAndType(uri, "|application/vnd.ms-excel|csv")
        this.startActivity(intent)
    }
}


