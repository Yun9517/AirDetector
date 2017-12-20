package com.microjet.airqi2.CustomAPI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.github.mikephil.charting.charts.BarChart;

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
                Log.i("getScrollX ", getScrollX() + "" );
                if (getScaleX() > 1 && Math.abs(evt.getX() - downPoint.x) > 5) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
        }
        return super.onTouchEvent(evt);
    }
}