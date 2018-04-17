package com.microjet.airqi2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.ViewSwitcher
import kotlinx.android.synthetic.main.activity_tour.*
import java.util.*

class TourActivity : AppCompatActivity(), ViewSwitcher.ViewFactory, View.OnTouchListener {

    //圖片id數組
//    private var images = intArrayOf(
//            R.drawable.operation_guide_01,
//            R.drawable.operation_guide_02,
//            R.drawable.operation_guide_03,
//            R.drawable.operation_guide_04,
//            R.drawable.operation_guide_05)

    private var images = intArrayOf(
            R.drawable.new_guide_01,
            R.drawable.new_guide_02,
            R.drawable.new_guide_03,
            R.drawable.new_guide_04,
            R.drawable.new_guide_05,
            R.drawable.new_guide_06,
            R.drawable.new_guide_07)

    //實例化存儲導航圓點的集合
    private var points = ArrayList<ImageView>()
    private var index: Int = 0  //聲明index，記錄圖片id數組下標
    private var startX: Float = 0.toFloat()   //手指接觸螢幕時X的坐標（演示左右滑動）
    private var endX: Float = 0.toFloat() //手指離開螢幕時的坐標（演示左右滑動）

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour)
        imgSwitcher!!.setFactory(this)//通過工廠實現ImageSwitcher
        initpoint()
        imgSwitcher!!.setOnTouchListener(this)//設置觸摸事件

        skipTour.setOnClickListener {
            mainShow()
        }

    }

    //初始化導航圓點的方法
    private fun initpoint() {
        val count = point_layout!!.childCount//獲取布局中圓點數量
        for (i in 0 until count) {
            //將布局中的圓點加入到圓點集合中
            points.add(point_layout!!.getChildAt(i) as ImageView)
        }
        //設置第一張圖片（也就是圖片數組的0下標）的圓點狀態為觸摸實心狀態
        points[0].setImageResource(R.drawable.page_indicator_focused)
    }

    //設選中圖片對應的導航原點的狀態
    private fun setImageBackground(selectImage: Int) {
        for (i in points.indices) {
            //如果選中圖片的下標等於圓點集合中下標的id，則改變圓點狀態
            if (i == selectImage) {
                points[i].setImageResource(R.drawable.page_indicator_focused)
            } else {
                points[i].setImageResource(R.drawable.page_indicator_unfocused)
            }
        }
    }

    //實現ViewFactory的方法實例化imageView（這裡未設置ImageView的屬性）
    override fun makeView(): View {
        //實例化一個用於切換的ImageView視圖
        val iv = ImageView(this)
        //默認展示的第一個視圖為images[0]
        iv.setImageResource(images[0])
        iv.adjustViewBounds = true
        //iv.scaleType = ImageView.ScaleType.FIT_CENTER
        return iv
    }

    private fun mainShow() {
        val i: Intent? = Intent(this, MainActivity::class.java)
        startActivity(i)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        //按下螢幕
        if (event.action == MotionEvent.ACTION_DOWN) {
            startX = event.x//獲取按下螢幕時X軸的坐標
            // 手指抬起
        } else if (event.action == MotionEvent.ACTION_UP) {
            endX = event.x
            //判斷結束坐標大於起始坐標則為下一張（為避免誤操作，設置30的判斷區間）
            if (startX - endX > 30) {
                //三目運算判斷當前圖片已經為最後一張，則從頭開始
                index = if (index + 1 < images.size) ++index else 0
                //使用系統自帶的切換出入動畫效果（也可以向ViewFlipper中一樣自定義動畫效果）
                imgSwitcher!!.setInAnimation(this, android.R.anim.fade_in)
                imgSwitcher!!.setOutAnimation(this, android.R.anim.fade_out)

                //判斷結束坐標小于于起始坐標則為上一張（為避免誤操作，設置30的判斷區間）
            } else if (endX - startX > 30) {
                //三目運算判斷當前圖片已經為第一張，則上一張為數組內最後一張圖片
                index = if (index - 1 >= 0) --index else images.size - 1
                imgSwitcher!!.setInAnimation(this, android.R.anim.fade_in)
                imgSwitcher!!.setOutAnimation(this, android.R.anim.fade_out)
            }
            //設置ImageSwitcher的圖片資源
            imgSwitcher!!.setImageResource(images[index])
            //調用方法設置圓點對應狀態
            setImageBackground(index)
        }
        return true
    }
}