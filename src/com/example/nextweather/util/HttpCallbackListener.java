package com.example.nextweather.util;
/*�Զ���Ľӿڣ������������󷽷������ڻص��ӷ������з��صĽ��*/
public interface HttpCallbackListener {
	void onFinish(String response);
	void onError(Exception e);
}
