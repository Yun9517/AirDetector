package com.microjet.airqi2;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by B00055 on 2017/12/3.
 */

public class myData implements Parcelable {
    public myData(String Temp, String Humidity, String Tvoc, String CO2, String Time) {
        Temperatur_Data = Temp;
        Humidy_Data = Humidity;
        TVOC_Data = Tvoc;
        CO2_Data = CO2;
        time = Time;
    }

    public String getTemperatur_Data() {
        return Temperatur_Data;
    }

    public String getHumidy_Data() {
        return Humidy_Data;
    }

    public String getTVOC_Data() {
        return TVOC_Data;
    }

    public String getCO2_Data() {
        return CO2_Data;
    }

    public String getTime() {
        return time;
    }

    String Temperatur_Data;
    String Humidy_Data;
    String TVOC_Data;
    String CO2_Data;
    String time;

    public myData(Parcel in) {
        String[] data = new String[5];

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.Temperatur_Data = data[0];
        this.Humidy_Data = data[1];
        this.TVOC_Data = data[2];
        this.CO2_Data = data[3];
        this.time = data[4];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                this.Temperatur_Data,
                this.Humidy_Data,
                this.TVOC_Data,
                this.CO2_Data,
                this.time
        });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public myData createFromParcel(Parcel in) {
            return new myData(in);
        }

        public myData[] newArray(int size) {
            return new myData[size];
        }
    };
}
