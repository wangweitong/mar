package com.mar.jobs.sub.tool;

import com.mar.jobs.JobContext;
import com.mar.jobs.ProcessJob;

import java.util.Arrays;
import java.util.List;

public class CopyLocalFileJob extends ProcessJob{

	private String sourcePath;
	public CopyLocalFileJob(JobContext jobContext,String path) {
		super(jobContext);
		this.sourcePath=path;
	}

	@Override
	public List<String> getCommandList() {
		String command="cp "+sourcePath+" "+getJobContext().getWorkDir();
		return Arrays.asList(command);
	}

}
