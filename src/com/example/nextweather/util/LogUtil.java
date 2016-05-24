package com.example.nextweather.util;

import android.util.Log;

public class LogUtil {
	//先定义六个整型常量
	public static final int VERBOSE = 1;
	public static final int DEBUG = 2;
	public static final int INFO = 3;
	public static final int WARN = 4;
	public static final int ERROR = 5;
	public static final int NOTHING = 6;
	/*接着又定义一个水平作参考比较的常量，其值可指定为上面六个常量值中的任意一个*/
	public static final int LEVEL = VERBOSE;	//可改为其他常量(的值)
	
	/*接下来提供五个自定义的日志方法，在其内部分别调用Log.v()，
	 * 不过在调用内部方法前都先将水平尺衡量对比判断一下，
	 * 符合设定的条件才可运行内部的方法
	 */
	public static void v(String tag, String msg) {
		if (LEVEL <= VERBOSE) {
			Log.v(tag, msg);
		}
	}
	public static void d(String tag, String msg) {
		if (LEVEL <= DEBUG) {
			Log.d(tag, msg);
		}
	}
	public static void i(String tag, String msg) {
		if (LEVEL <= INFO) {
			Log.i(tag, msg);
		}
	}
	public static void w(String tag, String msg) {
		if (LEVEL <= WARN) {
			Log.w(tag, msg);
		}
	}
	public static void e(String tag, String msg) {
		if (LEVEL <= ERROR) {
			Log.e(tag, msg);
		}
	}
}
