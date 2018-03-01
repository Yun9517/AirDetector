package com.microjet.airqi2;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by chang on 2017/12/9.
 */

public class AsmDataModel extends RealmObject {
    @PrimaryKey
    private int id;
    private String TEMPValue;
    private String HUMIValue;
    private String TVOCValue;
    private String ECO2Value;
    private String PM25Value;
    private Long Created_time;
    private String UpLoaded = "0";
    private Float Longitude = 121.421151f;
    private Float Latitude = 24.959817f;

    public AsmDataModel(){}

    public Integer getDataId() {
        return id;
    }

    public void setData_id(Integer Data_id) {
        this.id = Data_id;
    }

    public String getTEMPValue() {
        return TEMPValue;
    }

    public void setTEMPValue(String TEMPValue) {
        this.TEMPValue = TEMPValue;
    }

    public String getHUMIValue() {
        return HUMIValue;
    }

    public void setHUMIValue(String HUMIValue) {
        this.HUMIValue = HUMIValue;
    }

    public String getTVOCValue() {
        return TVOCValue;
    }

    public void setTVOCValue(String TVOCValue) {
        this.TVOCValue = TVOCValue;
    }

    public String getECO2Value() {
        return ECO2Value;
    }

    public void setECO2Value(String ECO2Value) {
        this.ECO2Value = ECO2Value;
    }

    public String getPM25Value() {
        return PM25Value;
    }

    public void setPM25Value(String PM25Value) {
        this.PM25Value = PM25Value;
    }

    public Long getCreated_time() {
        return Created_time;
    }

    public void setCreated_time(Long Created_time) {
        this.Created_time = Created_time;
    }

    public String getUpLoaded() { return UpLoaded; }

    public void setUpLoaded(String UpLoaded) { this.UpLoaded = UpLoaded; }

    public Float getLongitude() {
        return Longitude;
    }

    public void setLongitude(Float Longitude) {
        this.Longitude = Longitude;
    }

    public Float getLatitude() {
        return Latitude;
    }

    public void setLatitude(Float Latitude) {
        this.Latitude = Latitude;
    }

}
