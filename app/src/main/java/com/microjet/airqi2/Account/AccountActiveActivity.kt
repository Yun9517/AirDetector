package com.microjet.airqi2.Account

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.microjet.airqi2.AsmDataModel
import com.microjet.airqi2.MyApplication
import com.microjet.airqi2.R
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_account_active.*
import org.json.JSONException
import java.io.*
import java.util.*




/**
 * Created by B00170 on 2018/3/8.
 */
class AccountActiveActivity : AppCompatActivity() {
    private var mContext: Context? = null
    private val ASSET_NAME = "BLE_Data.csv"

    private var File: File? = null
    private var cacheFile: File? = null

    @SuppressLint("SdCardPath")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_active)
        mContext = this@AccountActiveActivity.applicationContext
        //dbData2CVSAsyncTasks().execute()

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

        // 0322
        downloadData.setOnClickListener {

        }

        // 03/19 Share to Line
        shareData.setOnClickListener {
            //showDialog("以按下"+"分享按鈕")
            dbData2CVSAsyncTasks()
            file_Provider()
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
    }


//    private inner class dbData2CVSAsyncTasks(){ //: AsyncTask<Void, Void, String>() {
private fun dbData2CVSAsyncTasks() {
    try {
        val AllData = getDbData(Date().day, Date().day)
        writeDataToFile(AllData!!, this@AccountActiveActivity)
    } catch (e: Exception) {
        Log.e("return_body_erro", e.toString())
    }
}

    //20180321
    val DataArrayListOne = ArrayList<String>()
    //val DataArrayListAll= ArrayList< ArrayList<String>>()
    private fun getDbData(startTimeZone: Int, EntTime: Int): ArrayList<String> {
        //很重要同區域才可以叫到同一個東西
        val realm1 = Realm.getDefaultInstance()
        val query1 = realm1.where(AsmDataModel::class.java)
        val result1 = query1.findAll()

        Log.e("資料筆數", result1.size.toString())
        Log.e("所有資料筆數", result1.toString().toString())
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

            val kk = context.getFileStreamPath ("BLE_Data.pdf")
            Log.e("","")
            if (!kk.exists()) {
                val outputStreamWriterData = OutputStreamWriter(context.openFileOutput("BLE_Data.pdf", Context.MODE_APPEND))
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
                mSDFile = context.getFileStreamPath("BLE_Data.pdf")
                mSDFile.delete()
                //mSDFile = context.getFileStreamPath("BLEaddressData.txt");
            }
            val mFileWriter = FileWriter(mSDFile!!, true)
            for (l in 0..data.size/3) {
                for (k in 0..7) {
                    mFileWriter.write(data[k].toString()+",")
                }
                mFileWriter.write("\r\n")
            }
            mFileWriter.close()
            Log.e("Excel檔完成!!路徑為:", mSDFile.path)
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: " + e.toString())
        }
    }

//    private fun cacheFileDoesNotExist(): Boolean {
//        if (cacheFile == null) {
//            cacheFile = File(cacheDir, ASSET_NAME)
//        }
//        return !cacheFile!!.exists()
//    }
//
//    private fun createCacheFile() {
//      //  var inputStream: InputStream? = null
//        var outputStream: OutputStream? = null
//        val BUFFER_SIZE = 2048
//        try {
//        //    inputStream = assets.open(ASSET_NAME)
//            outputStream = FileOutputStream(cacheFile)
//            val buf = ByteArray(BUFFER_SIZE)
//            var len: Int=2048//inputStream.read(buf)
//           // while (len > 0) {
//                outputStream.write(buf, 0, len)
//
//          //  }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        } finally {
//        //   close(inputStream)
//            close(outputStream)
//        }
//    }

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
private fun file_Provider(){
    var mSDFile: File? = null
    mSDFile = this.getFilesDir()
    mSDFile = this@AccountActiveActivity.getFileStreamPath("BLE_Data.pdf")
    val uri = FileProvider.getUriForFile(this, packageName, mSDFile)

    Log.e("#抓取檔案路徑為:",uri.path+"#packageName="+packageName+"#mSDFile="+mSDFile)
    val intent = Intent(Intent.ACTION_SEND)
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.data = uri
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.setType("pdf/plain")
    intent.type = "Application/csv"
    val chooser = Intent.createChooser(intent, title)

    //給目錄臨時的權限
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    // Verify the intent will resolve to at least one activity
    if (intent.resolveActivity(getPackageManager()) != null) {
        startActivity(chooser)
    }
}


}




