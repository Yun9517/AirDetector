package com.microjet.airqi2.CustomAPI;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import me.kaelaela.verticalviewpager.VerticalViewPager;

/**
 * Created by ray650128 on 2017/12/8.
 */

public class CustomViewPager  extends VerticalViewPager {

    private boolean isPagingEnabled = true;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.isPagingEnabled && super.onTouchEvent(event);
    }

    private float mDownPosX = 0;
    private float mDownPosY = 0;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        final float screenWidth = event.getSize();
        final float x = event.getX();
        final float y = event.getY();

        final int action = event.getAction();
        Log.e("CustomViewPager", String.valueOf(action));

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownPosX = x;
                mDownPosY = y;
                Log.e("CustomViewPager", "mDownPosX: " + x + "  mDownPosY: " + y);
                break;

            case MotionEvent.ACTION_MOVE:
                final float deltaX = Math.abs(x - mDownPosX);
                final float deltaY = Math.abs(y - mDownPosY);
                Log.e("CustomViewPager", "deltaX: " + deltaX + "  deltaY: " + deltaY);
                // 这里是否拦截的判断依据是左右滑动，读者可根据自己的逻辑进行是否拦截

                if(mDownPosY > 230) {
                    if(deltaX > 1) {
                        if (deltaX > deltaY) {// 左右滑动不拦截
                            return false;
                        } else {
                            return true;
                        }
                    }
                }
                break;
        }

        return super.onInterceptTouchEvent(event);
        //return this.isPagingEnabled && super.onInterceptTouchEvent(event);
    }

    public void setPagingEnabled(boolean b) {
        this.isPagingEnabled = b;
    }
}