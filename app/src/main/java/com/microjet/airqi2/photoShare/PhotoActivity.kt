package com.microjet.airqi2.photoShare

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.location.Geocoder
import android.media.ExifInterface
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
import com.microjet.airqi2.PrefObjects
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

    private lateinit var realm: Realm
    private var result: RealmResults<AsmDataModel>? = null

    private lateinit var mCal: Calendar

    //private val random = Random()

    private val reqCodeCamera = 101
    private val reqCodeWriteStorage = 102

    companion object {
        val CAMERA_INTENT_REQUEST_CODE = 0x11
        val CAMERA_RESULT_OK_CODE = -1
        val CAMERA_RESULT_CANCEL_CODE = 0
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
        if (requestCode == PhotoActivity.CAMERA_INTENT_REQUEST_CODE) {
            when (resultCode) {
                CAMERA_RESULT_OK_CODE -> {
                    if (addTextBitmap != null) {
                        addTextBitmap!!.recycle()
                    }

                    // 讀取暫存檔案
                    val tmpPath = File(this@PhotoActivity.filesDir, "tmp")
                    val tmpFilePath = File(tmpPath, "image.jpg")

                    // 解析成bitmap
                    val bitmap = decodeFile(tmpFilePath.path)

                    if (bitmap != null) {
                        // 判斷照片是直的還是橫的
                        val rotatedBitmap = rotateBitmap(bitmap, getImageOrientation(tmpFilePath))

                        // 修正旋轉角度後釋放原圖佔用的記憶體
                        bitmap.recycle()

                        if (rotatedBitmap != null) {
                            val cropBitmap = cropBitmap(rotatedBitmap)

                            val lastData = queryDatabaseLastData()
                            Log.e("LastData", "$lastData")

                            addTextBitmap = if (lastData != null) {
                                val lastLocation = getLocationName(lastData.latitude.toDouble(), lastData.longitude.toDouble())

                                val tempVal = Utils.convertTemperature(this@PhotoActivity, lastData.tempValue.toFloat())
                                setLayout2(cropBitmap, lastLocation,
                                        "${lastData.tvocValue} ppb",
                                        "${lastData.pM25Value} μg/m³",
                                        tempVal, lastData.created_time)
                            } else {
                                //setLayout(rotatedBitmap, mode, "----", "----", "----", "----", "----", "----")
                                setLayout2(cropBitmap, "Unknown", "----", "----", "----", System.currentTimeMillis())
                            }

                            // 合成後釋放原圖佔用的記憶體
                            rotatedBitmap.recycle()
                            cropBitmap.recycle()

                            //addTextBitmap = setLayout(rotatedBitmap, "看尛", "看尛", "看尛", "看尛", "看尛", "看尛")
                            this.imageView.setImageBitmap(addTextBitmap)
                            file = savePicture(addTextBitmap!!)
                            this.btnShare.visibility = View.VISIBLE
                            this.textNotice.visibility = View.INVISIBLE

                            if (tmpFilePath.exists()) tmpFilePath.delete()
                        }
                    }
                }

                CAMERA_RESULT_CANCEL_CODE -> {
                    this.textNotice.visibility = View.INVISIBLE
                }
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
        //val intent = CameraActivity.newIntent(this, isBackCamera = true, isFullScreen = false, isCountDownEnabled = false, countDownInSeconds = 0)
        //startActivityForResult(intent, CameraActivity.VSCAMERAACTIVITY_RESULT_CODE)

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
    private fun setLayout2(background: Bitmap, cityText: String,
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
        var bitmap: Bitmap? = null
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

        if (filePath != null) {
            bitmap = BitmapFactory.decodeFile(filePath, options)
        }

        return bitmap
    }

    private fun getImageOrientation(imageFile: File): Float {
        var rotate = 0f
        try {
            val exif = ExifInterface(imageFile.absolutePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270f
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180f
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90f
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        Log.e("rotation", "rotation angle is: $rotate")
        return rotate
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        matrix.postScale(0.5f, 0.5f)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun cropBitmap(source: Bitmap): Bitmap {
        val w = source.width // 得到圖片的寬，高
        val h = source.height

        // 4:3 = 4/3 = 1.33
        val cropWidth = if (w > h) (h / 1.33).toInt() else w
        val cropHeight = if (w > h) h else (w * 1.33).toInt()

        val scaleWidth = 1080f.div(cropWidth.toFloat())
        val scaleHeight = 1440f.div(cropHeight.toFloat())

        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)

        Log.e("cropBitmap", "w: $w, h: $h.  cropWidth: $cropWidth, cropHeight: $cropHeight")

        val xPosition = (w - cropWidth) / 2     // 取照片中間
        // val cropBMP = Bitmap.createBitmap(source, xPosition, 0, cropWidth, cropHeight, null, false)

        /*val srcWidth = cropBMP.width.toFloat()
        val srcHeight = cropBMP.height.toFloat()
        val scaleWidth = 1080f.div(srcWidth)
        val scaleHeight = 1440f.div(srcHeight)*/

        return Bitmap.createBitmap(source, xPosition, 0, cropWidth, cropHeight, matrix, true)
    }

    private fun startMediaScannerSync(dir: String) {
        val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        scanIntent.data = Uri.parse("file://$dir")
        sendBroadcast(scanIntent)
    }
}
