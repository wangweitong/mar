package com.mar.socket.worker.reqresp;

import com.mar.jobs.JobContext;
import com.mar.jobs.sub.tool.CpuLoadPerCoreJob;
import com.mar.jobs.sub.tool.MemUseRateJob;
import com.mar.schedule.mvc.ScheduleInfoLog;
import com.mar.socket.master.AtomicIncrease;
import com.mar.socket.protocol.Protocol.HeartBeatMessage;
import com.mar.socket.protocol.Protocol.Operate;
import com.mar.socket.protocol.Protocol.Request;
import com.mar.socket.protocol.Protocol.SocketMessage;
import com.mar.socket.protocol.Protocol.SocketMessage.Kind;
import com.mar.socket.worker.WorkerContext;
import com.mar.util.Environment;
import org.jboss.netty.channel.ChannelFuture;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.UUID;

public class WorkerHeartBeat {
	public static String host = UUID.randomUUID().toString();
	static {
		try {
			host = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// ignore
		}
	}

	public ChannelFuture execute(WorkerContext context) {
		JobContext jobContext = JobContext.getTempJobContext(JobContext.SYSTEM_RUN);
		MemUseRateJob job = new MemUseRateJob(jobContext, 1);
		CpuLoadPerCoreJob loadJob = new CpuLoadPerCoreJob(context, jobContext);
		runJob(jobContext, job);
		runJob(jobContext, loadJob);
		HeartBeatMessage hbm = HeartBeatMessage.newBuilder()
				.setCpuLoadPerCore((Float)jobContext.getData("cpuLoadPerCore"))
				.setMemRate(((Double) jobContext.getData("mem")).floatValue())
				.addAllDebugRunnings(context.getDebugRunnings().keySet())
				.addAllManualRunnings(context.getManualRunnings().keySet())
				.addAllRunnings(context.getRunnings().keySet())
				.setTimestamp(new Date().getTime()).setHost(host).build();
		Request req = Request.newBuilder()
				.setRid(AtomicIncrease.getAndIncrement())
				.setOperate(Operate.HeartBeat).setBody(hbm.toByteString())
				.build();

		SocketMessage sm = SocketMessage.newBuilder().setKind(Kind.REQUEST)
				.setBody(req.toByteString()).build();
		return context.getServerChannel().write(sm);
	}

	private void runJob(JobContext jobContext, MemUseRateJob job) {
		try {
			int exitCode = -1;
			int count = 0;
			while (count < 3 && exitCode != 0) {
				count++;
				exitCode = job.run();
			}
			if (exitCode != 0) {
				ScheduleInfoLog.error("HeartBeat Shell Error", new Exception(
						jobContext.getJobHistory().getLog().getContent()));
				// 防止后面NPE
				jobContext.putData("mem", 1.0);
			}
		} catch (Exception e) {
			ScheduleInfoLog.error("memratejob", e);
		}
	}
	
	private void runJob(JobContext jobContext, CpuLoadPerCoreJob job) {
		try {
			int exitCode = -1;
			int count = 0;
			while (count < 3 && exitCode != 0) {
				count++;
				exitCode = job.run();
			}
			if (exitCode != 0) {
				ScheduleInfoLog.error("HeartBeat Shell Error", new Exception(" error occurs during get cpu load "));
				// 防止后面NPE
				jobContext.putData("cpuLoadPerCore",Environment.getMaxCpuLoadPerCore());
			}
		} catch (Exception e) {
			ScheduleInfoLog.error("cpuLoadPerCore", e);
		}
	}
}
