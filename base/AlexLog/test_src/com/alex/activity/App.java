package com.alex.activity;

import android.app.Application;

import com.alex.log.CrashHandler;

public class App extends Application {
	
	private final static float HEAP_UTILIZATION = 0.75f;   
	private final static int MIN_HEAP_SIZE = 6* 1024* 1024 ;   
	
	@Override  
	public void onCreate() {   
		super.onCreate();   
		
		// �쳣����ͷ�������Ҫ����ͷ�ʱע�͵������伴�ɣ�   
		//CrashHandler crashHandler = CrashHandler.getInstance();    
		// ע��crashHandler    
		//crashHandler.init(getApplicationContext());    
           
	}   
}
