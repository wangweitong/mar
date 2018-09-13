package com.mar.broadcast.alarm;

import com.mar.model.*;

import com.mar.schedule.mvc.JobFailListener.ChainException;
import com.mar.store.FollowManagerOld;
import com.mar.store.GroupManagerOld;
import com.mar.store.JobHistoryManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.*;
import org.slf4j.impl.Log4jLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import parquet.org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractZeusAlarm implements ZeusAlarm{
	protected static parquet.org.slf4j.Logger log=LoggerFactory.getLogger(AbstractZeusAlarm.class);
	@Autowired
	protected JobHistoryManager jobHistoryManager;
	@Autowired
	@Qualifier("followManagerOld")
	protected FollowManagerOld followManagerOld;
	@Autowired
	@Qualifier("groupManagerOld")
	protected GroupManagerOld groupManagerOld;
/*	
	@Override
	public void alarm(String historyId, String title, String content,ChainException chain)
			throws Exception {
		JobHistory history=jobHistoryManager.findJobHistory(historyId);
		TriggerType type=history.getTriggerType();
		//获得action_id
		String jobId=history.getJobId();
		//获得job_id
		String tojobId=history.getToJobId();
		List<String> users=new ArrayList<String>();
		if(type==TriggerType.SCHEDULE){
			users=followManagerOld.findActualJobFollowers(tojobId);
		}else{
			users.add(groupManagerOld.getJobDescriptor(tojobId).getX().getOwner());
			if(history.getOperator()!=null){
				if(!users.contains(history.getOperator())){
					users.add(history.getOperator());
				}
			}
		}
		List<String> result=new ArrayList<String>();
		if(chain==null){
			result=users;
		}else{
			for(String uid:users){
				Integer count=chain.getUserCountMap().get(uid);
				if(count==null){
					count=1;
					chain.getUserCountMap().put(uid, count);
				}
				if(count<20){//一个job失败，最多发给同一个人20个报警
					chain.getUserCountMap().put(uid, ++count);
					result.add(uid);
				}
			}
		}
		alarm(jobId, result, title, content);
	}
*/
	@Override
	public void alarm(String historyId, String title, String content,ChainException chain)
			throws Exception {
		JobHistory history=jobHistoryManager.findJobHistory(historyId);
		TriggerType type=history.getTriggerType();
		//获得action_id
		String jobId=history.getJobId();
		//获得job_id
		String tojobId=history.getToJobId();
		List<String> users=new ArrayList<String>();
		if(type==TriggerType.SCHEDULE){
			List<ZeusFollow> zeusFollowers = followManagerOld.findAllFollowers(tojobId);
			List<ZeusFollow> importantContacts = new ArrayList<ZeusFollow>();
			List<ZeusFollow> otherFollowers = new ArrayList<ZeusFollow>();
			for(ZeusFollow zf : zeusFollowers){
				if (zf.isImportant() && ZeusFollow.JobType.equals(zf.getType())) {
					importantContacts.add(zf);
				}else {
					otherFollowers.add(zf);
				}
			}
			String owner = groupManagerOld.getJobDescriptor(tojobId).getX().getOwner();
			
			//首先添加重要联系人，然后是job本身的owner，最后是关注者。
			for(ZeusFollow person : importantContacts){
				if (!users.contains(person.getUid())) {
					users.add(person.getUid());
				}
			}
			if (!users.contains(owner)) {
				users.add(owner);
			}
			for (ZeusFollow other : otherFollowers) {
				if (!users.contains(other.getUid())) {
					users.add(other.getUid());
				}
			}
		}else{
			users.add(groupManagerOld.getJobDescriptor(tojobId).getX().getOwner());
			if(history.getOperator()!=null){
				if(!users.contains(history.getOperator())){
					users.add(history.getOperator());
				}
			}
		}
		List<String> result=new ArrayList<String>();
		if(chain==null){
			result=users;
		}else{
			for(String uid:users){
				Integer count=chain.getUserCountMap().get(uid);
				if(count==null){
					count=1;
					chain.getUserCountMap().put(uid, count);
				}
				if(count<20){//一个job失败，最多发给同一个人20个报警
					chain.getUserCountMap().put(uid, ++count);
					result.add(uid);
				}
			}
		}
		alarm(jobId, result, title, content);
	}
	
	@Override
	public void alarm(String historyId, String title, String content)
			throws Exception {
		alarm(historyId, title, content, null);
	}
	/**
	 * @param jobId anction_id
	 * @param users 用户域账号id
	 * @param title
	 * @param content
	 * @throws Exception
	 */
	public abstract void alarm(String jobId, List<String> users,String title,String content) throws Exception;

}