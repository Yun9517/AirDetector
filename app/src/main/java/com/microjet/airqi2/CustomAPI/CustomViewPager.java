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
        // The majority of the magic happens here
        setPageTransformer(true, new VerticalPageTransformer());
        // The easiest way to get rid of the overscroll drawing that happens on the left and right
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    private class VerticalPageTransformer implements ViewPager.PageTransformer {

        @Override
        public void transformPage(View view, float position) {

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                view.setAlpha(1);

                // Counteract the default slide transition
                view.setTranslationX(view.getWidth() * -position);

                //set Y position to swipe in from top
                float yPosition = position * view.getHeight();
                view.setTranslationY(yPosition);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

    /**
     * Swaps the X and Y coordinates of your touch event.
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
                // 这里是否拦截的判断依据是左右滑动，读者可根据自己的逻辑进行是否拦截

                if(mDownPosY > 230) {
                    // 左右滑动不拦截
                    Log.e("Gesture CustomViewPager", "is Intercept: " + (deltaX < deltaY));
                    return deltaX < deltaY;
                }
                break;
        }

        //return super.onInterceptTouchEvent(event);
        return intercept;
    }
}