package com.microjet.airqi2.photoShare

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.microjet.airqi2.AsmDataModel
import com.microjet.airqi2.CustomAPI.Utils
import com.microjet.airqi2.R
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_photo.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class PhotoActivity : AppCompatActivity() {

    private var addTextBitmap: Bitmap? = null
    private var file: File? = null
    private var imageCropFile: File? = null

    private lateinit var realm: Realm
    private var result: RealmResults<AsmDataModel>? = null

    private lateinit var mCal: Calendar

    private val reqCodeCamera = 101
    private val reqCodeWriteStorage = 102

    companion object {
        val CAMERA_INTENT_REQUEST_CODE = 0x11
        val CROP_INTENT_REQUEST_CODE = 0x12
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        initActionBar()

        mCal = Calendar.getInstance()

        btnTakeAShot.setOnClickListener {
            checkPermissions()
        }

        btnShare.setOnClickListener {
            shareContent(file!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e("onActivityResult", "requestCode: $requestCode, resultCode: $resultCode")
        if (requestCode == PhotoActivity.CAMERA_INTENT_REQUEST_CODE && resultCode == -1) {
            val tmpDir = File(this@PhotoActivity.filesDir, "tmp")
            val file = File(tmpDir, "image.jpg")
            val tmpUri = FileProvider.getUriForFile(this@PhotoActivity, "$packageName.fileprovider", file)
            cropFile(tmpUri)
        } else if (requestCode == PhotoActivity.CROP_INTENT_REQUEST_CODE && resultCode == -1) {
            if(addTextBitmap != null) {
                addTextBitmap!!.recycle()
            }

            val bitmap = decodeFile(imageCropFile!!.path)

            if(bitmap != null) {
                val lastData = queryDatabaseLastData()
                Log.e("LastData", "$lastData")

                addTextBitmap = if (lastData != null) {
                    val lastLocation = getLocationName(lastData.latitude.toDouble(), lastData.longitude.toDouble())

                    val tempVal = Utils.convertTemperature(this@PhotoActivity, lastData.tempValue.toFloat())
                    setTemplate(bitmap, lastLocation,
                            "${lastData.tvocValue} ppb",
                            "${lastData.pM25Value} μg/m³",
                            tempVal, lastData.created_time)
                } else {
                    setTemplate(bitmap, "Unknown", "----", "----", "----", System.currentTimeMillis())
                }

                // 合成後釋放原圖佔用的記憶體
                bitmap.recycle()

                this.imageView.setImageBitmap(addTextBitmap)
                file = savePicture(addTextBitmap!!)
                this.btnShare.visibility = View.VISIBLE
                this.textView4.visibility = View.GONE

                if (imageCropFile!!.exists()) imageCropFile!!.delete()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            reqCodeCamera -> {
                if(ActivityCompat.checkSelfPermission(this@PhotoActivity,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this@PhotoActivity,
                            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), reqCodeWriteStorage)
                } else {
                    callCameraActivity()
                    Log.e("CheckPerm", "Camera Permission Granted...")
                }
            }
            reqCodeWriteStorage -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callCameraActivity()
                    Log.e("CheckPerm", "Write External Storage Permission Granted...")
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request
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

    private fun checkPermissions() {
        when {
            ActivityCompat.checkSelfPermission(this@PhotoActivity, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ->
                ActivityCompat.requestPermissions(this@PhotoActivity,
                        arrayOf(android.Manifest.permission.CAMERA), reqCodeCamera)
            ActivityCompat.checkSelfPermission(this@PhotoActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ->
                ActivityCompat.requestPermissions(this@PhotoActivity,
                        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), reqCodeWriteStorage)
            else -> {
                callCameraActivity()
                Log.e("CheckPerm", "Permission Granted...")
            }
        }
    }

    private fun callCameraActivity() {
        this.btnShare.visibility = View.GONE
        //this.textView4.visibility = View.VISIBLE

        val tmpPath = File(this@PhotoActivity.filesDir, "tmp")
        if(!tmpPath.exists()) tmpPath.mkdirs()

        val tmpFile = File(tmpPath, "image.jpg")
        val outFileUri = FileProvider.getUriForFile(this@PhotoActivity, "$packageName.fileprovider", tmpFile)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outFileUri)

        if(intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, PhotoActivity.CAMERA_INTENT_REQUEST_CODE)
        }

    }

    private fun queryDatabaseLastData(): AsmDataModel? {
        realm = Realm.getDefaultInstance()

        //現在時間實體毫秒
        //val touchTime = if (mCal.get(Calendar.HOUR_OF_DAY) >= 8) mCal.timeInMillis else mCal.timeInMillis + mCal.timeZone.rawOffset
        //將日期設為今天日子加一天減1秒
        //val startTime = touchTime / (3600000 * 24) * (3600000 * 24) - mCal.timeZone.rawOffset
        //val endTime = startTime + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)

        result = realm.where(AsmDataModel::class.java).findAll()
                //.between("Created_time", startTime, endTime)
                //.sort("Created_time", Sort.ASCENDING).findAllAsync()

        return result?.lastOrNull()
    }

    private fun shareContent(imageFile: File) {
        try {
            val photoURI = FileProvider.getUriForFile(this, "$packageName.fileprovider", imageFile)
            Log.e("SHARE", photoURI.path)

            val intent = Intent(Intent.ACTION_SEND)

            intent.data = photoURI
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_STREAM, photoURI)  //圖片的實體路徑

            val chooser = Intent.createChooser(intent, "Share")

            //給目錄臨時的權限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            // Verify the intent will resolve to at least one activity
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(chooser, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun setTemplate(background: Bitmap, cityText: String,
                           tvocText: String, pm25Text: String, tempText: String, tempDate: Long): Bitmap {

        val mInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        //Inflate the layout into a view and configure it the way you like
        val view = RelativeLayout(this)
        mInflater.inflate(R.layout.photo_template, view, true)

        val img = view.findViewById<View>(R.id.imgBg) as ImageView
        img.setImageBitmap(background)

        val city = view.findViewById<View>(R.id.textLocation) as TextView
        city.text = cityText

        val tvoc = view.findViewById<View>(R.id.tvocValue) as TextView
        tvoc.text = Utils.setTextSubscript(tvocText)

        val pm25 = view.findViewById<View>(R.id.pm25Value) as TextView
        pm25.text = Utils.setTextSubscript(pm25Text)

        val temp = view.findViewById<View>(R.id.tempValue) as TextView
        temp.text = Utils.setTextSubscript(tempText)

        val date = view.findViewById<View>(R.id.textDate) as TextView
        val dateFormat = SimpleDateFormat("HH:mm\nMM/dd")
        date.text = dateFormat.format(tempDate)

        //Provide it with a layout params. It should necessarily be wrapping the
        //content as we not really going to have a parent for it.
        view.layoutParams = ViewGroup.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT)

        //Pre-measure the view so that height and width don't remain null.
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))

        //Assign a size and position to the view and all of its descendants
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        //Create the bitmap
        val bitmap = Bitmap.createBitmap(view.measuredWidth,
                view.measuredHeight,
                Bitmap.Config.ARGB_8888)
        //Create a canvas with the specified bitmap to draw into
        val c = Canvas(bitmap)

        //Render this view (and all of its children) to the given Canvas
        view.draw(c)
        return bitmap
    }

    private fun getLocationName(latitude: Double, longitude: Double): String {
        try {
            val geo = Geocoder(this.applicationContext, Locale.getDefault())
            val addresses = geo.getFromLocation(latitude, longitude, 1)
            return if (addresses.isEmpty()) {
                "Waiting for Location"
            } else {
                if (addresses.size > 0) {
                    "${addresses[0].locality }, ${addresses[0].adminArea}"
                } else {
                    "Waiting for Location"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "Waiting for Location"
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun savePicture(bitmap: Bitmap): File? {
        val createPath = File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/ADDWII")
        createPath.mkdir()
        Log.e("Photo", createPath.path)

        try {
            // 取得外部儲存裝置路徑
            val path = createPath.path
            // 開啟檔案
            //val imgCount = myPref.getSharePreferenceSaveImageCount()
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
            val dateAndTime = dateFormat.format(System.currentTimeMillis())
            val file = File(path, "ADDWII_$dateAndTime.jpg")
            // 開啟檔案串流
            val out = FileOutputStream(file)
            // 將 Bitmap壓縮成指定格式的圖片並寫入檔案串流
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            // 刷新並關閉檔案串流
            out.flush()
            out.close()
            Utils.toastMakeTextAndShow(this@PhotoActivity, getString(R.string.text_photo_saved), Toast.LENGTH_SHORT)

            startMediaScannerSync(createPath.path)

            return file
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun decodeFile(filePath: String): Bitmap? {
        val bitmap: Bitmap?
        val options = BitmapFactory.Options()
        options.inPurgeable = true

        try {
            BitmapFactory.Options::class.java.getField("inNativeAlloc").setBoolean(options, true)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }

        bitmap = BitmapFactory.decodeFile(filePath, options)

        return bitmap
    }

    private fun startMediaScannerSync(dir: String) {
        val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        scanIntent.data = Uri.parse("file://$dir")
        sendBroadcast(scanIntent)
    }

    private fun cropFile(sourceUri: Uri) {
        imageCropFile = createImageFile() //創建一個保存裁剪後照片的File
        imageCropFile?.let {
            val intent = Intent("com.android.camera.action.CROP")
            intent.putExtra("crop", "true")
            intent.putExtra("aspectX", 3) //X方向上的比例
            intent.putExtra("aspectY", 4) //Y方向上的比例
            intent.putExtra("outputX", 1080) //裁剪區的寬
            intent.putExtra("outputY", 1440) //裁剪區的高
            intent.putExtra("scale ", true) //是否保留比例
            intent.putExtra("return-data", false) //是否在Intent中返回圖片
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()) //設置輸出圖片的格式
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) //添加這一句表示對目標應用臨時授權該Uri所代表的文件
            intent.setDataAndType(sourceUri, "image/*") //設置數據源,必須是由FileProvider創建的ContentUri

            val imgCropUri = Uri.fromFile(it)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imgCropUri) //設置輸出 不需要ContentUri,否則失敗
            Log.e("tag", "輸入 $sourceUri")
            Log.e("tag", "輸出 ${Uri.fromFile(it)}")
            startActivityForResult(intent, CROP_INTENT_REQUEST_CODE)
        }
    }

    private fun createImageFile(): File? {
        return try {
            val rootFile = File(this@PhotoActivity.getExternalFilesDir("tmp").path)
            if (!rootFile.exists())
                rootFile.mkdirs()
            val fileName = "image_CROP.jpg"
            File(rootFile.absolutePath + File.separator + fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
