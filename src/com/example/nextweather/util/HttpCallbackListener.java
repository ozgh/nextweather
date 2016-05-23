package com.example.nextweather.util;
/*自定义的接口，定义两个抽象方法，用于回调从服务器中返回的结果*/
public interface HttpCallbackListener {
	void onFinish(String response);
	void onError(Exception e);
}
