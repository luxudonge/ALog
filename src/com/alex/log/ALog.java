package com.alex.log;

import java.io.File;

import android.content.Context;
import android.util.Log;

/**
 * 
 * 日志输出
 * 
 * 1.设置是否输出日志
 * 2.以什么方式输出（控制台，到文件，从logcat里抓取）
 * 3.设置输出日志的目录
 * 4.异常捕获功能（只能捕获部分异常）
 * 
 * 默认sdcard的路径为/sdcard/alog/
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
	 * 初始化日志
	 * 默认为/sdcard/alog/
	 * 
	 * @param path 日志存放的目录
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
	 * 启动异常捕获功能
	 * @param context
	 */
	public static void tryCatchStart(Context context){
		CrashHandler crashHandler = CrashHandler.getInstance();    
		crashHandler.init(context);   
	}
	
	/**
	 * 是否输出日志
	 * @param debug
	 */
	public static void setDebug(boolean debug){
		DEBUG = debug;
	}
	
	/**
	 * 
	 * 输出的目标
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
	 * 获取日志标签
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
