package com.alex.log;

import java.io.File;

import android.content.Context;
import android.util.Log;

/**
 * 
 * ��־���
 * 
 * 1.�����Ƿ������־
 * 2.��ʲô��ʽ���������̨�����ļ�����logcat��ץȡ��
 * 3.���������־��Ŀ¼
 * 4.�쳣�����ܣ�ֻ�ܲ��񲿷��쳣��
 * 
 * Ĭ��sdcard��·��Ϊ/sdcard/alog/
 * 
 * @author Alex.Lu
 * 
 */
public class ALog {
	
	static String LOG_PATH = "/sdcard/alog/"; 
	
	public static final int TO_CONSOLE = 0x1;
	public static final int TO_FILE = 0x10;
	public static final int TO_FROM_LOGCAT = 0x100;
	public static final int TO_SCREEN = 0x1000;
	
	private static int TO_DEST = TO_CONSOLE;
	
	private static boolean DEBUG = true;
	
	private static ALog mLog;
	private static Context mContext;
	
	private synchronized static ALog getInstance(){
		if(mLog == null){
			mLog = new ALog();
		}
		return mLog;
	}
	
	/**
	 * ��ʼ����־
	 * Ĭ��Ϊ/sdcard/alog/
	 * 
	 * @param path ��־��ŵ�Ŀ¼
	 */
	public static void init(Context context,String path){
		mContext = context;
		if(path != null){
			LOG_PATH = path;
		}
		File destDir = new File(LOG_PATH);
		if (!destDir.exists()) {
		   destDir.mkdirs();
		}
	}
	
	/**
	 * �����쳣������
	 * @param context
	 */
	public static void tryCatchStart(Context context){
		CrashHandler crashHandler = CrashHandler.getInstance();    
		crashHandler.init(context);   
	}
	
	/**
	 * �Ƿ������־
	 * @param debug
	 */
	public static void setDebug(boolean debug){
		DEBUG = debug;
	}
	
	/**
	 * 
	 * �����Ŀ��
	 * @param flag TO_CONSOLE/TO_FILE/TO_FROM_LOGCAT
	 * 
	 */
	public static void setToDest(int flag){
		TO_DEST = flag;
	}
	
	public static void v(String msg){
		getInstance().log(msg, Log.VERBOSE);
	}
	
	public static void d(String msg){
		getInstance().log(msg, Log.DEBUG);
	}
	
	public static void i(String msg){
		getInstance().log(msg, Log.INFO);
	}
	
	public static void w(String msg){
		getInstance().log(msg, Log.WARN);
	}
	
	public static void e(String msg){
		getInstance().log(msg, Log.ERROR);
	}
	
	
	private void log(String msg,int level){
		if(!DEBUG){
			return ;
		}
		
		ALogTag logTag = getLogTag();
		String tag = logTag.mTag;
		StringBuffer msgBuf = new StringBuffer();
		msgBuf.append( logTag.mInfo);
		msgBuf.append(msg);
		if((TO_DEST & TO_CONSOLE) != 0){
			LogToConsole.getInstance().logTo(tag, msgBuf.toString(), level);
		}
		if((TO_DEST & TO_FILE) != 0){
			LogToFile.getInstance().logTo(tag, msgBuf.toString(), level);
		}
		if((TO_DEST & TO_FROM_LOGCAT) != 0){
			LogToFormLogcat.getInstance().init();
		}
		if((TO_DEST & TO_SCREEN) != 0){
			LogToScreen.getInstance(mContext).logTo(tag, msgBuf.toString(), level);
		}
	}
	
	/**
	 * ��ȡ��־��ǩ
	 * @return
	 */
    private ALogTag getLogTag()  
    {  
    	
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();  
        if(sts == null)  
        {  
            return null;  
        }  
        for(StackTraceElement st : sts)  
        {  
            if(st.isNativeMethod())  
            {  
                continue;  
            }  
            if(st.getClassName().equals(Thread.class.getName()))  
            {  
                continue;  
            }  
            if(st.getClassName().equals(this.getClass().getName()))  
            {  
                continue;  
            }
            
            ALogTag tag = new ALogTag();
            tag.mTag = st.getFileName();
            StringBuffer stuf = new StringBuffer();
            stuf.append("[ ");
            stuf.append(Thread.currentThread().getName());
            stuf.append("/");
            stuf.append(st.getLineNumber());
            stuf.append("/");
            stuf.append(st.getMethodName());
            stuf.append(" ]");
            tag.mInfo = stuf.toString();
            return tag;  
        }  
        return null;  
    } 
    
}
