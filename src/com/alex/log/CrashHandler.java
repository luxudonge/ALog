package com.alex.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;
import java.util.TreeSet;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Looper;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class CrashHandler implements UncaughtExceptionHandler {

	/** Debug Log tag */
	public static final String TAG = "CrashHandler";
	/**
	 * 是否开启日记输出，在Debug状况下开启， 在Release状况下封闭以提示法度机能
	 * */
	public static final boolean DEBUG = true;
	/** 体系默认的UncaughtException处理惩罚类 */
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	/** CrashHandler实例 */
	private static CrashHandler INSTANCE;
	/** 法度的Context对象 */
	private Context mContext;
	/** 应用Properties来保存设备的信息和错误客栈信息 */
	private Properties mDeviceCrashInfo = new Properties();
	private static final String VERSION_NAME = "versionName";
	private static final String VERSION_CODE = "versionCode";
	private static final String STACK_TRACE = "STACK_TRACE";
	/** 错误呈报文件的扩大名 */
	private static final String CRASH_REPORTER_EXTENSION = ".cr";
	private String DIR;
	
	/** 包管只有一个CrashHandler实例 */
	private CrashHandler() {
	}

	/** 获取CrashHandler实例 ，单例模式 */
	public static CrashHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CrashHandler();
		}
		return INSTANCE;
	}

	/**
	 * 初始化，注册Context对象， 获取体系默认的UncaughtException处理惩罚器，
	 * 设置该CrashHandler为法度的默认处理惩罚器 ＠param ctx
	 */
	public void init(Context ctx) {
		mContext = ctx;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		DIR = ALog.LOG_PATH;
	}

	/**
	 * 当UncaughtException产生时会转入该函数来处理惩罚
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		
		if (!handleException(ex) && mDefaultHandler != null) {
			// 若是用户没有处理惩罚则让体系默认的异常处理惩罚器来处理惩罚
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			// Sleep一会后停止法度
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				Log.e(TAG, "Error : ", e);
			}
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(10);
		}
	}

	/**
	 * 自定义错误处理惩罚，收集错误信息 发送错误呈报等操纵均在此完成. 开辟者可以按照本身的景象来自定义异常处理惩罚逻辑 ＠param ex
	 * ＠return true:若是处理惩罚了该异常信息;不然返回false
	 */
	private boolean handleException(Throwable ex) {

		
		if (ex == null) {
			Log.w(TAG, "handleException --- ex==null");
			return true;
		}
		
		final String msg = ex.getLocalizedMessage();
		if (msg == null) {
			return false;
		}
		// 应用Toast来显示异常信息
		new Thread() {
			@Override
			public void run() {
				
				Looper.prepare();
				Toast toast = Toast.makeText(mContext, "法度失足,即将退出:\r\n" + msg,
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				// MsgPrompt.showMsg(mContext, "法度失足啦", msg+"\n点确认退出");
				Looper.loop();
			}
		}.start();
		// 收集设备信息
		collectCrashDeviceInfo(mContext);
		// 保存错误呈报文件
		saveCrashInfoToFile(ex);
		/*// 发送错误呈报到办事器
		sendCrashReportsToServer(mContext);*/
		return true;
	}

	/**
	 * 在法度启动时辰, 可以调用该函数来发送以前没有发送的呈报
	 */
	public void sendPreviousReportsToServer() {
		sendCrashReportsToServer(mContext);
	}

	/**
	 * 把错误呈报发送给办事器,包含新产生的和以前没发送的. ＠param ctx
	 */
	private void sendCrashReportsToServer(Context ctx) {
		String[] crFiles = getCrashReportFiles(ctx);
		if (crFiles != null && crFiles.length > 0) {
			TreeSet<String> sortedFiles = new TreeSet<String>();
			sortedFiles.addAll(Arrays.asList(crFiles));
			for (String fileName : sortedFiles) {
				File cr = new File(ctx.getFilesDir(), fileName);
				postReport(cr);
				cr.delete();// 删除已发送的呈报
			}
		}
	}

	private void postReport(File file) {
		// TODO 发送错误呈报到办事器
	}

	/**
	 * 获取错误呈报文件名 ＠param ctx ＠return
	 */
	private String[] getCrashReportFiles(Context ctx) {
		File filesDir = ctx.getFilesDir();
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(CRASH_REPORTER_EXTENSION);
			}
		};
		return filesDir.list(filter);
	}

	/**
	 * 保存错误信息到文件中 ＠param ex ＠return
	 */
	private String saveCrashInfoToFile(Throwable ex) {
		Writer info = new StringWriter();
		PrintWriter printWriter = new PrintWriter(info);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		String result = info.toString();
		printWriter.close();
		mDeviceCrashInfo.put("EXEPTION", ex.getLocalizedMessage());
		mDeviceCrashInfo.put(STACK_TRACE, result);
		try {
			// long timestamp = System.currentTimeMillis();
			Time t = new Time("GMT+8");
			t.setToNow(); // 取得体系时候
			int date = t.year * 10000 + t.month * 100 + t.monthDay;
			int time = t.hour * 10000 + t.minute * 100 + t.second;
			String fileName = "crash-" + date + "-" + time
					+ CRASH_REPORTER_EXTENSION;
			/*FileOutputStream trace = mContext.openFileOutput("/sdcard/"+fileName,
					Context.MODE_PRIVATE);*/
			FileOutputStream trace = new FileOutputStream(DIR+fileName);
			mDeviceCrashInfo.store(trace, "");
			trace.flush();
			trace.close();
			return fileName;
		} catch (Exception e) {
			Log.e(TAG, "an error occured while writing report file...", e);
		}
		return null;
	}

	/**
	 * 收集法度溃散的设备信息
	 * 
	 * ＠param ctx
	 */
	public void collectCrashDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				mDeviceCrashInfo.put(VERSION_NAME,
						pi.versionName == null ? "not set" : pi.versionName);
				mDeviceCrashInfo.put(VERSION_CODE, "" + pi.versionCode);
			}
		} catch (NameNotFoundException e) {
			Log.e(TAG, "Error while collect package info", e);
		}
		// 应用反射来收集设备信息.在Build类中包含各类设备信息,
		// 例如: 体系版本号,设备临盆商 等帮助调试法度的有效信息
		// 具体信息请参考后面的截图
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				mDeviceCrashInfo.put(field.getName(), "" + field.get(null));
				if (DEBUG) {
					Log.d(TAG, field.getName() + " : " + field.get(null));
				}
			} catch (Exception e) {
				Log.e(TAG, "Error while collect crash info", e);
			}
		}
	}
}
