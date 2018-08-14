package com.mar.schedule.mvc.event;

import com.mar.model.JobHistory;
import com.mar.model.JobStatus.TriggerType;
import com.mar.mvc.AppEvent;
import com.mar.schedule.mvc.ZeusJobException;

/**
 * Job失败触发的事件
 * 
 * @author zhoufang
 * 
 */
public class JobFailedEvent extends AppEvent {

	private final JobHistory history;
	private final String jobId;
	private TriggerType triggerType;
	private final ZeusJobException jobException;
	private int runCount = 0;
	private int rollBackTime = 0;

	public JobFailedEvent(String jobId, TriggerType triggerType) {
		this(jobId, triggerType, null, null);
	}

	public JobFailedEvent(String jobId, TriggerType triggerType,
			JobHistory history, ZeusJobException t) {
		super(Events.JobFailed);
		this.jobId = jobId;
		this.triggerType = triggerType;
		this.history = history;
		this.jobException = t;
	}

	public String getJobId() {
		return jobId;
	}

	public TriggerType getTriggerType() {
		return triggerType;
	}

	public JobHistory getHistory() {
		return history;
	}

	public ZeusJobException getJobException() {
		return jobException;
	}

	public int getRunCount() {
		return runCount;
	}

	public void setRunCount(int value) {
		if (triggerType.equals(triggerType.SCHEDULE)) {
			this.runCount = value;
		}
	}

	public int getRollBackTime() {
		return rollBackTime;
	}

	public void setRollBackTime(int value) {
		if (triggerType.equals(triggerType.SCHEDULE)) {
			this.rollBackTime = value;
		}
	}

}
