package com.mar.store.mysql.tool;

import com.mar.client.ZeusException;
import com.mar.model.GroupDescriptor;

public class GroupValidate {
	public static boolean valide(GroupDescriptor group) throws ZeusException{
		if(group.getName()==null || group.getName().trim().equals("")){
			throw new ZeusException("name字段不能为空");
		}
		
		return true;
	}
}
