package com.mar.socket.master.reqresp;

import com.mar.schedule.mvc.event.Events;
import com.mar.schedule.mvc.event.JobMaintenanceEvent;
import com.mar.socket.SocketLog;
import com.mar.socket.master.MasterContext;
import com.mar.socket.protocol.Protocol.Status;
import com.mar.socket.protocol.Protocol.WebOperate;
import com.mar.socket.protocol.Protocol.WebRequest;
import com.mar.socket.protocol.Protocol.WebResponse;

public class MasterBeUpdate {
	public WebResponse beWebUpdate(MasterContext context,WebRequest req) {
		
		context.getDispatcher().forwardEvent(new JobMaintenanceEvent(Events.UpdateActions,req.getId()));
		WebResponse resp=WebResponse.newBuilder().setRid(req.getRid()).setOperate(WebOperate.UpdateJob)
			.setStatus(Status.OK).build();
		SocketLog.info("send web update response,rid="+req.getRid()+",jobId="+req.getId());
		return resp;
	}
}
