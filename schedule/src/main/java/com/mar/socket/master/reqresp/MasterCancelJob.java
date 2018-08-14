package com.mar.socket.master.reqresp;

import com.mar.socket.master.AtomicIncrease;
import com.mar.socket.master.MasterContext;
import com.mar.socket.master.MasterHandler.ResponseListener;
import com.mar.socket.protocol.Protocol.*;
import com.mar.socket.protocol.Protocol.SocketMessage.Kind;
import org.jboss.netty.channel.Channel;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public class MasterCancelJob {
	
	public Future<Response> cancel(final MasterContext context,Channel channel,ExecuteKind ek,String id){
		// 如果在运行中 从worker列表中查询正在运行该job的woker，发出取消命令
		// 如果在等待队列，从等待队列删除
		// 如果都不在，抛出异常
		CancelMessage cm=CancelMessage.newBuilder().setEk(ek).setId(id).build();
		final Request req=Request.newBuilder().setRid(AtomicIncrease.getAndIncrement()).setOperate(Operate.Cancel)
			.setBody(cm.toByteString()).build();
		SocketMessage sm=SocketMessage.newBuilder().setKind(Kind.REQUEST).setBody(req.toByteString()).build();
		Future<Response> f=context.getThreadPool().submit((new Callable<Response>() {
			private Response response;
			public Response call() throws Exception {
				final CountDownLatch latch=new CountDownLatch(1);
				context.getHandler().addListener(new ResponseListener() {
					public void onWebResponse(WebResponse resp) {}
					public void onResponse(Response resp) {
						if(req.getRid()==resp.getRid()){
							context.getHandler().removeListener(this);
							response=resp;
							latch.countDown();
						}
					}
				});
				latch.await();
				return response;
			}
		}));
		channel.write(sm);
		return f;
	}
}
