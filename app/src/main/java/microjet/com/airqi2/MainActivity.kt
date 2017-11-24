package microjet.com.airqi2

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.DatePicker
import me.kaelaela.verticalviewpager.VerticalViewPager
import microjet.com.airqi2.CustomAPI.FragmentAdapter
import microjet.com.airqi2.Fragment.MainFragment
import microjet.com.airqi2.Fragment.TVOCFragment
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    //private val mContext = this@MainActivity

    // Fragment 容器
    private val mFragmentList = ArrayList<Fragment>()

    // ViewPager
    private var mPageVp: VerticalViewPager? = null

    // var viewPager = VerticalViewPager()
    // ViewPager目前頁面
    private var currentIndex: Int = 0

    // Drawer & NavigationBar
    private var mDrawerLayout : DrawerLayout? = null
    private var mDrawerToggle : ActionBarDrawerToggle? = null

    // 電池電量數值
    private var batValue: Int = 0



    //20171124 Andy月曆的方法聆聽者
    var dateSetListener : DatePickerDialog.OnDateSetListener? = null
    var cal = Calendar.getInstance()


    // date and time
    private val mYear: Int = 0
    private val mMonth: Int = 0
    private val mDay: Int = 0
    private val mHour: Int = 0
    private val mMinute: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        uiFindViewById()
        viewPagerInit()
        initActionBar()

        // 電池電量假資料
        batValue = 30



        //20171124 Andy月曆實現
        // create an OnDateSetListener
        dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }
        }

        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val menuItem : MenuItem? = menu!!.findItem(R.id.batStatus)

        menuItem!!.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId) {
            R.id.batStatus -> {
                dialogShow(getString(R.string.text_battery_title),
                        getString(R.string.text_battery_value) + batValue + "%")
            }


            R.id.calendarView -> {
                DatePickerDialog(this@MainActivity, R.style.MyDatePickerDialogTheme,
                        dateSetListener,
                        // set DatePickerDialog to point to today's date when it loads up
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)).show()
            }
              //  R.id.Andy_calendarView ->{
                 //   CalendarShow("月曆","月曆選擇")
           // }



            android.R.id.home -> {
                mDrawerToggle!!.onOptionsItemSelected(item)
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun uiFindViewById() {
        mPageVp = this.findViewById(R.id.id_page_vp)
        mDrawerLayout = this.findViewById(R.id.drawer_layout)
    }

    @Suppress("DEPRECATION")
    private fun viewPagerInit() {
        // 加入 Fragment 成員
        val mMainFg = MainFragment()
        val mTvocFg = TVOCFragment()

        mFragmentList.add(mMainFg)
        mFragmentList.add(mTvocFg)

        val mFragmentAdapter = FragmentAdapter(this.supportFragmentManager, mFragmentList)
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

    private fun initActionBar() {
        // 取得 actionBar
        val actionBar = supportActionBar
        // 設定顯示左上角的按鈕
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        // 將 actionBar 和 DrawerLayout 取得關聯
        mDrawerToggle = ActionBarDrawerToggle(this, mDrawerLayout, R.string.text_drawer_open, R.string.text_drawer_close)
        // 同步 actionBarDrawerToggle
        mDrawerToggle!!.syncState()
        // 設定 DrawerLayout 監聽事件
        mDrawerLayout!!.addDrawerListener(mDrawerToggle!!)
    }

    private fun dialogShow(title : String, content : String) {
        val i : Intent? = Intent(this, CustomDialogActivity::class.java)
        val bundle : Bundle? = Bundle()
        bundle!!.putString("dialogTitle", title)
        bundle.putString("dialogContent", content)

        i!!.putExtras(bundle)
        startActivity(i)
    }


    //20171124 Andy叫出月曆的方法
    private fun updateDateInView() {
        val myFormat = "MM/dd/yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.TAIWAN)
        //val pickerData = DatePickerDialog(this, R.style.MyDatePickerDialogTheme, dateSetListener, mYear, mMonth, mDay)

    }

}


