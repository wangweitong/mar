package com.mar.socket.master.reqresp;

import com.mar.model.DebugHistory;
import com.mar.model.JobHistory;
import com.mar.socket.SocketLog;
import com.mar.socket.master.MasterContext;
import com.mar.socket.protocol.Protocol.*;

public class MasterBeWebExecute {
	public WebResponse beWebExecute(MasterContext context,WebRequest req) {
		if(req.getEk()==ExecuteKind.ManualKind || req.getEk()==ExecuteKind.ScheduleKind){
			String historyId=req.getId();
			JobHistory history=context.getJobHistoryManager().findJobHistory(historyId);
			String jobId=history.getJobId();
			context.getMaster().run(history);
			
			WebResponse resp=WebResponse.newBuilder().setRid(req.getRid()).setOperate(WebOperate.ExecuteJob)
				.setStatus(Status.OK).build();
			SocketLog.info("send web execute response,rid="+req.getRid()+",jobId="+jobId);
			return resp;
		}else if(req.getEk()==ExecuteKind.DebugKind){
			String debugId=req.getId();
			DebugHistory history=context.getDebugHistoryManager().findDebugHistory(debugId);
			SocketLog.info("receive web debug request,rid="+req.getRid()+",debugId="+debugId);
			
			context.getMaster().debug(history);
			
			WebResponse resp=WebResponse.newBuilder().setRid(req.getRid()).setOperate(WebOperate.ExecuteJob)
				.setStatus(Status.OK).build();
			SocketLog.info("send web debug response,rid="+req.getRid()+",debugId="+debugId);
			return resp;
		}
		return null;
	}
}
