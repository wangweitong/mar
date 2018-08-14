package com.mar.client.crud;

import com.mar.client.ZeusException;
import com.mar.model.JobDescriptor;
import com.mar.model.JobDescriptor.JobRunType;

public interface ZeusJobService_Old {

	public JobDescriptor createJob(String uid,String jobName,String parentGroup,JobRunType jobType) throws ZeusException;
	
	public void updateJob(String uid,JobDescriptor jobDescriptor) throws ZeusException;
	
	public void deleteJob(String uid,String jobId) throws ZeusException;
	
	public JobDescriptor getJobDescriptor(String jobId);
	
}
