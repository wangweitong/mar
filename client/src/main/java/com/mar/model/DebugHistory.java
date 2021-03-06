package com.mar.model;

import com.mar.model.JobDescriptor.JobRunType;
import com.mar.model.JobStatus.Status;

import java.util.Date;

public class DebugHistory{

	private String id;
	private String fileId;
	private Date startTime;
	private Date endTime;
	private String executeHost;
	private Status status;
	private String owner;
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	private Date gmtCreate = new Date();
	private Date gmtModified = new Date();
	private String script;
	private JobRunType jobRunType;
	private LogContent log = new LogContent();
	
	private String host;

	private String hostGroupId;
	
	public LogContent getLog() {
		return log;
	}

	public void setLog(String log) {
		log = log == null ? "" : log;
		this.log.setContent(new StringBuffer(log));
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getExecuteHost() {
		return executeHost;
	}

	public void setExecuteHost(String executeHost) {
		this.executeHost = executeHost;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}

	public Date getGmtModified() {
		return gmtModified;
	}

	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public JobRunType getJobRunType() {
		return jobRunType;
	}

	public void setJobRunType(JobRunType jobRunType) {
		this.jobRunType = jobRunType;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String gethostGroupId() {
		return hostGroupId;
	}

	public void setHostGroupId(String hostGroupId) {
		this.hostGroupId = hostGroupId;
	}
}
