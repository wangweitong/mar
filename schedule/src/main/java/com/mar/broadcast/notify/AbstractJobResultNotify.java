package com.mar.broadcast.notify;

import com.mar.store.GroupManager;
import com.mar.store.JobHistoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class AbstractJobResultNotify implements JobResultNotify{
	@Autowired
	protected JobHistoryManager jobHistoryManager;
	@Autowired
	@Qualifier("groupManager")
	protected GroupManager groupManager;

}