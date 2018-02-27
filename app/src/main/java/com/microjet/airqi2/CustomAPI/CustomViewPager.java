package com.microjet.airqi2.CustomAPI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ray650128 on 2017/12/8.
 * 
 */

public class CustomViewPager  extends ViewPager {

    public CustomViewPager(Context context) {
        super(context);
        init();
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 設定ViewPager 頁面轉換
        setPageTransformer(true, new VerticalPageTransformer());
        // 先把回彈效果關掉，等找到解法在恢復
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    private class VerticalPageTransformer implements ViewPager.PageTransformer {

        @Override
        public void transformPage(View view, float position) {

            if (position < -1) { // [-Infinity,-1)
                // 這個頁面在螢幕左邊
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                view.setAlpha(1);

                // 抵銷預設的轉換頁面效果
                view.setTranslationX(view.getWidth() * -position);

                // 設定Y的位置從頂部滑入
                float yPosition = position * view.getHeight();
                view.setTranslationY(yPosition);

            } else { // (1,+Infinity]
                // 這個頁面在螢幕右邊
                view.setAlpha(0);
            }
        }
    }

    /**
     * 交換觸控事件的 X＆Y 坐標
     */
    private MotionEvent swapXY(MotionEvent ev) {
        float width = getWidth();
        float height = getHeight();

        float newX = (ev.getY() / height) * width;
        float newY = (ev.getX() / width) * height;

        ev.setLocation(newX, newY);

        return ev;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(swapXY(ev));
    }

    private float mDownPosX = 0;
    private float mDownPosY = 0;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean intercept = super.onInterceptTouchEvent(swapXY(event));
        swapXY(event);

        final float x = event.getX();
        final float y = event.getY();

        final int action = event.getAction();
        Log.e("Gesture CustomViewPager", String.valueOf(action));

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownPosX = x;
                mDownPosY = y;
                Log.e("Gesture CustomViewPager", "mDownPosX: " + x + "  mDownPosY: " + y);
                break;

            case MotionEvent.ACTION_MOVE:
                final float deltaX = Math.abs(x - mDownPosX);
                final float deltaY = Math.abs(y - mDownPosY);
                Log.e("Gesture CustomViewPager", "deltaX: " + deltaX + "  deltaY: " + deltaY);
                // 這裡是否攔截的判斷依據是左右滑動，讀者可根據自己的邏輯進行是否攔截

                if(mDownPosY > 230 && deltaX > 4) {
                    // 左右滑動不攔截
                    Log.e("Gesture CustomViewPager", "is Intercept: " + (deltaX < deltaY));
                    return deltaX < deltaY;
                }
                break;
        }

        //return super.onInterceptTouchEvent(event);
        return intercept;
    }
}