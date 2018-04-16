package com.microjet.airqi2

/**
 * Created by ray650128 on 2018/4/16.
 */
class AirQiDataSet {
    private var TEMPValue: String? = null
    private var HUMIValue: String? = null
    private var TVOCValue: String? = null
    private var ECO2Value: String? = null
    private var PM25Value: String? = null
    private var Created_time: Long? = null
    private var Longitude: Float? = 121.4215f
    private var Latitude: Float? = 24.959817f

    fun getTEMPValue(): String? {
        return TEMPValue
    }

    fun setTEMPValue(TEMPValue: String) {
        this.TEMPValue = TEMPValue
    }

    fun getHUMIValue(): String? {
        return HUMIValue
    }

    fun setHUMIValue(HUMIValue: String) {
        this.HUMIValue = HUMIValue
    }

    fun getTVOCValue(): String? {
        return TVOCValue
    }

    fun setTVOCValue(TVOCValue: String) {
        this.TVOCValue = TVOCValue
    }

    fun getECO2Value(): String? {
        return ECO2Value
    }

    fun setECO2Value(ECO2Value: String) {
        this.ECO2Value = ECO2Value
    }

    fun getPM25Value(): String? {
        return PM25Value
    }

    fun setPM25Value(PM25Value: String) {
        this.PM25Value = PM25Value
    }

    fun getCreated_time(): Long? {
        return Created_time
    }

    fun setCreated_time(Created_time: Long?) {
        this.Created_time = Created_time
    }

    fun getLongitude(): Float? {
        return Longitude
    }

    fun setLongitude(Longitude: Float?) {
        this.Longitude = Longitude
    }

    fun getLatitude(): Float? {
        return Latitude
    }

    fun setLatitude(Latitude: Float?) {
        this.Latitude = Latitude
    }
}