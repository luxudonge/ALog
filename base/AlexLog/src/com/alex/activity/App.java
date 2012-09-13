package com.alex.activity;

import android.app.Application;

import com.alex.log.CrashHandler;

public class App extends Application {
	
	private final static float HEAP_UTILIZATION = 0.75f;   
	private final static int MIN_HEAP_SIZE = 6* 1024* 1024 ;   
	
	@Override  
	public void onCreate() {   
		super.onCreate();   
		
		// 异常处理惩罚，不须要处理惩罚时注释掉这两句即可！   
		//CrashHandler crashHandler = CrashHandler.getInstance();    
		// 注册crashHandler    
		//crashHandler.init(getApplicationContext());    
           
	}   
}
