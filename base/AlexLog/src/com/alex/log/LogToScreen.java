package com.alex.log;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

public class LogToScreen {

	private static LogToScreen instance;
	private NotificationManager mNotificationManager;
	private Notification notif;
	public static LogToScreen getInstance(Context context){
		if(instance == null){
			instance = new LogToScreen(context);
		}
		return instance;
	}
	
	
	private LogToScreen(Context context){
		 // our custom view
		 mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
		 notif = new Notification();
	}
	
	public void logTo(String tag, String msg, int level){
		
	}
	
}
