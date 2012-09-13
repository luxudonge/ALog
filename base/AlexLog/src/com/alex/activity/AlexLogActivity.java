package com.alex.activity;

import java.io.BufferedReader;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.alex.log.ALog;


public class AlexLogActivity extends Activity implements OnClickListener{
	
	Process logCatProc = null;
	BufferedReader reader = null;
	String[] aa = new String[] { "logcat", "-d", "-v", "time", "-s", "AndroidRuntime:E", "-p" };
	TextView tx;
	Button btn;
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}


	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}


	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//ALog.setDebug(false);
		ALog.init(this,null);
		ALog.d("ALog.d");
		ALog.e("ALog.e");
		ALog.i("ALog.i");
		ALog.v("ALog.v");
		ALog.w("ALog.w");
		
		
		Button bu = (Button)findViewById(R.id.btn);
		bu.setOnClickListener(this);
		
		//throw new NullPointerException();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		//throw new RuntimeException();
		//try{
		throw new NullPointerException();
		//}
		//catch (Exception e) {
        //        throw new RuntimeException(
        //            "Unable to instantiate activity " 
        //            + ": " + e.toString(), e);
        //}

	}
}