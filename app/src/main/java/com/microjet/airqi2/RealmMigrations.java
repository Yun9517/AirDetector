package com.microjet.airqi2;

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
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();
        Log.d("REALMVERSION", schema.toString());
        if (oldVersion < 1L) {
            RealmObjectSchema userSchema = schema.get("AsmDataModel");
            userSchema.addField("UpLoaded", String.class);
            //userSchema.transform(obj -> obj.set("UpLoaded", "N"));
            userSchema.transform(new RealmObjectSchema.Function() {
                @Override
                public void apply(DynamicRealmObject obj) {
                    obj.set("UpLoaded", "N");
                }
            });
            oldVersion++;
        }
    }
}
