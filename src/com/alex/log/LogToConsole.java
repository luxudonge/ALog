package com.alex.log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

/**
 * 输出到控制台
 * 
 * @author Alex.Lu
 *
 */
class LogToConsole {

	
	private static boolean IS_SHOW_LOG = true;

    private static final String DEFAULT_MESSAGE = "execute";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final int JSON_INDENT = 4;
    
	private static LogToConsole instance;
	
	public static LogToConsole getInstance(){
		if(instance == null)
			instance = new LogToConsole();
		return instance;
	}
	
	public void logTo(String tag,String info, String msg, int level){
		switch (level) {
		case Log.DEBUG:
			Log.d(tag, info+msg);
			break;
		case Log.ERROR:
			Log.e(tag, info+msg);
			break;
		case Log.INFO:
			Log.i(tag, info+msg);
			break;
		case Log.VERBOSE:
			Log.v(tag, info+msg);
			break;
		case Log.WARN:
			Log.w(tag, info+msg);
			break;
		case ALog.JSON_FORMAT:
			 if (TextUtils.isEmpty(msg)) {
                 Log.d(tag, "Empty or Null json content");
                 return;
             }

             String message = null;

             try {
                 if (msg.startsWith("{")) {
                     JSONObject jsonObject = new JSONObject(msg);
                     message = jsonObject.toString(JSON_INDENT);
                 } else if (msg.startsWith("[")) {
                     JSONArray jsonArray = new JSONArray(msg);
                     message = jsonArray.toString(JSON_INDENT);
                 }
             } catch (JSONException e) {
            	 Log.e(tag,info + " error:" + msg);
                 return;
             }

             printLine(tag, true);
             message = info + LINE_SEPARATOR + message;
             String[] lines = message.split(LINE_SEPARATOR);
             StringBuilder jsonContent = new StringBuilder();
             for (String line : lines) {
                 jsonContent.append("║ ").append(line).append(LINE_SEPARATOR);
             }
             Log.d(tag, jsonContent.toString());
             printLine(tag, false);
			break;
		default:
			break;
		}
	}
	
	private static void printLine(String tag, boolean isTop) {
        if (isTop) {
            Log.d(tag, "╔════════════════════════════");
        } else {
            Log.d(tag, "╚════════════════════════════");
        }
    }
}
