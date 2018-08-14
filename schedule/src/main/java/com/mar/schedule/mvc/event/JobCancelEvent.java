package com.mar.schedule.mvc.event;

import com.mar.mvc.AppEvent;

public class JobCancelEvent extends AppEvent{

	private String jobId;
	public JobCancelEvent(String jobId) {
		super(Events.JobCancel);
		this.jobId=jobId;
	}

}
