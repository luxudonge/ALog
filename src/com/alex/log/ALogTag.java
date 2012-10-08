package com.alex.log;

import java.io.Serializable;

/**
 * 
 * 日志标签
 * mTag : 标签名字
 * mInfo : 线程/行数/方法
 * @author Alex.Lu
 *
 */
class ALogTag implements Serializable{

	private static final long serialVersionUID = -7165886269807899291L;

	public String mTag;
	public String mInfo;
}
