package com.microjet.airqi2;

import android.content.SharedPreferences;
import android.util.Log;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by B00175 on 2018/2/26.
 */

public class RealmMigrations implements RealmMigration {

    private String MACADDR = MyApplication.Companion.getSharePreferenceMAC();

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();
        Log.d("REALMVERSION", schema.toString());
        if (oldVersion < 1L) {
            RealmObjectSchema userSchema = schema.get("AsmDataModel");
            userSchema.addField("UpLoaded", String.class);
            userSchema.transform(obj -> obj.set("UpLoaded", "0"));
            oldVersion++;
        }
        if (oldVersion < 2L) {
            RealmObjectSchema userSchema = schema.get("AsmDataModel");
            userSchema.addField("Longitude", Float.class);
            userSchema.addField("Latitude", Float.class);
            userSchema.addField("MACAddress", String.class);
            userSchema.transform(obj -> obj.set("Longitude", 255f));
            userSchema.transform(obj -> obj.set("Latitude", 255f));
            userSchema.transform(obj -> obj.set("MACAddress", MACADDR));
            oldVersion++;
        }
        if (oldVersion < 3L) {
            RealmObjectSchema userSchema = schema.get("AsmDataModel");
            userSchema.addField("PM10", Integer.class);
            oldVersion++;
        }
    }
}
