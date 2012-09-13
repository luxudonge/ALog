package com.alex.log;

import java.util.Calendar;

/**
 * 
 * @author Alex.Lu
 *
 */
class ALogUtil {
	
	public static String getLogStr(String tag, String msg) {

		Calendar mDate = Calendar.getInstance();
		StringBuffer mBuffer = new StringBuffer();
		
		mDate.setTimeInMillis(System.currentTimeMillis());

		mBuffer.setLength(0);
		mBuffer.append("[");
		mBuffer.append(tag);
		mBuffer.append(" : ");
		mBuffer.append(mDate.get(Calendar.MONTH) + 1);
		mBuffer.append("-");
		mBuffer.append(mDate.get(Calendar.DATE));
		mBuffer.append(" ");
		mBuffer.append(mDate.get(Calendar.HOUR_OF_DAY));
		mBuffer.append(":");
		mBuffer.append(mDate.get(Calendar.MINUTE));
		mBuffer.append(":");
		mBuffer.append(mDate.get(Calendar.SECOND));
		mBuffer.append(":");
		mBuffer.append(mDate.get(Calendar.MILLISECOND));
		mBuffer.append("] ");
		mBuffer.append(msg);

		return mBuffer.toString();
	}
	
}
