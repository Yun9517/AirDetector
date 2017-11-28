package microjet.com.airqi2

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.design.R.id.left
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.Toast
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


/*
    //20171124 Andy月曆的方法聆聽者
    var dateSetListener : DatePickerDialog.OnDateSetListener? = null
    var cal = Calendar.getInstance()
*/




    //Richard 171124
    private var nvDrawer : NavigationView? = null

    //20171128   Andy SQLlite
    internal lateinit var dbrw: SQLiteDatabase
    internal lateinit var dbhelper: AndyAirDBhelper
    internal var tablename = "Andyairtable"


    internal var colstT = arrayOf("編號","溫度", "濕度", "揮發", "二氧")// };
    internal var columT = arrayOf("_id", "temper", "hum", "tvoc", "co2")//,"CO2"};
    internal var co10T = ""
    internal var co11T = ""
    internal var co12T = ""
    internal var co13T = ""
    internal var co14T = ""
    internal var coTTDBTEST = ""
    internal var SaveToDB = arrayOf("20", "20", "20", "20")
    internal var idTTDB: Long = 4
    internal var c: Cursor? = null
    internal var cv: ContentValues? = null
    internal var IDID = ""
    internal var Count: Long = 0
    internal var idTTDBStr = ""




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        uiFindViewById()
        viewPagerInit()
        initActionBar()

        // 電池電量假資料
        batValue = 30


//20171128 Andy SQL
        //*********************************************************************************************
// ------------------------------------------------------------------------------------------------------------------------------------------------

        dbhelper = AndyAirDBhelper(this)
        dbrw = dbhelper.writableDatabase
        Toast.makeText(this,AndyAirDBhelper.database15 + "資料庫是否建立?" + dbrw.isOpen + "版本" + dbrw.version,Toast.LENGTH_LONG).show()
        AddedSQLlite(60000)
        SearchSQLlite()

        //20171128 Andy SQL
        //*********************************************************************************************
// ------------------------------------------------------------------------------------------------------------------------------------------------
        //查詢CO2資料
        //查詢CO2資料
        //查詢CO2資料
    /*
        c = dbrw.query(tablename, columT, null, null, null, null, null)

        //Toast.makeText(MainActivity.this, "現在位置:"+c.getPosition(), 3000).show();
        //Toast.makeText(MainActivity.this, "現在ColumnIndex:"+ c.getString(c.getColumnIndex(columT[0])), 3000).show();


        // 排版
        //co10T += colstT[0] + "\n";
        //co11T += colstT[1] + "\n";
        //co12T += colstT[2] + "\n";
        // co13T += colstT[3] + "\n";
        co14T += colstT[4] + "\n"




        if (c!!.getCount() > 0) {
            //Toast.makeText(MainActivity.this, "測試是否有進去!!  " + c.getCount() + "筆紀錄",Toast.LENGTH_LONG).show();
            c!!.moveToFirst()

            for (i in 0 until c!!.getCount()) {
                Toast.makeText(this@MainActivity, "測試是否進For!!  " + c!!.getCount() + "第" + i + "筆紀錄", Toast.LENGTH_LONG).show()
                co10T += c!!.getString(c!!.getColumnIndex(columT[0])) + "\n"
                // sqlite比較不嚴僅，都用getString()取值即可
                co14T += c!!.getString(4) + "\n"
                Toast.makeText(this@MainActivity, "將新增資料庫CO2第 [ " + (i + 1) + " ]筆CO2:" + c!!.getString(0 + 1) +"ppm", Toast.LENGTH_LONG).show()
                c!!.moveToNext()
            }

            Count = c!!.getCount().toLong()
            //c.close();
            val CountString = Count.toString()
            Toast.makeText(this@MainActivity, "共有" + CountString + "筆CO2紀錄", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this@MainActivity, "資料庫無查CO2資料", Toast.LENGTH_LONG).show()
        }
*/
//*********************************************************************************************
// ------------------------------------------------------------------------------------------------------------------------------------------------

/*
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
        */

        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        setupDrawerContent(nvDrawer)
    }

//20171128 Andy SQL
    private fun AddedSQLlite(intData: Int)
    {
        //////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        cv = ContentValues()
        //idTTDB = c!!.getCount().toLong()
        //Toast.makeText(this,"我要查比數:"+idTTDB,Toast.LENGTH_LONG).show()

        if (SaveToDB[0] !== "" && SaveToDB[1] !== "" && SaveToDB[2] !== "" && SaveToDB[3] !== "" && idTTDB >= 0) {//****************************************************************************
            Toast.makeText(this@MainActivity, "資料滿4筆，我將要存到資料庫去!!!!!", Toast.LENGTH_LONG).show()
            //cv.put(columT[0],c.getPosition());
            cv!!.put(columT[1], SaveToDB[0])
            cv!!.put(columT[2], SaveToDB[1])
            cv!!.put(columT[3], SaveToDB[2])
            cv!!.put(columT[4], SaveToDB[3])
            //新增一筆四個測項資料到資料庫中
            idTTDB = dbrw.insert(tablename, null, cv)
            Toast.makeText(this@MainActivity, "資料滿4，這筆資料內容:" + SaveToDB[0]+","+SaveToDB[1]+","+SaveToDB[2]+","+SaveToDB[3]+",", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this@MainActivity, "溫度、濕度、TVOC、CO2未滿，不新增資料庫", Toast.LENGTH_LONG).show()
        }
        //新增一筆四個測項資料到資料庫中
//////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }
//****************************************************************************************************************************************************
//----------------------------------------------------------------------------------------------------------------------------------------------------------------------


private fun SearchSQLlite() {
    //****************************************************************************************************************************************************
//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //查詢CO2資料
    //查詢CO2資料
    //查詢CO2資料
    c = dbrw.query(tablename, columT, null, null, null, null, null)

    //Toast.makeText(MainActivity.this, "現在位置:"+c.getPosition(), 3000).show();
    //Toast.makeText(MainActivity.this, "現在ColumnIndex:"+ c.getString(c.getColumnIndex(columT[0])), 3000).show();


    // 排版
    //co10T += colstT[0] + "\n";
    //co11T += colstT[1] + "\n";
    //co12T += colstT[2] + "\n";
    // co13T += colstT[3] + "\n";
    co14T += colstT[4] + "\n"


    if (c!!.getCount() > 0) {
        //Toast.makeText(MainActivity.this, "測試是否有進去!!  " + c.getCount() + "筆紀錄",Toast.LENGTH_LONG).show();
        c!!.moveToFirst()

        for (i in 0 until c!!.getCount()) {
            Toast.makeText(this@MainActivity, "測試是否進For!!  " + c!!.getCount() + "第" + i + "筆紀錄", Toast.LENGTH_LONG).show()
            co10T += c!!.getString(c!!.getColumnIndex(columT[0])) + "\n"
            // sqlite比較不嚴僅，都用getString()取值即可
            co14T += c!!.getString(4) + "\n"
            Toast.makeText(this@MainActivity, "將新增資料庫CO2第 [ " + (i + 1) + " ]筆CO2:" + c!!.getString(0 + 1) +"ppm", Toast.LENGTH_LONG).show()
            c!!.moveToNext()
        }

        Count = c!!.getCount().toLong()
        //c.close();
        val CountString = Count.toString()
        Toast.makeText(this@MainActivity, "共有" + CountString + "筆CO2紀錄", Toast.LENGTH_LONG).show()
    } else {
        Toast.makeText(this@MainActivity, "資料庫無查CO2資料", Toast.LENGTH_LONG).show()
    }
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

/*
            R.id.calendarView -> {
                DatePickerDialog(this@MainActivity, R.style.MyDatePickerDialogTheme,
                        dateSetListener,
                        // set DatePickerDialog to point to today's date when it loads up
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            */
              //  R.id.Andy_calendarView ->{
                 //   CalendarShow("月曆","月曆選擇")
           // }


            //點選ActionBAR會返回
            android.R.id.home -> {
                mDrawerToggle!!.onOptionsItemSelected(item)
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun uiFindViewById() {
        mPageVp = this.findViewById(R.id.id_page_vp)
        mDrawerLayout = this.findViewById(R.id.drawer_layout)
        nvDrawer = this.findViewById(R.id.navigation);
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

    // 20171127 Peter 新增：AboutActivity, AirMapActivity
    private fun aboutShow() {
        val i : Intent? = Intent(this, AboutActivity::class.java)
        startActivity(i)
    }

    private fun airmapShow(){
        val i : Intent? = Intent(this, AirMapActivity::class.java)
        startActivity(i)
    }

    // 20171127 Raymond 新增：知識庫activity
    private fun knowledgeShow() {
        val i : Intent? = Intent(this, KnowledgeActivity::class.java)
        startActivity(i)
    }


/*
    //20171124 Andy叫出月曆的方法
    private fun updateDateInView() {
        val myFormat = "yyyy/MM/dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.TAIWAN)
        Toast.makeText(this,sdf.format(cal.getTime()),Toast.LENGTH_LONG).show()

    }
    */


    private fun setupDrawerContent(navigationView: NavigationView?) {
        navigationView?.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            true
        }
    }

    private fun selectDrawerItem(menuItem: MenuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        //var fragment: Fragment? = null
        //val fragmentClass: Class<*>
        when (menuItem.itemId) {
            R.id.nav_add_device -> dialogShow("新增裝置" ,"新增裝置")
            R.id.nav_about -> aboutShow()
            R.id.nav_air_map -> airmapShow()
            //R.id.nav_about -> AboutActivity
            //R.id.nav_second_fragment -> fragmentClass = SecondFragment::class.java
            R.id.nav_knowledge -> knowledgeShow()
            //R.id.nav_third_fragment -> fragmentClass = ThirdFragment::class.java
            //else -> fragmentClass = FirstFragment::class.java
        }

//        try {
//            fragment = fragmentClass.newInstance() as Fragment
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        // Insert the fragment by replacing any existing fragment
//        val fragmentManager = supportFragmentManager
//        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit()

        // Highlight the selected item has been done by NavigationView
        //menuItem.isChecked = true
        // Set action bar title
        title = menuItem.title
        // Close the navigation drawer
        mDrawerLayout?.closeDrawer(GravityCompat.START)
    }

}


