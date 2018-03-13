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
import android.view.MenuItem
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener


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
            mMap.uiSettings.isZoomControlsEnabled = true

            initLocation()
        }
    }

    private lateinit var mMap: GoogleMap

    private val REQUEST_LOCATION = 2
    private val perms: Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_airmap)

        initActionBar()
        initGoogleMapFragment()

        createLocationRequest()
    }

    private fun initActionBar() {
        // 取得 actionBar
        val actionBar = supportActionBar
        // 設定顯示左上角的按鈕
        actionBar!!.setDisplayHomeAsUpEnabled(true)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home //對用戶按home icon的處理，本例只需關閉activity，就可返回上一activity，即主activity。
            -> {
                finish()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initGoogleMapFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    private fun initLocation() {
        val client = LocationServices.getFusedLocationProviderClient(this)

        client.lastLocation.addOnCompleteListener(this, {
            if(it.isSuccessful) {
                val location = it.result
            // add & mark by chungyen   if (location !=null)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude), 15f))
            }

            Log.i("LOCATION", "Location Task is Successful: ${it.isSuccessful}")
        })
    }

    private fun createLocationRequest() {
        val locationRequest = LocationRequest()
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 2000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
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