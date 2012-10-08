package com.alex.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * 
 * 输出到文件
 * 
 * @author Alex.Lu
 *
 */
class LogToFile {

	private Object mLock = new Object();
	private OutputStream mLogStream;
	private long mFileSize;
	private static final int LOG_MAXSIZE = 1024 * 1024; // double the size
	private static final String LOG_TEMP_FILE = "log_to_file.temp";
	private static final String LOG_LAST_FILE = "log_to_file_last.txt";
	
	private static LogToFile instance;
	
	public static LogToFile getInstance(){
		if(instance == null){
			instance = new LogToFile();
		}
		return instance;
	}
	
	public void logTo(String tag, String msg, int level){
		synchronized (mLock) {
			openLogFileOutStream();
			if (mLogStream != null) {
				try {
					byte[] d =  ALogUtil.getLogStr(tag, msg).getBytes("utf-8");
					if (mFileSize < LOG_MAXSIZE) {
						mLogStream.write(d);
						mLogStream.write("\r\n".getBytes());
						mLogStream.flush();
						mFileSize += d.length;
					} else {
						closeLogFileOutStream();
						renameLogFile();
						logTo(tag, msg, level);
					}

				} catch (Exception e) {
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
				File file = new File(ALog.LOG_PATH,LOG_TEMP_FILE);
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
			
			File file = new File(ALog.LOG_PATH,LOG_TEMP_FILE);
			File destFile = new File(ALog.LOG_PATH,LOG_LAST_FILE);
			if (destFile.exists()) {
				destFile.delete();
			}
			file.renameTo(destFile);
		}
	}
}
