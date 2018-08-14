package com.mar.jobs.sub;

import com.mar.jobs.JobContext;
import com.mar.util.RunningJobKeys;

public class HadoopShellJob extends ShellJob{

	public HadoopShellJob(JobContext jobContext) {
		super(jobContext);
		jobContext.getProperties().setProperty(RunningJobKeys.JOB_RUN_TYPE, "HadoopShellJob");
	}
	
	@Override
	public Integer run() throws Exception {
		return super.run();
	}
}
