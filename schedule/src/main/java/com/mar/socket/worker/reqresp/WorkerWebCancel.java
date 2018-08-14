package com.mar.socket.worker.reqresp;

import com.mar.socket.SocketLog;
import com.mar.socket.master.AtomicIncrease;
import com.mar.socket.protocol.Protocol.*;
import com.mar.socket.protocol.Protocol.SocketMessage.Kind;
import com.mar.socket.worker.WorkerContext;
import com.mar.socket.worker.WorkerHandler.ResponseListener;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public class WorkerWebCancel {

	
	public Future<WebResponse> cancel(final WorkerContext context,ExecuteKind kind,String id,String operater){
		final WebRequest req=WebRequest.newBuilder().setRid(AtomicIncrease.getAndIncrement()).setOperate(WebOperate.CancelJob)
			.setExecutor(operater).setEk(kind).setId(id).build();
		
		SocketMessage sm=SocketMessage.newBuilder().setKind(Kind.WEB_REUQEST).setBody(req.toByteString()).build();
		Future<WebResponse> f=context.getThreadPool().submit(new Callable<WebResponse>() {
			private WebResponse response;
			public WebResponse call() throws Exception {
				final CountDownLatch latch=new CountDownLatch(1);
				context.getHandler().addListener(new ResponseListener() {
					public void onWebResponse(WebResponse resp) {
						if(resp.getRid()==req.getRid()){
							context.getHandler().removeListener(this);
							response=resp;
							latch.countDown();
						}
					}
					public void onResponse(Response resp) {}
				});
				latch.await();
				return response;
			}
		});
		context.getServerChannel().write(sm);
		SocketLog.info("send web cancel request,rid="+req.getRid()+",id="+id);
		return f;
	}
}
