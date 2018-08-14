package com.mar.jobs;




public interface Job {

	Integer run() throws Exception;
	
	void cancel();
	
	JobContext getJobContext();
	
	boolean isCanceled();
	
	
}
