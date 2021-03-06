package com.mar.socket.master.reqresp;

import com.mar.model.DebugHistory;
import com.mar.socket.SocketLog;
import com.mar.socket.master.JobElement;
import com.mar.socket.master.MasterContext;
import com.mar.socket.protocol.Protocol.Status;
import com.mar.socket.protocol.Protocol.WebOperate;
import com.mar.socket.protocol.Protocol.WebRequest;
import com.mar.socket.protocol.Protocol.WebResponse;

import java.util.ArrayList;

public class MasterBeWebDebug {
	public WebResponse beWebExecute(MasterContext context,WebRequest req) {
		// 判断job是否已经在运行中，或者在队列中
		// 如果在，抛出异常，已经在执行中
		// 如果不在，将该job放入等待队列
		SocketLog.info("receive web debug request,rid="+req.getRid()+",debugId="+req.getId());
		String debugId=req.getId();
		for(JobElement e:new ArrayList<JobElement>(context.getDebugQueue())){
			if(e.getJobID().equals(debugId)){
				WebResponse resp=WebResponse.newBuilder().setRid(req.getRid()).setOperate(WebOperate.ExecuteDebug)
					.setStatus(Status.ERROR).setErrorText("已经在队列中，无法再次运行").build();
				return resp;
			}
		}
		
		DebugHistory debug=context.getDebugHistoryManager().findDebugHistory(debugId);
		context.getMaster().debug(debug);
		
		WebResponse resp=WebResponse.newBuilder().setRid(req.getRid()).setOperate(WebOperate.ExecuteDebug)
			.setStatus(Status.OK).build();
		SocketLog.info("send web debug response,rid="+req.getRid()+",debugId="+debugId);
		return resp;
	}
}
