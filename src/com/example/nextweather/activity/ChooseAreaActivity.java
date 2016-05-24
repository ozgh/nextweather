package com.example.nextweather.activity;

import java.util.ArrayList;
import java.util.List;

import com.example.nextweather.R;
import com.example.nextweather.db.DatabaseManager;
import com.example.nextweather.model.City;
import com.example.nextweather.model.County;
import com.example.nextweather.model.Province;
import com.example.nextweather.util.HttpCallbackListener;
import com.example.nextweather.util.HttpUtil;
import com.example.nextweather.util.LogUtil;
import com.example.nextweather.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {

	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private DatabaseManager databaseManager;
	private List<String> dataList = new ArrayList<String>();

	//省列表
	private List<Province> provinceList;

	//市列表
	private List<City> cityList;

	//县列表
	private List<County> countyList;

	//选中的省份
	private Province selectedProvince;

	//选中的城市
	private City selectedCity;

	//当前选中的级别
	private int currentLevel;

	/*判断是否从WeatherInfoActivity活动中跳转过来*/
	private boolean isFromWeatherActivity;	/*布尔型成员变量用来标记
	当前活动是否从WeatherActivity活动中跳转过来的*/
	/*判断有没有已选定好的城市*/
	private Boolean hasSelectedCity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isFromWeatherActivity=getIntent().getBooleanExtra("from_weather_activity", false);
		if(isFromWeatherActivity){
			LogUtil.d("ChooseAreaActivity", "成员变量isFromWeatherActivity的值为true。");
		}else{
			LogUtil.d("ChooseAreaActivity", "成员变量isFromWeatherActivity的值为false。");
		}
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		hasSelectedCity=prefs.getBoolean("city_selected", false);
		if (hasSelectedCity) {
			LogUtil.d("ChooseAreaActivity", "SPs实例中存放的键\"city_selected\"对应的值为true，"
					+ "说明Utility类中的保存获取并解析出的天气信息的saveWeatherInfo()方法被调用执行过。");
		} else {
			LogUtil.d("ChooseAreaActivity", "SPs实例中存放的键\"city_selected\"对应的值为false，"
					+"Utility类中保存天气信息的方法未成功执行。");
		}
		if (hasSelectedCity && !isFromWeatherActivity) {
			/*已经选择了城市，且不是从WeatherInfoActivity活动中跳转过来的，才会跳转到WeatherInfoActivity*/
			Intent intent = new Intent(this, WeatherInfoActivity.class);
			LogUtil.d("ChooseAreaActivity", "Intent对象已准备好！");
			startActivity(intent);
			LogUtil.d("ChooseAreaActivity", "已启动目标活动——天气信息。");
			finish();	//结束/关闭当前活动
			return;
		}else{
			Toast.makeText(this, "请先选择想查询的城市", Toast.LENGTH_LONG).show();
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		databaseManager = DatabaseManager.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long arg3) {
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(index);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(index);
					queryCounties();
				} else if (currentLevel == LEVEL_COUNTY) {
					String countyCode = countyList.get(index).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this,WeatherInfoActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvinces(); // 加载省级数据
	}

	/* 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。*/
	private void queryProvinces() {
		provinceList = databaseManager.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}
	}

	/* 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。*/
	private void queryCities() {
		cityList = databaseManager.loadCities(selectedProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}

	/* 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。*/
	private void queryCounties() {
		countyList = databaseManager.loadCounties(selectedCity.getId());
		if (countyList.size() > 0) {
			dataList.clear();
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}

	/* 根据传入的代号和类型从服务器上查询省市县数据。*/
	private void queryFromServer(final String code, final String type) {
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code +
					".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvincesResponse(databaseManager,
							response);
				} else if ("city".equals(type)) {
					result = Utility.handleCitiesResponse(databaseManager,
							response, selectedProvince.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountiesResponse(databaseManager,
							response, selectedCity.getId());
				}
				if (result) {
					// 通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}
			}
			@Override
			public void onError(Exception e) {
				// 通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this,
								"加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	//显示进度对话框
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	//关闭进度对话框
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	/* 捕获Back按键，根据当前的级别来判断，此时应该返回市列表、省列表、还是直接退出。*/
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
			Toast.makeText(this, "返回到查询市级", Toast.LENGTH_SHORT).show();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
			Toast.makeText(this, "返回到查询省份", Toast.LENGTH_SHORT).show();
		} else if(isFromWeatherActivity){
			Intent intent=new Intent(this,WeatherInfoActivity.class);
			startActivity(intent);	/*按下Back键时，如果是从WeatherActivity活动中
			跳转过来的，则应重新回到WeatherInfoActivity中*/
			Toast.makeText(this, "若是从天气显示界面更改城市中跳转过来的，则还是返回到天气界面中",
					Toast.LENGTH_LONG).show();
			finish();
		} else if(currentLevel==LEVEL_PROVINCE&selectedCity==null) {
			Toast.makeText(this, "再次按返回将退出程序", Toast.LENGTH_SHORT);
			finish();
		}
	}
}