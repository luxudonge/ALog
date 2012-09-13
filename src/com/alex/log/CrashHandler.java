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
	 * �Ƿ����ռ��������Debug״���¿����� ��Release״���·������ʾ���Ȼ���
	 * */
	public static final boolean DEBUG = true;
	/** ��ϵĬ�ϵ�UncaughtException����ͷ��� */
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	/** CrashHandlerʵ�� */
	private static CrashHandler INSTANCE;
	/** ���ȵ�Context���� */
	private Context mContext;
	/** Ӧ��Properties�������豸����Ϣ�ʹ����ջ��Ϣ */
	private Properties mDeviceCrashInfo = new Properties();
	private static final String VERSION_NAME = "versionName";
	private static final String VERSION_CODE = "versionCode";
	private static final String STACK_TRACE = "STACK_TRACE";
	/** ����ʱ��ļ��������� */
	private static final String CRASH_REPORTER_EXTENSION = ".cr";
	private String DIR;
	
	/** ����ֻ��һ��CrashHandlerʵ�� */
	private CrashHandler() {
	}

	/** ��ȡCrashHandlerʵ�� ������ģʽ */
	public static CrashHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CrashHandler();
		}
		return INSTANCE;
	}

	/**
	 * ��ʼ����ע��Context���� ��ȡ��ϵĬ�ϵ�UncaughtException����ͷ�����
	 * ���ø�CrashHandlerΪ���ȵ�Ĭ�ϴ���ͷ��� ��param ctx
	 */
	public void init(Context ctx) {
		mContext = ctx;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		DIR = ALog.LOG_PATH;
	}

	/**
	 * ��UncaughtException����ʱ��ת��ú���������ͷ�
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		
		if (!handleException(ex) && mDefaultHandler != null) {
			// �����û�û�д���ͷ�������ϵĬ�ϵ��쳣����ͷ���������ͷ�
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			// Sleepһ���ֹͣ����
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
	 * �Զ��������ͷ����ռ�������Ϣ ���ʹ���ʱ��Ȳ��ݾ��ڴ����. �����߿��԰��ձ���ľ������Զ����쳣����ͷ��߼� ��param ex
	 * ��return true:���Ǵ���ͷ��˸��쳣��Ϣ;��Ȼ����false
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
		// Ӧ��Toast����ʾ�쳣��Ϣ
		new Thread() {
			@Override
			public void run() {
				
				Looper.prepare();
				Toast toast = Toast.makeText(mContext, "����ʧ��,�����˳�:\r\n" + msg,
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				// MsgPrompt.showMsg(mContext, "����ʧ����", msg+"\n��ȷ���˳�");
				Looper.loop();
			}
		}.start();
		// �ռ��豸��Ϣ
		collectCrashDeviceInfo(mContext);
		// �������ʱ��ļ�
		saveCrashInfoToFile(ex);
		/*// ���ʹ���ʱ���������
		sendCrashReportsToServer(mContext);*/
		return true;
	}

	/**
	 * �ڷ�������ʱ��, ���Ե��øú�����������ǰû�з��͵ĳʱ�
	 */
	public void sendPreviousReportsToServer() {
		sendCrashReportsToServer(mContext);
	}

	/**
	 * �Ѵ���ʱ����͸�������,�����²����ĺ���ǰû���͵�. ��param ctx
	 */
	private void sendCrashReportsToServer(Context ctx) {
		String[] crFiles = getCrashReportFiles(ctx);
		if (crFiles != null && crFiles.length > 0) {
			TreeSet<String> sortedFiles = new TreeSet<String>();
			sortedFiles.addAll(Arrays.asList(crFiles));
			for (String fileName : sortedFiles) {
				File cr = new File(ctx.getFilesDir(), fileName);
				postReport(cr);
				cr.delete();// ɾ���ѷ��͵ĳʱ�
			}
		}
	}

	private void postReport(File file) {
		// TODO ���ʹ���ʱ���������
	}

	/**
	 * ��ȡ����ʱ��ļ��� ��param ctx ��return
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
	 * ���������Ϣ���ļ��� ��param ex ��return
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
			t.setToNow(); // ȡ����ϵʱ��
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
	 * �ռ�������ɢ���豸��Ϣ
	 * 
	 * ��param ctx
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
		// Ӧ�÷������ռ��豸��Ϣ.��Build���а��������豸��Ϣ,
		// ����: ��ϵ�汾��,�豸������ �Ȱ������Է��ȵ���Ч��Ϣ
		// ������Ϣ��ο�����Ľ�ͼ
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
