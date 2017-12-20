package com.microjet.airqi2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class AndyAirDBhelper extends SQLiteOpenHelper {
	//final static String database = "AirDataBase";
	//final static String database2 = "AirDataBase2";
	//final static String database3 = "AirDataBase3";
	//final static String database4 = "AirDataBase4";
    //final static String database5 = "AirDataBase5";
	//final static String database6 = "AirDataBase6";
	//final static String database7 = "AirDataBase7";
	//final static String database8 = "AirDataBase8";
	//final static String database9 = "AirDataBase9";
	//final static String database10 = "AirDataBase10";
	//final static String database11 = "AirDataBase11";
	//final static String database12 = "AirDataBase12";
	//final static String database13 = "AirDataBase13";
    //final static String database14 = "AirDataBase14";
    //final static String database16 = "AndyAirDataBase16";
	//public final static String database17 = "AndyAirDataBase17";
	public final static String database18 = "AndyAirDataBase18";

	//final static int version = 1;
	//final static int version2 = 3;
	//final static int version3 = 3;
	//final static int version4 = 3;
    //final static int version5 = 3;
	//final static int version6 = 3;
	//final static int version7 = 3;
	//final static int version8 = 3;
	//final static int version9 = 3;
	//final static int version10 = 3;
	//final static int version11 = 3;
	//final static int version12 = 3;
	//final static int version13 = 3;
    //final static int version14 = 3;
    //final static int version15 = 4;
	//final static int version16 = 4;
	final static int version17 = 4;
    public static SQLiteDatabase dbrw;

	// 內建的建構子，用來建立資料庫
	public AndyAirDBhelper(Context context, String name, CursorFactory factory,
                           int version) {
		super(context, name, factory, version);

		// TODO Auto-generated constructor stub
	}

	// 自建的建構子，只需傳入一個Context物件即可
	public AndyAirDBhelper(Context context) {
		super(context,database18, null, version17);
	}
	//建立資料表
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
        db.execSQL(String.format("CREATE TABLE `Andyairtable` (\n" +
				"\t`_id`\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
				"\t`collection_time`\tINTEGER,\n" +
				"\t`temper`\tTEXT,\n" +
				"\t`hum`\tTEXT,\n" +
				"\t`tvoc`\tTEXT,\n" +
				"\t`CO2`\tTEXT\n" +
				");"));
	}

	//資料庫更新，刪除資料表，再次呼叫onCreate()重建資料表
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldversion, int newversion) {
		if(newversion>oldversion){
			// TODO Auto-generated method stub
        	db.execSQL("DROP TABLE IF EXISTS airtable");
			onCreate(db);
		}
	}

}
