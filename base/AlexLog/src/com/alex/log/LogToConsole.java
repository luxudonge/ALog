package com.alex.log;

import android.util.Log;

/**
 * 输出到控制台
 * 
 * @author Alex.Lu
 *
 */
class LogToConsole {

	private static LogToConsole instance;
	
	public static LogToConsole getInstance(){
		if(instance == null)
			instance = new LogToConsole();
		return instance;
	}
	
	public void logTo(String tag, String msg, int level){
		switch (level) {
		case Log.DEBUG:
			Log.d(tag, msg);
			break;
		case Log.ERROR:
			Log.e(tag, msg);
			break;
		case Log.INFO:
			Log.i(tag, msg);
			break;
		case Log.VERBOSE:
			Log.v(tag, msg);
			break;
		case Log.WARN:
			Log.w(tag, msg);
			break;
		default:
			break;
		}
	}
}
