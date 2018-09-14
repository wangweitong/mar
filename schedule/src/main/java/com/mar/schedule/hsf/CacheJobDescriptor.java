package com.mar.schedule.hsf;

import com.mar.model.JobDescriptor;
import com.mar.model.JobStatus;
import com.mar.store.GroupManager;
import com.mar.util.Tuple;
import org.apache.logging.log4j.Logger;

import parquet.org.slf4j.LoggerFactory;

import java.util.Date;

public class CacheJobDescriptor {
	private static parquet.org.slf4j.Logger log=LoggerFactory.getLogger(CacheJobDescriptor.class);
	private GroupManager groupManager;
	
	private final String jobId;
	private  JobDescriptor jobDescriptor;
	private Date lastTime=new Date();
	
	public CacheJobDescriptor(String jobId,GroupManager groupManager){
		this.jobId=jobId;
		this.groupManager=groupManager;
	}
	

	public JobDescriptor getJobDescriptor() {
		if(jobDescriptor==null/* || System.currentTimeMillis()-lastTime.getTime()>60*1000L*/){
			try {
				Tuple<JobDescriptor, JobStatus> job=groupManager.getJobDescriptor(jobId);
				if(job!=null){
					jobDescriptor=job.getX();
				}else{
					jobDescriptor=null;
				}
				/*lastTime=new Date();*/
			} catch (Exception e) {
				log.error("load job descriptor fail",e);
			}
		}
		return jobDescriptor;
	}
	
	public void refresh(){
		Tuple<JobDescriptor, JobStatus> job=groupManager.getJobDescriptor(jobId);
		if(job!=null){
			jobDescriptor=job.getX();
		}else{
			jobDescriptor=null;
		}
	}
		
}
