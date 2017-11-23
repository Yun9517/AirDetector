package microjet.com.airqi2.CustomAPI

/**
 * Created by ray650128 on 2017/11/21.
 */

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

import java.util.ArrayList

/**
 * Created by Carson_Ho on 16/5/23.
 */
class FragmentAdapter(fm: FragmentManager, fragmentList: List<Fragment>) : FragmentPagerAdapter(fm) {

    internal var fragmentList: List<Fragment> = ArrayList()

    init {
        this.fragmentList = fragmentList
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }
}
