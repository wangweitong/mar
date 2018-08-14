package com.mar.schedule.mvc.event;

import com.mar.mvc.AppEvent;
import com.mar.mvc.EventType;

public class JobLostEvent extends AppEvent {
	
	private final String jobId;
	public JobLostEvent(EventType type,String jobId){
		super(type);
		this.jobId=jobId;
	}
	public String getJobId() {
		return jobId;
	}

}
