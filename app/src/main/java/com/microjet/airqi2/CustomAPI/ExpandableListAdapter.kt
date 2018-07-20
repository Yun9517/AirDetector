package layout

import android.content.Context
import android.widget.TextView
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.database.Cursor
import android.view.LayoutInflater
import android.view.ViewGroup
import android.graphics.Typeface
import android.support.v4.content.ContextCompat.getDrawable
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import android.view.View
import android.widget.ExpandableListView
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import com.microjet.airqi2.*
import com.microjet.airqi2.Account.AccountManagementActivity


/**
 * Created by B00055 on 2018/4/27.
 */
class ExpandableListAdapter(private val mContext: Context, private val mListDataHeader: ArrayList<ExpandedMenuModel> // header titles
                            , // child data in format of header title, child title
                            private val mListDataChild: HashMap<ExpandedMenuModel, ArrayList<String>>, internal var expandList: ExpandableListView, val myPref: PrefObjects = PrefObjects(mContext)) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        val i = mListDataHeader.size
        Log.d("GROUPCOUNT", i.toString())
        return this.mListDataHeader.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        var childCount = 0
        if (groupPosition == 1 || groupPosition == 5 || groupPosition == 6) {
            childCount = this.mListDataChild[this.mListDataHeader[groupPosition]]!!.size
        }
        return childCount
    }

    override fun getGroup(groupPosition: Int): Any {
        return this.mListDataHeader[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        Log.d("CHILD", mListDataChild[this.mListDataHeader[groupPosition]]!!.get(childPosition))
        return mListDataChild[this.mListDataHeader[groupPosition]]!!.get(childPosition)
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val headerTitle = getGroup(groupPosition) as ExpandedMenuModel
        if (convertView == null) {
            val infalInflater = mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = infalInflater.inflate(R.layout.listheader, null)
        }
        val lblListHeader = convertView!!.findViewById<TextView>(R.id.submenu)
        val headerIcon = convertView!!.findViewById<ImageView>(R.id.iconimage)
        // 2018/05/09 Expandable View, Indicator right
        val headerIndicator = convertView!!.findViewById<ImageView>(R.id.indicatorImage)

        lblListHeader.setTypeface(null, Typeface.NORMAL)
        lblListHeader.text = headerTitle.iconName
        headerIcon.setImageResource(headerTitle.iconImg)

        // 2018/07/19 Add FW Indicator in Drawer Group View
        val fwIndicator = convertView!!.findViewById<ImageView>(R.id.img_FW_Indicator)
        fwIndicator.setImageResource(headerTitle.FWIndicator)

        // 2018/05/09 Expandable View, Indicator status
        if (getChildrenCount( groupPosition ) == 0) {
            headerIndicator.visibility = View.GONE
        } else {
            headerIndicator.visibility = View.VISIBLE
            if (isExpanded) {
                headerIndicator.setBackgroundResource((R.drawable.ic_keyboard_arrow_up_grey_400_18dp))
            } else {
                headerIndicator.setBackgroundResource((R.drawable.ic_keyboard_arrow_down_grey_400_18dp))
            }
        }
        // 2018/07/19 Control Group View's Indicator
        if (groupPosition == 6) {
            if (isExpanded) {
                headerIndicator.setBackgroundResource((R.drawable.ic_keyboard_arrow_up_grey_400_18dp))
                fwIndicator.setImageResource(R.color.transparent)
            } else {
                headerIndicator.setBackgroundResource((R.drawable.ic_keyboard_arrow_down_grey_400_18dp))
                if (myPref.getSharePreferenceCheckFWVersion()){
                    fwIndicator.setImageResource(R.drawable.app_android_icon_fw_remind)
                } else {
                    fwIndicator.setImageResource(R.color.transparent)
                }
            }
        }

        return convertView
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val childText = getChild(groupPosition, childPosition) as String
        if (convertView == null) {
            val infalInflater = this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = infalInflater.inflate(R.layout.list_submenu, null)
        }

        // 2018/07/20 Set up val for Child View's FW Indicator
        val txtListChild = convertView!!.findViewById<TextView>(R.id.text_Submenu)
        val childIndicatorImage = convertView!!.findViewById<ImageView>(R.id.childIndicatorImage)//Important, add bridge for Indicator
        val headerTitle = getGroup(childPosition) as ExpandedMenuModel
        txtListChild.text = childText
        childIndicatorImage.setImageResource(headerTitle.FWIndicatorChild) //Important, add bridge for Indicator

        // 2018/07/20 Control Child View's indicator
        if (groupPosition == 6 && childPosition == 1){
            if (myPref.getSharePreferenceCheckFWVersion()){
                childIndicatorImage.setImageResource(R.drawable.app_android_icon_fw_remind)
            } else {
                childIndicatorImage.setImageResource(R.color.transparent)
            }
        }

        return convertView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}