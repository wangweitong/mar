package com.mar.schedule.mvc.event;

import com.mar.mvc.EventType;

public class Events {
	
	public static final EventType Initialize=new EventType();
	//Job执行成功
	public static final EventType JobSucceed=new EventType();
	//Job执行失败
	public static final EventType JobFailed=new EventType();
	//触发Job定时任务
	public static final EventType ScheduleTrigger=new EventType();
	
	//新增Job
//	public static final EventType AddJob=new EventType();
	//更新一个Job
	public static final EventType UpdateJob=new EventType();
	//根据批量更新属于该JobId的Actions
	public static final EventType UpdateActions=new EventType();
	//删除一个Job
//	public static final EventType DeleteJob=new EventType();
	
	public static final EventType JobCancel=new EventType();
}
