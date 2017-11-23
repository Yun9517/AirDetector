package microjet.com.airqi2

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import microjet.com.airqi2.CustomAPI.FragmentAdapter
import microjet.com.airqi2.Fragment.MainFragment
import java.util.*

class MainActivity : AppCompatActivity() {

    private val mContext = this@MainActivity

    private val mFragmentList = ArrayList<Fragment>()

    // ViewPager
    private var mPageVp: ViewPager? = null

    // ViewPager目前頁面
    private var currentIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        uiFindViewById()
        viewPagerInit()
    }

    private fun uiFindViewById() {
        mPageVp = this.findViewById(R.id.id_page_vp)
    }

    private fun viewPagerInit() {
        // 加入 Fragment 成員
        val mMainFg = MainFragment()

        mFragmentList.add(mMainFg)

        val mFragmentAdapter = FragmentAdapter(
                this.supportFragmentManager, mFragmentList)
        mPageVp!!.adapter = mFragmentAdapter
        mPageVp!!.currentItem = 0

        mPageVp!!.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            // state：滑動狀態（0，1，2）
            // 1：滑動中 2：滑動完畢 0：閒置。
            override fun onPageScrollStateChanged(state: Int) {

            }

            /**************************
             * position :目前頁面，以及你點擊滑動的頁面 offset:目前頁面偏移的百分比
             * offsetPixels:目前頁面偏移的像素位置
             */
            override fun onPageScrolled(position: Int, offset: Float,
                                        offsetPixels: Int) {

                Log.e("offset:", offset.toString() + "")
            }

            override fun onPageSelected(position: Int) {
                currentIndex = position
            }
        })

    }
}
