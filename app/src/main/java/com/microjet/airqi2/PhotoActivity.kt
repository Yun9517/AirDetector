package com.microjet.airqi2

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_photo.*
import android.graphics.Bitmap
import android.view.View
import android.widget.TextView
import android.widget.ImageView
import android.view.View.MeasureSpec
import android.widget.RelativeLayout
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Environment.getExternalStorageDirectory
import android.util.Log
import android.widget.Toast
import com.microjet.airqi2.CustomAPI.Utils
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class PhotoActivity : AppCompatActivity() {

    private var addTextBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        btnTakeAShot.setOnClickListener {
            this.btnSave.visibility = View.GONE
            val intent = CameraActivity.newIntent(this, isBackCamera = true, isFullScreen = false, isCountDownEnabled = false, countDownInSeconds = 0)
            startActivityForResult(intent, CameraActivity.VSCAMERAACTIVITY_RESULT_CODE)
        }

        btnSave.setOnClickListener {
            savePicture(addTextBitmap!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == CameraActivity.VSCAMERAACTIVITY_RESULT_CODE) {
            val bitmap = VSBitmapStore.getBitmap(data!!.getIntExtra(CameraActivity.VSCAMERAACTIVITY_IMAGE_ID, 0))
            val rotatedBitmap = rotateBitmap(bitmap, 90f)
            addTextBitmap = setLayout(rotatedBitmap, "看尛", "看尛", "看尛", "看尛", "看尛", "看尛")
            this.imageView.setImageBitmap(addTextBitmap)
            this.btnSave.visibility = View.VISIBLE
        }
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        matrix.postScale(0.5f, 0.5f)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun setLayout(background: Bitmap,
                          tvocText: String, pm25Text: String, pm10Text: String,
                          eco2Text: String, tempText: String, thumiText: String): Bitmap {
        val mInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        //Inflate the layout into a view and configure it the way you like
        val view = RelativeLayout(this)
        mInflater.inflate(R.layout.photo_layout, view, true)

        val img = view.findViewById<View>(R.id.imgBg) as ImageView
        img.setImageBitmap(background)

        val tvoc = view.findViewById<View>(R.id.tvocValue) as TextView
        tvoc.text = tvocText

        val pm25 = view.findViewById<View>(R.id.pm25Value) as TextView
        pm25.text = pm25Text

        val pm10 = view.findViewById<View>(R.id.pm10Value) as TextView
        pm10.text = pm10Text

        val eco2 = view.findViewById<View>(R.id.eco2Value) as TextView
        eco2.text = eco2Text

        val temp = view.findViewById<View>(R.id.tempValue) as TextView
        temp.text = tempText

        val humi = view.findViewById<View>(R.id.rhValue) as TextView
        humi.text = thumiText

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

    private fun savePicture(bitmap: Bitmap) {
        val createPath = File("${android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/ADDWII")
        createPath.mkdir()
        Log.e("Photo", createPath.path)

        val myPref = PrefObjects(this@PhotoActivity)
        try {
            // 取得外部儲存裝置路徑
            val path = createPath.path
            // 開啟檔案
            val imgCount = myPref.getSharePreferenceSaveImageCount()
            val file = File(path, "Image_$imgCount.jpg")
            // 開啟檔案串流
            val out = FileOutputStream(file)
            // 將 Bitmap壓縮成指定格式的圖片並寫入檔案串流
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            // 刷新並關閉檔案串流
            out.flush()
            out.close()
            myPref.setSharePreferenceSaveImageCount(imgCount + 1)
            Utils.toastMakeTextAndShow(this@PhotoActivity, "Save Photo success!", Toast.LENGTH_SHORT)
        } catch (e: FileNotFoundException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

    }
}
