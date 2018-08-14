package com.mar.jobs.sub.tool;

import com.mar.broadcast.alarm.MailAlarm;
import com.mar.jobs.AbstractJob;
import com.mar.jobs.JobContext;
import com.mar.model.processer.MailProcesser;
import org.springframework.context.ApplicationContext;

public class MailJob extends AbstractJob{

	private MailAlarm mailAlarm;
	private MailProcesser processer;
	public MailJob(JobContext jobContext,MailProcesser p,ApplicationContext applicationContext) {
		super(jobContext);
		mailAlarm=(MailAlarm) applicationContext.getBean("mailAlarm");
		this.processer=p;
	}
	
	@Override
	public Integer run() throws Exception {
		String render=processer.getTemplate();
		jobContext.getJobHistory().getLog().appendZeus("开始执行发送邮件job");
		try {
			mailAlarm.alarm(getJobContext().getJobHistory().getId(), processer.getSubject(), render);
			jobContext.getJobHistory().getLog().appendZeus("邮件发送成功");
		} catch (Exception e) {
			jobContext.getJobHistory().getLog().appendZeusException(e);
		}
		
		return 0;
	}

	@Override
	public void cancel() {
		canceled=true;
	}

}
