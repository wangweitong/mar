package com.mar.client.crud;

import com.mar.client.ZeusException;
import com.mar.model.JobDescriptorOld;
import com.mar.model.JobDescriptorOld.JobRunTypeOld;

public interface ZeusJobService {

	public JobDescriptorOld createJob(String uid, String jobName, String parentGroup, JobRunTypeOld jobType) throws ZeusException;

	public void updateJob(String uid, JobDescriptorOld jobDescriptor) throws ZeusException;

	public void deleteJob(String uid, String jobId) throws ZeusException;
	
	public JobDescriptorOld getJobDescriptor(String jobId);
	
}
