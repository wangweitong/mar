package com.mar.client.operate;

import com.mar.model.JobHistory;

import java.util.Map;

public interface ZeusHelperService {
	/**
	 * 获取Job所有的配置项(包含继承下来的配置项)
	 * @param jobId
	 * @return
	 */
	public Map<String, String> getJobAllProperties(String jobId);
	/**
	 * 获取Job的一次运行记录
	 * @param jobId
	 * @param historyId
	 * @return
	 */
	public JobHistory getJobHistory(String historyId);
	
}
