package com.microjet.airqi2

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_photo.*
import android.view.View.MeasureSpec
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Paint.ANTI_ALIAS_FLAG


class PhotoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        btnTakeAShot.setOnClickListener {
            val intent = CameraActivity.newIntent(this, isBackCamera = true, isFullScreen = false, isCountDownEnabled = true, countDownInSeconds = 3)
            startActivityForResult(intent, CameraActivity.VSCAMERAACTIVITY_RESULT_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == CameraActivity.VSCAMERAACTIVITY_RESULT_CODE) {
            val bitmap = VSBitmapStore.getBitmap(data.getIntExtra(CameraActivity.VSCAMERAACTIVITY_IMAGE_ID, 0))
            val rotatedBitmap = rotateBitmap(bitmap, 90f)
            val addTextBitmap = drawTextToBitmap(this@PhotoActivity, rotatedBitmap, "看尛")
            this.imageView.setImageBitmap(addTextBitmap)
        }
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun drawTextToBitmap(gContext: Context, gRes: Bitmap, gText: String): Bitmap {
        val resources = gContext.resources
        val scale = resources.displayMetrics.density
        var bitmap = gRes

        var bitmapConfig: android.graphics.Bitmap.Config? = bitmap.config
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true)

        val canvas = Canvas(bitmap)
        // new antialised Paint
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        // text color - #3D3D3D
        paint.color = Color.rgb(61, 61, 61)
        // text size in pixels
        paint.textSize = 14f * scale * 5f
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE)

        // draw text to the Canvas center
        val bounds = Rect()
        paint.getTextBounds(gText, 0, gText.length, bounds)

        //int x = (bitmap.getWidth() - bounds.width()) / 2;
        //int y = (bitmap.getHeight() + bounds.height()) / 2;
        //draw text to the bottom
        val x = (bitmap.width - bounds.width()) / 10 * 9
        val y = (bitmap.height + bounds.height()) / 10 * 9
        canvas.drawText(gText, x.toFloat(), y.toFloat(), paint)

        return bitmap
    }

}
