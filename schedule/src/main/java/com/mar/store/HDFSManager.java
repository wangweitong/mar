package com.mar.store;

import com.mar.jobs.sub.conf.ConfUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 云梯文件操作
 * 
 * @author gufei.wzy 2012-10-17
 */
public class HDFSManager {
	private static FileSystem fs;
	private final static Log log = LogFactory.getLog(HDFSManager.class);

	static {
		try {
			fs = FileSystem.get(ConfUtil.getDefaultCoreAndHdfsSite());
		} catch (IOException e) {
			log.error("Open HDFS FileSystem失败！", e);
		}
	}

	public static Map<String, Long> getPathSize(List<String> pathList)
			throws IOException {
		HashMap<String, Long> res = new HashMap<String, Long>(pathList.size());
		for (String path : pathList) {
			res.put(path, getPathSize(path));
		}
		return res;
	}

	public static Long getPathSize(String path) {
		try {
			return fs.getContentSummary(new Path(path)).getLength();
		} catch (IOException e) {
			log.warn("查询路径大小失败！", e);
		}
		return -1L;
	}

	public static Long getPathSize(String path, String ugi) {
		if (ugi == null) {
			return getPathSize(path);
		}
		try {
			Configuration conf = ConfUtil.getDefaultCoreAndHdfsSite();
			conf.set("hadoop.job.ugi", ugi);
			FileSystem fs = FileSystem.get(conf);
		
			return fs.getContentSummary(new Path(path)).getLength();
		} catch (Exception e) {
			log.warn("find path size error!", e);
		}
		return -1L;
	}

	public static Long getPathSize(String path, Configuration conf) {
		if (conf == null) {
			return getPathSize(path);
		}
		try {
			FileSystem fs = FileSystem.get(conf);
			return fs.getContentSummary(new Path(path)).getLength();
		} catch (IOException e) {
			log.warn("查询路径大小失败！", e);
		}
		return -1L;
	}
}
