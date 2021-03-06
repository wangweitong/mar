package com.mar.store.mysql;

import com.mar.client.ZeusException;
import com.mar.model.GroupDescriptor;
import com.mar.model.JobDescriptorOld;
import com.mar.model.JobDescriptorOld.JobRunTypeOld;
import com.mar.model.JobStatus;
import com.mar.model.processer.Processer;
import com.mar.schedule.mvc.DebugInfoLog;
import com.mar.store.GroupBeanOld;
import com.mar.store.GroupManagerOld;
import com.mar.store.GroupManagerToolOld;
import com.mar.store.JobBeanOld;
import com.mar.store.mysql.persistence.GroupPersistence;
import com.mar.store.mysql.persistence.JobPersistenceOld;
import com.mar.store.mysql.persistence.Worker;
import com.mar.store.mysql.tool.Judge;
import com.mar.store.mysql.tool.PersistenceAndBeanConvertOld;
import com.mar.util.Tuple;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

/**
 * 性能优化，防止每次都递归去查询mysql
 * @author zhoufang
 *
 */
public class ReadOnlyGroupManagerOld extends HibernateDaoSupport{
	
	private static final Logger log = LoggerFactory.getLogger(ReadOnlyGroupManagerOld.class);
	
	private Judge jobjudge=new Judge();
	private Judge groupjudge=new Judge();
	
	private Judge ignoreContentJobJudge=new Judge();
	private Judge ignoreContentGroupJudge=new Judge();
	
	private GroupManagerOld groupManager;
	public void setGroupManager(GroupManagerOld groupManager) {
		this.groupManager = groupManager;
	}
	/**完整的globe GroupBean*/
	private GroupBeanOld globe;
	
	private GroupBeanOld ignoreGlobe;
	
	private static final ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(20);
	/**
	 * Jobs或者Groups是否有变化，忽略脚本内容的改变(保证树形结构不变即可)
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private boolean isJobsAndGroupsChangedIgnoreContent(){
		//init
		final Judge ignoreContentJobJudge=this.ignoreContentJobJudge;
		final Judge ignoreContentGroupJudge=this.ignoreContentGroupJudge;
		final GroupBeanOld ignoreGlobe=this.ignoreGlobe;
		
		boolean jobChanged;
		Judge jobrealtime=null;
		jobrealtime=(Judge) getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				Object[] o=(Object[]) session.createSQLQuery("select count(*),max(id),max(gmt_modified) from zeus_job").uniqueResult();
				if(o!=null){
					Judge j=new Judge();
					j.count=((Number) o[0]).intValue();
					j.maxId=o[1]==null?0:((Number)o[1]).intValue();
					j.lastModified=o[2]==null?new Date(0):(Date) o[2];
					j.stamp=new Date();
					return j;
				}
				return null;
			}
		});
		
		List<JobDescriptorOld> changedJobs;
		changedJobs=(List<JobDescriptorOld>) getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				Query query=session.createQuery("select id,groupId from com.com.mar.store.mysql.persistence.JobPersistenceOld where gmt_modified>?");
				query.setDate(0, ignoreContentJobJudge.lastModified);
				List<Object[]> list=query.list();
				List<JobDescriptorOld> result=new ArrayList<JobDescriptorOld>();
				for(Object[] o:list){
					JobDescriptorOld jd=new JobDescriptorOld();
					jd.setId(String.valueOf(o[0]));
					jd.setGroupId(String.valueOf(o[1]));
					result.add(jd);
				}
				return result;
			}
		});
		
		if(jobrealtime!=null && jobrealtime.count.equals(ignoreContentJobJudge.count) && jobrealtime.maxId.equals(ignoreContentJobJudge.maxId)
				&& isAllJobsNotChangeParent(ignoreGlobe, changedJobs)){
			ignoreContentJobJudge.stamp=new Date();
			ignoreContentJobJudge.lastModified=jobrealtime.lastModified;
			jobChanged= false;
		}else{
			this.ignoreContentJobJudge=jobrealtime;
			jobChanged= true;
		}
		
		
		//Group
		boolean groupChanged;
		Judge grouprealtime=null;
		grouprealtime=(Judge) getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				Object[] o=(Object[]) session.createSQLQuery("select count(*),max(id),max(gmt_modified) from zeus_group").uniqueResult();
				if(o!=null){
					Judge j=new Judge();
					j.count=((Number) o[0]).intValue();
					j.maxId=o[1]==null?0:((Number)o[1]).intValue();
					j.lastModified=o[2]==null?new Date(0):(Date) o[2];
					j.stamp=new Date();
					return j;
				}
				return null;
			}
		});
		
		
		List<GroupDescriptor> changedGroups=null;
		changedGroups=(List<GroupDescriptor>) getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				Query query=session.createQuery("from com.com.mar.store.mysql.persistence.GroupPersistence where gmt_modified>?");
				query.setDate(0, ignoreContentGroupJudge.lastModified);
				List<GroupPersistence> list=query.list();
				List<GroupDescriptor> result=new ArrayList<GroupDescriptor>();
				for(GroupPersistence p:list){
					result.add(PersistenceAndBeanConvertOld.convert(p));
				}
				return result;
			}
		});
		
		if(grouprealtime!=null && grouprealtime.count.equals(ignoreContentGroupJudge.count) && grouprealtime.maxId.equals(ignoreContentGroupJudge.maxId)
				&& isAllGroupsNotChangeThese(ignoreGlobe, changedGroups)){
			ignoreContentGroupJudge.stamp=new Date();
			groupChanged= false;
		}else{
			this.ignoreContentGroupJudge=grouprealtime;
			groupChanged= true;
		}
		
		
		return jobChanged || groupChanged;
	}
	/**
	 * 判断变动的Job中，是否全部不涉及parent节点的变化
	 * @param gb
	 * @param list
	 * @return
	 */
	private boolean isAllJobsNotChangeParent(GroupBeanOld gb,List<JobDescriptorOld> list){
		Map<String, JobBeanOld> allJobs=gb.getAllSubJobBeans();
		for(JobDescriptorOld jd:list){
			JobBeanOld bean=allJobs.get(jd.getId());
			if(bean==null){
				DebugInfoLog.info("isAllJobsNotChangeParent job id="+ jd.getId()+" has changed");
				return false;
			}
			JobDescriptorOld old=bean.getJobDescriptor();
			if(!old.getGroupId().equals(jd.getGroupId())){
				DebugInfoLog.info("isAllJobsNotChangeParent job id="+ jd.getId()+" has changed");
				return false;
			}
		}
		return true;
	}
//	/**
//	 * 判断变动的Group中，是否全部不涉及parent节点的变化
//	 * @param gb
//	 * @param list
//	 * @return
//	 */
//	private boolean isAllGroupsNotChangeParent(GroupBeanOld gb,List<GroupDescriptor> list){
//		Map<String, GroupBeanOld> allGroups=gb.getAllSubGroupBeans();
//		for(GroupDescriptor gd:list){
//			GroupBeanOld bean=allGroups.get(gd.getId());
//			if(gd.getId().equals(gb.getGroupDescriptor().getId())){
//				break;
//			}
//			if(bean==null){
//				DebugInfoLog.info("isAllGroupsNotChangeParent group id="+ gd.getId()+" has changed");
//				return false;
//			}
//			GroupDescriptor old=bean.getGroupDescriptor();
//			if(!old.getParent().equals(gd.getParent())){
//				DebugInfoLog.info("isAllGroupsNotChangeParent group id="+ gd.getId()+" has changed");
//				return false;
//			}
//		}
//		return true;
//	}
	
	private boolean isGroupsNotChangeExisted(Map<String, GroupBeanOld> allGroups,List<GroupDescriptor> list){
		for(GroupDescriptor tmp:list){
			GroupBeanOld bean=allGroups.get(tmp.getId());
			if (bean!=null && bean.isExisted()!=tmp.isExisted()) {
				return false;
			}
		}
		return true;
	}

	private boolean isAllGroupsNotChangeThese(GroupBeanOld gb,List<GroupDescriptor> list){
		Map<String, GroupBeanOld> allGroups=gb.getAllSubGroupBeans();
		for(GroupDescriptor gd:list){
			GroupBeanOld bean=allGroups.get(gd.getId());
			if(gd.getId().equals(gb.getGroupDescriptor().getId())){
				break;
			}
			if(bean==null){
				DebugInfoLog.info("isAllGroupsNotChangeParent group id="+ gd.getId()+" has changed");
				return false;
			}
			GroupDescriptor old=bean.getGroupDescriptor();
			if(!old.getParent().equals(gd.getParent())){
				DebugInfoLog.info("isAllGroupsNotChangeParent group id="+ gd.getId()+" has changed");
				return false;
			}
		}
		return isGroupsNotChangeExisted(allGroups,list);
	}
	
	/**
	 * Jobs或者Groups是否有变化
	 * 判断标准：同时满足以下条件
	 * 1.max id 一致
	 * 2.count 数一致
	 * 3.last_modified 一致
	 * @return
	 */
	private boolean isJobsAndGroupsChanged(){
		//init
		final Judge jobjudge=this.jobjudge;
		final Judge groupjudge=this.groupjudge;
		
		boolean jobChanged;
		Judge jobrealtime=(Judge) getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				Object[] o=(Object[]) session.createSQLQuery("select count(*),max(id),max(gmt_modified) from zeus_job").uniqueResult();
				if(o!=null){
					Judge j=new Judge();
					j.count=((Number) o[0]).intValue();
					j.maxId=((Number)o[1]).intValue();
					j.lastModified=(Date) o[2];
					j.stamp=new Date();
					return j;
				}
				return null;
			}
		});
		
		if(jobrealtime!=null && jobrealtime.count.equals(jobjudge.count) && jobrealtime.maxId.equals(jobjudge.maxId) && jobrealtime.lastModified.equals(jobjudge.lastModified)){
			jobjudge.stamp=new Date();
			jobChanged= false;
		}else{
			this.jobjudge=jobrealtime;
			jobChanged= true;
		}
		
		boolean groupChanged;
		Judge grouprealtime=(Judge) getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				Object[] o=(Object[]) session.createSQLQuery("select count(*),max(id),max(gmt_modified) from zeus_group").uniqueResult();
				if(o!=null){
					Judge j=new Judge();
					j.count=((Number) o[0]).intValue();
					j.maxId=((Number)o[1]).intValue();
					j.lastModified=(Date) o[2];
					j.stamp=new Date();
					return j;
				}
				return null;
			}
		});
		if(grouprealtime!=null && grouprealtime.count.equals(groupjudge.count) && grouprealtime.maxId.equals(groupjudge.maxId) && grouprealtime.lastModified.equals(groupjudge.lastModified)){
			groupjudge.stamp=new Date();
			groupChanged= false;
		}else{
			this.groupjudge=grouprealtime;
			groupChanged= true;
		}
		return jobChanged || groupChanged;
	}

	public synchronized GroupBeanOld getGlobeGroupBean() {
		if(globe!=null){
			if(!isJobsAndGroupsChanged()){
				return globe;
			}
		}
		globe=new ReadOnlyGroupManagerAssemblyOld(groupManager).getGlobeGroupBean();
		return globe;
	}

	/**
	 * 为Tree展示提供的方法，每次都返回Copy对象，可以对返回结果进行引用修改
	 * @return
	 */
	public synchronized GroupBeanOld getGlobeGroupBeanForTreeDisplay(boolean copy){
		if(ignoreGlobe==null || isJobsAndGroupsChangedIgnoreContent()  ){
			ignoreGlobe=new ReadOnlyGroupManagerAssemblyOld(groupManager).getGlobeGroupBean();
		}
		if(copy){
			return GroupManagerToolOld.buildGlobeGroupBeanWithoutDepend(new CopyGroupManagerAssembly(ignoreGlobe));
		}else{
			return ignoreGlobe;
		}
	}
	
	public GroupBeanOld getCopyGlobeGroupBean(){
		GroupBeanOld gb=getGlobeGroupBean();
		return GroupManagerToolOld.buildGlobeGroupBean(new CopyGroupManagerAssembly(gb));
	}
	
	private class CopyGroupManagerAssembly extends ReadOnlyGroupManagerAssemblyOld{
		private GroupBeanOld globe;
		public CopyGroupManagerAssembly(GroupBeanOld globe) {
			super(null);
			this.globe=globe;
		}
		@Override
		public String getRootGroupId() {
			return globe.getGroupDescriptor().getId();
		}
		@Override
		public List<GroupDescriptor> getChildrenGroup(String groupId) {
			List<GroupBeanOld> list=null;
			if(globe.getGroupDescriptor().getId().equals(groupId)){
				list=globe.getChildrenGroupBeans();
			}else{
				list=globe.getAllSubGroupBeans().get(groupId).getChildrenGroupBeans();
			}
			List<GroupDescriptor> result=new ArrayList<GroupDescriptor>();
			if(list!=null){
				for(GroupBeanOld gb:list){
					result.add(gb.getGroupDescriptor());
				}
			}
			return result;
		}
		@Override
		public GroupDescriptor getGroupDescriptor(String groupId) {
			if(globe.getGroupDescriptor().getId().equals(groupId)){
				return globe.getGroupDescriptor();
			}else{
				return globe.getAllSubGroupBeans().get(groupId).getGroupDescriptor();
			}
		}
		@Override
		public List<Tuple<JobDescriptorOld, JobStatus>> getChildrenJob(
				String groupId) {
			Map<String, JobBeanOld> map=globe.getAllSubGroupBeans().get(groupId).getJobBeans();
			List<Tuple<JobDescriptorOld, JobStatus>> result=new ArrayList<Tuple<JobDescriptorOld,JobStatus>>();
			for(JobBeanOld jb:map.values()){
				result.add(new Tuple<JobDescriptorOld, JobStatus>(jb.getJobDescriptor(), jb.getJobStatus()));
			}
			return result;
		}
	}
	
	private class ReadOnlyGroupManagerAssemblyOld implements GroupManagerOld{
		private GroupManagerOld groupManager;
		public ReadOnlyGroupManagerAssemblyOld(GroupManagerOld gm){
			this.groupManager=gm;
		}
		@Override
		public GroupDescriptor createGroup(String user, String groupName,
				String parentGroup, boolean isDirectory) throws ZeusException {
			throw new UnsupportedOperationException();
		}
		@Override
		public JobDescriptorOld createJob(String user, String jobName,
				String parentGroup, JobRunTypeOld jobType) throws ZeusException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void deleteGroup(String user, String groupId)
				throws ZeusException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void deleteJob(String user, String jobId) throws ZeusException {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<GroupDescriptor> getChildrenGroup(String groupId) {
			List<GroupDescriptor> list= groupManager.getChildrenGroup(groupId);
			List<GroupDescriptor> result=new ArrayList<GroupDescriptor>();
			for(GroupDescriptor gd:list){
				result.add(new ReadOnlyGroupDescriptor(gd));
			}
			return result;
		}

		@Override
		public List<Tuple<JobDescriptorOld, JobStatus>> getChildrenJob(
				String groupId) {
			List<Tuple<JobDescriptorOld, JobStatus>> list=groupManager.getChildrenJob(groupId);
			List<Tuple<JobDescriptorOld, JobStatus>> result=new ArrayList<Tuple<JobDescriptorOld,JobStatus>>();
			for(Tuple<JobDescriptorOld, JobStatus> tuple:list){
				Tuple<JobDescriptorOld, JobStatus> t=new Tuple<JobDescriptorOld, JobStatus>(new ReadOnlyJobDescriptor(tuple.getX()),new ReadOnlyJobStatus(tuple.getY()));
				result.add(t);
			}
			return result;
		}

		@Override
		public GroupBeanOld getDownstreamGroupBean(String groupId) {
			ReadOnlyGroupDescriptor readGd=null;
			GroupDescriptor group=getGroupDescriptor(groupId);
			if(group instanceof ReadOnlyGroupDescriptor){
				readGd=(ReadOnlyGroupDescriptor) group;
			}else{
				readGd=new ReadOnlyGroupDescriptor(group);
			}
			GroupBeanOld result=new GroupBeanOld(readGd);
			return getDownstreamGroupBean(result);
		}

		@Override
		public GroupBeanOld getDownstreamGroupBean(GroupBeanOld parent) {
			try {
				return getDownstreamGroupBean(parent, 99).get(10,TimeUnit.SECONDS);
			} catch (Exception e) {
				log.error("getDownstreamGroupBean failed", e);
				return null;
			}
		}

		private Future<GroupBeanOld> getDownstreamGroupBean(final GroupBeanOld parent, final int depth) throws Exception{
			Callable<GroupBeanOld> callable = new Callable<GroupBeanOld>(){
				
				@Override
				public GroupBeanOld call() throws Exception {
					if(parent.isDirectory()){
						List<GroupDescriptor> children=getChildrenGroup(parent.getGroupDescriptor().getId());
						ArrayList<Future<GroupBeanOld>> futures = new ArrayList<Future<GroupBeanOld>>(children.size());
						for(GroupDescriptor child:children){
							ReadOnlyGroupDescriptor readGd=null;
							if(child instanceof ReadOnlyGroupDescriptor){
								readGd=(ReadOnlyGroupDescriptor) child;
							}else{
								readGd=new ReadOnlyGroupDescriptor(child);
							}
							GroupBeanOld childBean=new GroupBeanOld(readGd);
							if(pool.getActiveCount()<15) {
								futures.add(getDownstreamGroupBean(childBean, 99));
							}else{
								getDownstreamGroupBean(childBean, 0);
							}
							childBean.setParentGroupBean(parent);
							parent.getChildrenGroupBeans().add(childBean);
						}
						for(Future<GroupBeanOld> f:futures){
							f.get(10,TimeUnit.SECONDS);
						}
					}else{
						List<Tuple<JobDescriptorOld, JobStatus>> jobs=getChildrenJob(parent.getGroupDescriptor().getId());
						for(Tuple<JobDescriptorOld, JobStatus> tuple:jobs){
							JobBeanOld jobBean=new JobBeanOld(tuple.getX(),tuple.getY());
							jobBean.setGroupBean(parent);
							parent.getJobBeans().put(tuple.getX().getId(), jobBean);
						}
					}
					return parent;
				}
			};
			if(depth>0) {
				return pool.submit(callable);
			}else{
				callable.call();
				return new Future<GroupBeanOld>() {
					@Override
					public boolean cancel(boolean mayInterruptIfRunning) {return false;}
					@Override
					public boolean isCancelled() {return false;}
					@Override
					public boolean isDone() {return false;}
					@Override
					public GroupBeanOld get() throws InterruptedException,
							ExecutionException {return null;}
					@Override
					public GroupBeanOld get(long timeout, TimeUnit unit)
							throws InterruptedException, ExecutionException,
							TimeoutException {
						return parent;
					}
				};
			}
		}

		@Override
		public GroupBeanOld getGlobeGroupBean() {
			return GroupManagerToolOld.buildGlobeGroupBean(this);
		}

		@Override
		public GroupDescriptor getGroupDescriptor(String groupId) {
			return new ReadOnlyGroupDescriptor(groupManager.getGroupDescriptor(groupId));
		}

		@Override
		public Tuple<JobDescriptorOld, JobStatus> getJobDescriptor(String jobId) {
			return groupManager.getJobDescriptor(jobId);
		}

		@Override
		public Map<String, Tuple<JobDescriptorOld, JobStatus>> getJobDescriptor(
				Collection<String> jobIds) {
			return groupManager.getJobDescriptor(jobIds);
		}

		@Override
		public JobStatus getJobStatus(String jobId) {
			return new ReadOnlyJobStatus(groupManager.getJobStatus(jobId));
		}

		@Override
		public String getRootGroupId() {
			return groupManager.getRootGroupId();
		}

		@Override
		public GroupBeanOld getUpstreamGroupBean(String groupId) {
			return GroupManagerToolOld.getUpstreamGroupBean(groupId, this);
		}

		@Override
		public JobBeanOld getUpstreamJobBean(String jobId) {
			return GroupManagerToolOld.getUpstreamJobBean(jobId, this);
		}
		@Override
		public void grantGroupOwner(String granter, String uid, String groupId)
				throws ZeusException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void grantJobOwner(String granter, String uid, String jobId)
				throws ZeusException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void moveGroup(String uid, String groupId,
				String newParentGroupId) throws ZeusException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void moveJob(String uid, String jobId, String groupId)
				throws ZeusException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void updateGroup(String user, GroupDescriptor group)
				throws ZeusException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void updateJob(String user, JobDescriptorOld job)
				throws ZeusException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void updateJobStatus(JobStatus jobStatus) {
			throw new UnsupportedOperationException();
		}
		@Override
		public List<String> getHosts() throws ZeusException {
			return Collections.emptyList();
		}
		@Override
		public void replaceWorker(Worker worker) throws ZeusException {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void removeWorker(String host) throws ZeusException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public List<JobPersistenceOld> getAllJobs() {
			return null;
		}
		@Override
		public List<String> getAllDependencied(String jobID) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public List<String> getAllDependencies(String jobID) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public void updateActionList(JobDescriptorOld job) {
			// TODO Auto-generated method stub
			
		}
	}
	
	/**
	 * 不可变的GroupDescriptor类
	 * @author zhoufang
	 *
	 */
	public class ReadOnlyGroupDescriptor extends GroupDescriptor{
		private static final long serialVersionUID = 1L;
		private GroupDescriptor gd;
		public ReadOnlyGroupDescriptor(GroupDescriptor gd){
			this.gd=gd;
		}
		@Override
		public String getDesc() {
			return gd.getDesc();
		}

		@Override
		public String getId() {
			return gd.getId();
		}

		@Override
		public String getName() {
			return gd.getName();
		}

		@Override
		public String getOwner() {
			return gd.getOwner();
		}
		
		@Override
		public boolean isExisted(){
			return gd.isExisted();
		}

		@Override
		public boolean isDirectory() {
			return gd.isDirectory();
		}

		@Override
		public void setDesc(String desc) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setName(String name) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setOwner(String owner) {
			throw new UnsupportedOperationException();
		}


		@Override
		public String getParent() {
			return gd.getParent();
		}

		@Override
		public void setParent(String parent) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<Map<String, String>> getResources() {
			List<Map<String, String>> list=gd.getResources();
			List<Map<String, String>> result=new ArrayList<Map<String,String>>();
			for(Map<String, String> map:list){
				result.add(new HashMap<String, String>(map));
			}
			return result;
		}

		@Override
		public void setResources(List<Map<String, String>> resources) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setId(String id) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setDirectory(boolean directory) {
			throw new UnsupportedOperationException();
		}


		@Override
		public Map<String, String> getProperties() {
			return new HashMap<String, String>(gd.getProperties());
		}

		@Override
		public void setProperties(Map<String, String> properties) {
			throw new UnsupportedOperationException();
		}
		
	}
	/**
	 * 不可变JobDescriptor类
	 * @author zhoufang
	 *
	 */
	public class ReadOnlyJobDescriptor extends JobDescriptorOld{
		private static final long serialVersionUID = 1L;
		private JobDescriptorOld jd;
		public ReadOnlyJobDescriptor(JobDescriptorOld jd){
			this.jd=jd;
		}
		@Override
		public List<Map<String, String>> getResources() {
			List<Map<String, String>> list=jd.getResources();
			List<Map<String, String>> result=new ArrayList<Map<String,String>>();
			for(Map<String, String> map:list){
				result.add(new HashMap<String, String>(map));
			}
			return result;
		}
		@Override
		public String getCronExpression() {
			return jd.getCronExpression();
		}

		@Override
		public List<String> getDependencies() {
			return new ArrayList<String>(jd.getDependencies());
		}

		@Override
		public String getDesc() {
			return jd.getDesc();
		}

		@Override
		public String getGroupId() {
			return jd.getGroupId();
		}

		@Override
		public String getId() {
			return jd.getId();
		}

		@Override
		public JobRunTypeOld getJobType() {
			return jd.getJobType();
		}

		@Override
		public String getName() {
			return jd.getName();
		}

		@Override
		public String getOwner() {
			return jd.getOwner();
		}


		@Override
		public JobScheduleTypeOld getScheduleType() {
			return jd.getScheduleType();
		}

		@Override
		public boolean hasDependencies() {
			return !jd.getDependencies().isEmpty();
		}

		@Override
		public void setCronExpression(String cronExpression) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setDependencies(List<String> depends) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setDesc(String desc) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setJobType(JobRunTypeOld type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setName(String name) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setOwner(String owner) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setScheduleType(JobScheduleTypeOld type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setGroupId(String groupId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setId(String id) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setResources(List<Map<String, String>> resources) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Map<String, String> getProperties() {
			return new HashMap<String, String>(jd.getProperties());
		}

		@Override
		public void setProperties(Map<String, String> properties) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Boolean getAuto() {
			return jd.getAuto();
		}

		@Override
		public void setAuto(Boolean auto) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getScript() {
			return jd.getScript();
		}

		@Override
		public void setScript(String script) {
			throw new UnsupportedOperationException();
		}
		@Override
		public List<Processer> getPreProcessers() {
			return new ArrayList<Processer>(jd.getPreProcessers());
		}

		@Override
		public void setPreProcessers(List<Processer> preProcessers) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<Processer> getPostProcessers() {
			return new ArrayList<Processer>(jd.getPostProcessers());
		}

		@Override
		public void setPostProcessers(List<Processer> postProcessers) {
			throw new UnsupportedOperationException();
		}
	}
	/**
	 * 不可变JobStatus类
	 * @author zhoufang
	 *
	 */
	public class ReadOnlyJobStatus extends JobStatus{
		private static final long serialVersionUID = 1L;
		private JobStatus jobStatus;
		public ReadOnlyJobStatus(JobStatus js){
			jobStatus=js;
		}
		@Override
		public String getJobId(){
			return jobStatus.getJobId();
		}
		@Override
		public void setJobId(String jobId){
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Status getStatus(){
			return jobStatus.getStatus();
		}
		@Override
		public void setStatus(Status status){
			throw new UnsupportedOperationException();
		}
		@Override
		public Map<String, String> getReadyDependency() {
			return new HashMap<String, String>(jobStatus.getReadyDependency());
		}
		@Override
		public void setReadyDependency(Map<String, String> readyDependency){
			throw new UnsupportedOperationException();
		}
		@Override
		public String getHistoryId() {
			return jobStatus.getHistoryId();
		}
		@Override
		public void setHistoryId(String historyId) {
			throw new UnsupportedOperationException();
		}
	}

}
