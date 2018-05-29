package com.microjet.airqi2


import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.RadioGroup
import com.amap.api.maps2d.AMap
import com.amap.api.maps2d.MapView


/**
 * AMapV1地图中介绍如何显示世界图
 */
class GoldenMapActivity : Activity(), OnClickListener {

    private var mapView: MapView? = null
    private var aMap: AMap? = null
    private var basicmap: Button? = null
    private var rsmap: Button? = null

    private var mRadioGroup: RadioGroup? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activicy_goldenmap)
        mapView = this.findViewById<MapView>(R.id.goldenMap)
        mapView!!.onCreate(savedInstanceState)// 此方法必须重写

        init()
    }

    /**
     * 初始化AMap对象
     */
    private fun init() {
        if (aMap == null) {
            aMap = mapView!!.getMap()
            val uiSettings = aMap?.getUiSettings()
            uiSettings?.setScaleControlsEnabled(true)
            uiSettings?.setMyLocationButtonEnabled(true)
        }
    }

    /**
     * 方法必须重写
     */
    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    /**
     * 方法必须重写
     */
    override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }

    /**
     * 方法必须重写
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState)
    }

    /**
     * 方法必须重写
     */
    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    override fun onClick(v: View?) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
