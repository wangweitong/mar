package com.mar.jobs.sub.tool;

import com.mar.jobs.JobContext;
import com.mar.jobs.ProcessJob;
import com.mar.util.JobUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 上传附件到HDFS系统的Job
 * 
 * @author zhoufang
 * 
 */
public class UploadHdfsFileJob extends ProcessJob {

	private String localFilePath;
	private String hdfsPath;

	public UploadHdfsFileJob(JobContext jobContext, String localFilePath,
			String hdfsPath) {
		super(jobContext);
		this.localFilePath = localFilePath;
		this.hdfsPath = hdfsPath;
	}

	@Override
	public List<String> getCommandList() {
		List<String> commands = new ArrayList<String>();
		String hadoopCmd = JobUtils.getHadoopCmd(envMap);
		commands.add(hadoopCmd + " fs -copyFromLocal " + localFilePath + " "
				+ hdfsPath);
		return commands;
	}

}
