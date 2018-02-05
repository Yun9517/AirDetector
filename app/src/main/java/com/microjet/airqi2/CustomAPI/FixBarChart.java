package com.microjet.airqi2.CustomAPI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.github.mikephil.charting.charts.BarChart;
import com.microjet.airqi2.Fragment.TVOCFragment;

/**
 * Created by ray650128 on 2017/11/30.
 */

public class FixBarChart extends BarChart {

    PointF downPoint = new PointF();

    public FixBarChart(Context context) {
        super(context);
    }

    public FixBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixBarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent evt) {
        switch (evt.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downPoint.x = evt.getX();
                downPoint.y = evt.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i("Gesture getScrollX ", getScrollX() + "" );
                if (getScaleX() > 1 && Math.abs(evt.getX() - downPoint.x) > 5) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
        }
        return super.onTouchEvent(evt);
    }

    /*private float mDownPosX = 0;
    private float mDownPosY = 0;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        final int action = event.getAction();
        Log.e("Gesture FixBarChart", String.valueOf(action));

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownPosX = x;
                mDownPosY = y;
                Log.e("Gesture FixBarChart", "mDownPosX: " + x + "  mDownPosY: " + y);
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaX = Math.abs(x - mDownPosX);
                final float deltaY = Math.abs(y - mDownPosY);
                Log.e("Gesture FixBarChart", "deltaX: " + deltaX + "  deltaY: " + deltaY);
                // 这里是否拦截的判断依据是左右滑动，读者可根据自己的逻辑进行是否拦截

                return !(deltaX < deltaY);
                //break;
        }
        return super.onInterceptTouchEvent(event);
        //return this.isPagingEnabled && super.onInterceptTouchEvent(event);
    }*/
}