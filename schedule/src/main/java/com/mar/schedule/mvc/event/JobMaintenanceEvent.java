package com.mar.schedule.mvc.event;

import com.mar.mvc.AppEvent;
import com.mar.mvc.EventType;

public class JobMaintenanceEvent extends AppEvent {
	
	private final String id;
	public JobMaintenanceEvent(EventType type,String id){
		super(type);
		this.id=id;
	}
	public String getId() {
		return id;
	}

}
