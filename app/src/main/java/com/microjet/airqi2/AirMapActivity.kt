package com.microjet.airqi2

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat.checkSelfPermission
import android.support.v4.app.ActivityCompat.requestPermissions
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng


/**
 * Created by B00174 on 2017/11/27.
 *
 */

class AirMapActivity: AppCompatActivity(), OnMapReadyCallback {


    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0!!

        /*val howBonBon = LatLng(25.029639, 121.544416)
        mMap.addMarker(MarkerOptions()
                .position(howBonBon)
                .title("好棒棒！"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(howBonBon))*/

        if(checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(this, perms, REQUEST_LOCATION)
        } else {
            mMap.isMyLocationEnabled = true
        }
    }

    private lateinit var mLocationManager: LocationManager
    private lateinit var mMap: GoogleMap

    private val REQUEST_LOCATION = 2
    private val perms: Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_airmap)

        initActionBar()
        initGoogleMapFragment()
    }

    private fun initActionBar() {
        // 取得 actionBar
        val actionBar = supportActionBar
        // 設定顯示左上角的按鈕
        actionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun initGoogleMapFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    private fun initLocation() {
        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE

        val provider = mLocationManager.getBestProvider(criteria, true)

        val location = mLocationManager.getLastKnownLocation(provider)

        if(location != null) {
            Log.i("LOCATION", "${location.latitude}, ${location.longitude}")

            mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            LatLng(location.latitude, location.longitude), 15f)
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        when(requestCode) {
            REQUEST_LOCATION -> {
                mMap.isMyLocationEnabled =
                        grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED

                initLocation()
            }
        }
    }
}