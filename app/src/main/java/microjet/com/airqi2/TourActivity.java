package microjet.com.airqi2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

import java.util.ArrayList;

public class TourActivity extends AppCompatActivity implements
        ViewSwitcher.ViewFactory,View.OnTouchListener {

    private ImageSwitcher is;   //聲明ImageSwitcher布局
    private LinearLayout point_layout;  //聲明導航圓點的布局

    //圖片id數組
    int[] images = new int[] {
            R.drawable.operation_guide_01, R.drawable.operation_guide_02,
            R.drawable.operation_guide_03, R.drawable.operation_guide_04,
            R.drawable.operation_guide_05};

    //實例化存儲導航圓點的集合
    ArrayList<ImageView> points = new ArrayList<>();
    int index;  //聲明index，記錄圖片id數組下標
    float startX;   //手指接觸螢幕時X的坐標（演示左右滑動）
    float endX; //手指離開螢幕時的坐標（演示左右滑動）

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour);
        is = findViewById(R.id.imgSwitcher);
        is.setFactory(this);//通過工廠實現ImageSwitcher
        initpoint();
        is.setOnTouchListener(this);//設置觸摸事件
    }

    //初始化導航圓點的方法
    private void initpoint() {
        point_layout = findViewById(R.id.point_layout);
        int count = point_layout.getChildCount();//獲取布局中圓點數量
        for(int i = 0; i < count; i++) {
            //將布局中的圓點加入到圓點集合中
            points.add((ImageView) point_layout.getChildAt(i));
        }
        //設置第一張圖片（也就是圖片數組的0下標）的圓點狀態為觸摸實心狀態
        points.get(0).setImageResource(R.drawable.page_indicator_focused);
    }

    //設選中圖片對應的導航原點的狀態
    public void setImageBackground(int selectImage) {
        for(int i = 0; i < points.size(); i++) {
            //如果選中圖片的下標等於圓點集合中下標的id，則改變圓點狀態
            if(i == selectImage) {
                points.get(i).setImageResource(R.drawable.page_indicator_focused);
            } else {
                points.get(i).setImageResource(R.drawable.page_indicator_unfocused);
            }
        }
    }

    //實現ViewFactory的方法實例化imageView（這裡未設置ImageView的屬性）
    @Override
    public View makeView() {
        //實例化一個用於切換的ImageView視圖
        ImageView iv = new ImageView(this);
        //默認展示的第一個視圖為images[0]
        iv.setImageResource(images[0]);
        return iv;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //按下螢幕
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            startX = event.getX();//獲取按下螢幕時X軸的坐標
            // 手指抬起
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            endX = event.getX();
            //判斷結束坐標大於起始坐標則為下一張（為避免誤操作，設置30的判斷區間）
            if(startX - endX>30) {
                //三目運算判斷當前圖片已經為最後一張，則從頭開始
                index = index + 1 < images.length? ++ index:0;
                //使用系統自帶的切換出入動畫效果（也可以向ViewFlipper中一樣自定義動畫效果）
                is.setInAnimation(this, android.R.anim.fade_in);
                is.setOutAnimation(this, android.R.anim.fade_out);

                //判斷結束坐標小于于起始坐標則為上一張（為避免誤操作，設置30的判斷區間）
            } else if(endX - startX > 30) {
                //三目運算判斷當前圖片已經為第一張，則上一張為數組內最後一張圖片
                index = index - 1 >= 0? -- index : images.length - 1;
                is.setInAnimation(this, android.R.anim.fade_in);
                is.setOutAnimation(this, android.R.anim.fade_out);
            }
            //設置ImageSwitcher的圖片資源
            is.setImageResource(images[index]);
            //調用方法設置圓點對應狀態
            setImageBackground(index);
        }
        return true;
    }}