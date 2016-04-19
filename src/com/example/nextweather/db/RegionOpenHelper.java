package com.example.nextweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class RegionOpenHelper extends SQLiteOpenHelper {

	public static final String CREATE_PROVINCE = "create table Province ("
			+ "id integer primary key autoincrement, "
			+ "province_name text, "
			+ "province_code text)";	//ʡ�ݽ������
	
	public static final String CREATE_CITY = "create table City ("
			+ "id integer primary key autoincrement, "
			+ "city_name text, "
			+ "city_code text, "
			+ "province_id integer)";	//���н������
	
	public static final String CREATE_COUNTY = "create table County ("
			+ "id integer primary key autoincrement, "
			+ "county_name text, "
			+ "county_code text, "
			+ "city_id integer)";	//���ؼ������
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_PROVINCE);	//����ʡ�ݱ�
		db.execSQL(CREATE_CITY);
		db.execSQL(CREATE_COUNTY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	public RegionOpenHelper(Context context,String name,CursorFactory factory,int version){
		super(context,name,factory,version);
		/*��д���������һ�����췽��ֻ�ǽ��˷�������ʽ���������޸���һ��*/
	}

}
