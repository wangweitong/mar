package com.mar.client.operate;

import com.mar.client.ZeusException;
import com.mar.model.JobStatus.TriggerType;

import java.util.Map;

public interface ZeusInvokeService {
	/**
	 * 同步执行一个任务
	 * 直到该任务执行完毕，才会返回
	 * 触发类型只能设置  手动触发  或者  手动恢复
	 * @param uid
	 * @param jobId
	 * @param type
	 * @throws ZeusException
	 * @return map keys:id,historyId,time,status
	 */
	public Map<String, String> syncExecuteJob(String uid, String jobId, TriggerType type, Map<String, String> prop) throws ZeusException;
	/**
	 * 异步调用宙斯任务
	 * 触发类型只能设置  手动触发  或者  手动恢复
	 * @param uid
	 * @param jobId
	 * @param type
	 * @param prop
	 * @throws ZeusException
	 */
	public String asyncExecuteJob(String uid, String jobId, TriggerType type, Map<String, String> prop) throws ZeusException;
	
}
