/**
 * 
 */
package com.mar.jobs.sub.tool;

import com.mar.jobs.AbstractJob;
import com.mar.jobs.JobContext;
import com.mar.model.processer.HiveProcesser;
import org.springframework.context.ApplicationContext;

/**
 * @author gufei.wzy
 * @date 2013-1-9
 */
public class HiveProcesserJob extends AbstractJob {

	private HiveProcesser processer;
	private int exitCode = 0;
	private ApplicationContext applicationContext;

	public HiveProcesserJob(final JobContext jobContext,
			final HiveProcesser processer,
			final ApplicationContext applicationContext) {
		super(jobContext);
		this.processer = processer;
		this.applicationContext = applicationContext;
	}

	@Override
	public Integer run() {

		/************************
		 *  前置
		 ************************/
		if (jobContext.getCoreExitCode() == null) {

		} else {
		/*****************************
		 *  后置
		 *****************************/
			if (processer.getOutputTables() != null
					&& processer.getOutputTables().size() > 0) {
				if (processer.getDriftPercent() != null) {
					try {
						log("hive分区产出检查开始");
						exitCode = new HiveOutputCheckJob(jobContext,
								processer, applicationContext).run();
					} catch (Exception e) {
						log("hive分区产出检查失败");
						log(e);
					} finally {
						log("hive分区产出检查结束");
					}
				}
				if (processer.getKeepDays() != null) {
					try {
						log("历史分区清理开始");
						exitCode = new HivePartitionCleanJob(jobContext,
								processer, applicationContext).run();
					} catch (Exception e) {
						log("历史分区清理失败");
						log(e);
					} finally {
						log("历史分区清理结束");
					}
				}
			}

		}

		return exitCode;
	}

	@Override
	public void cancel() {
		canceled = true;
	}

}
