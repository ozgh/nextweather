package com.example.nextweather.util;

import com.example.nextweather.db.DatabaseManager;
import com.example.nextweather.model.City;
import com.example.nextweather.model.County;
import com.example.nextweather.model.Province;

import android.text.TextUtils;

/*解析和处理服务器返回的数据*/
public class Utility {
	/* 解析和处理服务器返回的省级数据*/
	public synchronized static boolean handleProvincesResponse(DatabaseManager databaseManager, String response) {
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					// 将解析出来的数据存储到Province表
					databaseManager.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}

	/* 解析和处理服务器返回的市级数据*/
	public static boolean handleCitiesResponse(DatabaseManager databaseManager,
			String response, int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String c : allCities) {
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					// 将解析出来的数据存储到City表
					databaseManager.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}

	/* 解析和处理服务器返回的县级数据*/
	public static boolean handleCountiesResponse(DatabaseManager databaseManager,
			String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0) {
				for (String c : allCounties) {
					String[] array = c.split("\\|");
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					// 将解析出来的数据存储到County表
					databaseManager.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
}
