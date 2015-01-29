package com.alex.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import android.util.Log;

/**
 * 
 * 从logcat里获取数据
 * 
 * @author Alex.Lu
 *
 */
class LogToFormLogcat {

	
	private Object mLock;
	private OutputStream mLogStream;
	private long mFileSize;
	
	private static final int LOG_MAXSIZE = 1024 * 1024; // double the size
	private static final String LOG_TEMP_FILE = "log_form_logcat.temp";
	private static final String LOG_LAST_FILE = "log_form_logcat_last.txt";
	
	private PaintLogThread mPaintLogThread = null;
	private static LogToFormLogcat instance;
	
	public static LogToFormLogcat getInstance(){
		if(instance == null){
			instance = new LogToFormLogcat();
		}
		return instance;
	}
	
	private LogToFormLogcat(){
		mLock = new Object();
		mPaintLogThread = new PaintLogThread();
	}
	
	public void init(){
		if(!mPaintLogThread.isAlive()){
			mPaintLogThread.start();
		}
	}
	
	public void logTo(String msg){
		synchronized (mLock) {
			openLogFileOutStream();
			if (mLogStream != null) {
				try {
					byte[] d = msg.getBytes("utf-8");
					if (mFileSize < LOG_MAXSIZE) {
						mLogStream.write(d);
						mLogStream.write("\r\n".getBytes());
						mLogStream.flush();
						mFileSize += d.length;
					} else {
						closeLogFileOutStream();
						renameLogFile();
						logTo(msg);
					}

				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

			}
		}
	}
	
	
	/**
	 * 获取日志临时文件输入流
	 * 
	 * @return
	 */
	private void openLogFileOutStream() {
		if (mLogStream == null) {
			try {
				File file = new File(ALog.getLogPath(),LOG_TEMP_FILE);
				//File file = new File(mContext.getCacheDir(),LOG_TEMP_FILE);
				if (file.exists()) {
					mLogStream = new FileOutputStream(file, true);
					mFileSize = file.length();
				} else {
					// file.createNewFile();
					mLogStream = new FileOutputStream(file);
					mFileSize = 0;
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 关闭日志输出流
	 */
	private void closeLogFileOutStream() {
		try {
			if (mLogStream != null) {
				mLogStream.close();
				mLogStream = null;
				mFileSize = 0;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * rename log file
	 */
	private void renameLogFile() {
		
		synchronized (mLock) {
			
			File file = new File(ALog.getLogPath(),LOG_TEMP_FILE);
			File destFile = new File(ALog.getLogPath(),LOG_LAST_FILE);
			if (destFile.exists()) {
				destFile.delete();
			}
			file.renameTo(destFile);
		}
	}
	
	class PaintLogThread extends Thread {

		Process mProcess;
		boolean mStop = false;

		public void shutdown() {
			mStop = true;
			if (mProcess != null) {
				mProcess.destroy();
				mProcess = null;
			}
		}

		public void run() {
			// TODO Auto-generated method stub
			try {
				ArrayList<String> commandLine = new ArrayList<String>();
				commandLine.add("logcat");
				//commandLine.add("-f");
				//commandLine.add("/sdcard/ff.txt");
				commandLine.add("-d");
				commandLine.add("-v");
				commandLine.add("time");
				commandLine.add("*:W");

				mProcess = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));

				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));

				String line = null;
				while (!mStop) {
					line = bufferedReader.readLine();
					if (line != null) {
						logTo(line);
					} else {
						if (line == null) {
							Log.i("PaintLogThread:", "readLine==null");
							break;
							// Log.i("PaintLogThread:","PaintLogThread sleep 1000second"
							// );
							// Thread.sleep(1000);
						}
						// Thread.sleep(1000);
					}
				}

				bufferedReader.close();
				if (mProcess != null)
					mProcess.destroy();
				mProcess = null;
				mPaintLogThread = null;

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
}
