package com.microjet.airqi2.CustomAPI;


import android.content.Context;
import android.util.AttributeSet;
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

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return this.isPagingEnabled && super.onInterceptTouchEvent(event);
    }

    public void setPagingEnabled(boolean b) {
        this.isPagingEnabled = b;
    }
}