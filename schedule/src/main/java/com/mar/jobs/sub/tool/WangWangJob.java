package com.mar.jobs.sub.tool;

import com.mar.jobs.AbstractJob;
import com.mar.jobs.JobContext;

public class WangWangJob extends AbstractJob{

	public WangWangJob(JobContext jobContext) {
		super(jobContext);
	}

	@Override
	public Integer run() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cancel() {
		canceled=true;
	}

}
