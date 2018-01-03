package com.microjet.airqi2.CustomAPI

/**
 * Created by ray650128 on 2017/12/21.
 */

import android.os.SystemClock
import android.view.View
import java.util.WeakHashMap

abstract class OnMultipleClickListener
/**
 * Constructor
 * @param maxIntervalMsec The max allowed time between clicks, or else discard all previous clicks.
 */
@JvmOverloads constructor(private val mContinuousClickCount: Int, private val mMaxInterval: Long = DEFAULT_MAX_INTERVAL.toLong()) : View.OnClickListener {
    private var mClickIndex: Int = 0
    private val mClickMap: MutableMap<View, Long>

    /**
     * Implement this in your subclass instead of onClick
     * @param v The view that was clicked
     */
    abstract fun onMultipleClick(v: View)

    init {
        mClickIndex = 0
        mClickMap = WeakHashMap()
    }

    override fun onClick(clickedView: View) {
        val previousClickTimestamp = mClickMap[clickedView]
        val currentTimestamp = SystemClock.uptimeMillis()

        mClickMap.put(clickedView, currentTimestamp)

        if (previousClickTimestamp == null) {
            // first click
            mClickIndex = 1
        } else {
            // other click
            if (currentTimestamp - previousClickTimestamp.toLong() < mMaxInterval) {
                ++mClickIndex
                if (mClickIndex >= mContinuousClickCount) {
                    mClickIndex = 0
                    mClickMap.clear()
                    onMultipleClick(clickedView)
                }
            } else {
                // timeout
                mClickIndex = 1
            }
        }
    }

    companion object {
        private val DEFAULT_MAX_INTERVAL = 1000 // ms
    }
}
