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

	//ʡ�б�
	private List<Province> provinceList;

	//���б�
	private List<City> cityList;

	//���б�
	private List<County> countyList;

	//ѡ�е�ʡ��
	private Province selectedProvince;

	//ѡ�еĳ���
	private City selectedCity;

	//��ǰѡ�еļ���
	private int currentLevel;

	/*�ж��Ƿ��WeatherInfoActivity�����ת����*/
	private boolean isFromWeatherActivity;	/*�����ͳ�Ա�����������
	��ǰ��Ƿ��WeatherActivity�����ת������*/
	/*�ж���û����ѡ���õĳ���*/
	private Boolean hasSelectedCity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isFromWeatherActivity=getIntent().getBooleanExtra("from_weather_activity", false);
		if(isFromWeatherActivity){
			LogUtil.d("ChooseAreaActivity", "��Ա����isFromWeatherActivity��ֵΪtrue��");
		}else{
			LogUtil.d("ChooseAreaActivity", "��Ա����isFromWeatherActivity��ֵΪfalse��");
		}
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		hasSelectedCity=prefs.getBoolean("city_selected", false);
		if (hasSelectedCity) {
			LogUtil.d("ChooseAreaActivity", "SPsʵ���д�ŵļ�\"city_selected\"��Ӧ��ֵΪtrue��"
					+ "˵��Utility���еı����ȡ����������������Ϣ��saveWeatherInfo()����������ִ�й���");
		} else {
			LogUtil.d("ChooseAreaActivity", "SPsʵ���д�ŵļ�\"city_selected\"��Ӧ��ֵΪfalse��"
					+"Utility���б���������Ϣ�ķ���δ�ɹ�ִ�С�");
		}
		if (hasSelectedCity && !isFromWeatherActivity) {
			/*�Ѿ�ѡ���˳��У��Ҳ��Ǵ�WeatherInfoActivity�����ת�����ģ��Ż���ת��WeatherInfoActivity*/
			Intent intent = new Intent(this, WeatherInfoActivity.class);
			LogUtil.d("ChooseAreaActivity", "Intent������׼���ã�");
			startActivity(intent);
			LogUtil.d("ChooseAreaActivity", "������Ŀ������������Ϣ��");
			finish();	//����/�رյ�ǰ�
			return;
		}else{
			Toast.makeText(this, "����ѡ�����ѯ�ĳ���", Toast.LENGTH_LONG).show();
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
		queryProvinces(); // ����ʡ������
	}

	/* ��ѯȫ�����е�ʡ�����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ��*/
	private void queryProvinces() {
		provinceList = databaseManager.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}
	}

	/* ��ѯѡ��ʡ�����е��У����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ��*/
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

	/* ��ѯѡ���������е��أ����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ��*/
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

	/* ���ݴ���Ĵ��ź����ʹӷ������ϲ�ѯʡ�������ݡ�*/
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
					// ͨ��runOnUiThread()�����ص����̴߳����߼�
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
				// ͨ��runOnUiThread()�����ص����̴߳����߼�
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this,
								"����ʧ��", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	//��ʾ���ȶԻ���
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	//�رս��ȶԻ���
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	/* ����Back���������ݵ�ǰ�ļ������жϣ���ʱӦ�÷������б�ʡ�б�����ֱ���˳���*/
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
			Toast.makeText(this, "���ص���ѯ�м�", Toast.LENGTH_SHORT).show();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
			Toast.makeText(this, "���ص���ѯʡ��", Toast.LENGTH_SHORT).show();
		} else if(isFromWeatherActivity){
			Intent intent=new Intent(this,WeatherInfoActivity.class);
			startActivity(intent);	/*����Back��ʱ������Ǵ�WeatherActivity���
			��ת�����ģ���Ӧ���»ص�WeatherInfoActivity��*/
			Toast.makeText(this, "���Ǵ�������ʾ������ĳ�������ת�����ģ����Ƿ��ص�����������",
					Toast.LENGTH_LONG).show();
			finish();
		} else if(currentLevel==LEVEL_PROVINCE&selectedCity==null) {
			Toast.makeText(this, "�ٴΰ����ؽ��˳�����", Toast.LENGTH_SHORT);
			finish();
		}
	}
}