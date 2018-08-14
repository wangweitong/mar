package com.mar.jobs.sub.tool;

import com.mar.jobs.JobContext;
import com.mar.schedule.mvc.ScheduleInfoLog;
import com.mar.socket.worker.WorkerContext;
import com.mar.util.Environment;
import com.mar.util.RunShell;

import java.util.Date;

public class CpuLoadPerCoreJob {
    private WorkerContext workerContext;
    private JobContext jobContext;
    private static Date date = new Date();
    private static final String loadStr = "load average:";
    private static int len = loadStr.length();

    public CpuLoadPerCoreJob(WorkerContext workerContext, JobContext jobContext) {
        this.workerContext = workerContext;
        this.jobContext = jobContext;
    }

    public Integer run() throws Exception {
        //window mac 系统直接返回成功
        String os = System.getProperties().getProperty("os.name");
        if (os != null && (os.startsWith("win") || os.startsWith("Win") || os.startsWith("Mac"))) {
            //放一个假的数字，方便开发
            jobContext.putData("cpuLoadPerCore", Environment.getMaxCpuLoadPerCore());
            return 0;
        }
        RunShell shell = new RunShell("uptime");
        int exitCode = shell.run();
        if (exitCode == 0) {
            String result = shell.getResult();
            String cpuloadstr = getCpuload(result);
            Float cpuload = Float.valueOf(cpuloadstr);//最近1分钟系统的平均cpu负载
            Integer coreNum = workerContext.getCpuCoreNum();
            if (coreNum == null) {
                return -1;
            }
            Float cpuloadpercore = cpuload / workerContext.getCpuCoreNum();
            if ((new Date().getTime() - date.getTime()) > 3 * 60 * 1000) {
                ScheduleInfoLog.info("cpu load:" + cpuloadstr + " core Number:" + coreNum + " rate:" + cpuloadpercore.toString());
                date = new Date();
            }
            jobContext.putData("cpuLoadPerCore", cpuloadpercore);
            return 0;
        }
        return -1;
    }

    private String getCpuload(String result) {
        String sub = result.substring(result.indexOf(loadStr)).substring(len).trim();
        String[] tmps = sub.split(",");
        return tmps[0].trim();
    }
}
