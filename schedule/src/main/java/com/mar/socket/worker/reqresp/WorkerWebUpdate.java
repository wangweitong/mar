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

public class WorkerWebUpdate {

	public Future<WebResponse> execute(final WorkerContext context,String jobId){
		final WebRequest req=WebRequest.newBuilder().setRid(AtomicIncrease.getAndIncrement()).setOperate(WebOperate.UpdateJob)
			.setEk(ExecuteKind.ManualKind)//此次无用，随便设置一个
			.setId(jobId).build();
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
		SocketLog.info("send web update to master,rid="+req.getRid()+",jobId="+jobId);
		return f;
	}
}
