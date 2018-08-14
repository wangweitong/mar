package com.mar.store;

import com.mar.model.HostGroupCache;
import com.mar.store.mysql.persistence.HostGroupPersistence;

import java.util.List;
import java.util.Map;

public interface HostGroupManager{
	public HostGroupPersistence getHostGroupName(String hostGroupId);
	
	public Map<String,HostGroupCache> getAllHostGroupInfomations();
	
	public List<HostGroupPersistence> getAllHostGroup();
	
	public List<String> getPreemptionHost();
}
