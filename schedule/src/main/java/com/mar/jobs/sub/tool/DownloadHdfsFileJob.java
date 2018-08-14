package com.mar.jobs.sub.tool;

import com.mar.jobs.JobContext;
import com.mar.jobs.ProcessJob;
import com.mar.util.Environment;
import com.mar.util.JobUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 从HDFS文件系统下载到本地文件系统
 * 
 * @author zhoufang
 * 
 */
public class DownloadHdfsFileJob extends ProcessJob {

	private String hdfsFilePath;
	private String localPath;

	public DownloadHdfsFileJob(final JobContext jobContext, final String localPath,
			final String hdfsFilePath) {
		super(jobContext);
		this.localPath = localPath;
		this.hdfsFilePath = hdfsFilePath;

	}
	
	@Override
	public List<String> getCommandList() {
		String hadoopCmd=JobUtils.getHadoopCmd(envMap);
		List<String> commands = new ArrayList<String>();
		commands.add(hadoopCmd+" fs -copyToLocal " + hdfsFilePath + " " + localPath);
		//格式转换
		String[] excludeFiles = Environment.getExcludeFile().split(";");
		boolean isDos2unix = true;
		if(excludeFiles!=null && excludeFiles.length>0){
			for(String excludeFile : excludeFiles){
				if(localPath.toLowerCase().endsWith("."+excludeFile.toLowerCase())){
					isDos2unix = false;
					break;
				}
			}
//			System.out.println(Environment.getExcludeFile());
		}
		if(isDos2unix){
			commands.add("dos2unix " + localPath);
//			System.out.println("dos2unix file: " + localPath);
			log("dos2unix file: " + localPath);
		}
		//commands.add("dos2unix " + localPath);
		return commands;
	}

}
