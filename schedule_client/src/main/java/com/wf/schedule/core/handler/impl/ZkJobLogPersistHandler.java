/**
 * 
 */
package com.wf.schedule.core.handler.impl;


import com.wf.schedule.common.context.ZkConstant;
import com.wf.schedule.common.util.GfJsonUtil;
import com.wf.schedule.core.handler.JobLogPersistHandler;
import com.wf.schedule.log.LogExceptionStackTrace;
import com.wf.schedule.model.JobConfig;
import com.wf.schedule.model.JobLogInfo;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class ZkJobLogPersistHandler implements JobLogPersistHandler {

	protected static final Logger logger = LoggerFactory.getLogger(ZkJobLogPersistHandler.class);

	private ZkClient zkClient;

	public ZkJobLogPersistHandler(ZkClient zkClient) {
		this.zkClient = zkClient;
	}

	@Override
	public void onSucess(JobConfig conf, Date nextFireTime) {
		JobLogInfo jobLogInfo  = new JobLogInfo();
		jobLogInfo.setJobConfig(conf);
		jobLogInfo.setNextFireTime(nextFireTime);
		jobLogInfo.setSuccess(conf.isResult());
		save(conf, jobLogInfo);

	}

	@Override
	public void onError(JobConfig conf, Date nextFireTime, Exception e) {
		JobLogInfo jobLogInfo  = new JobLogInfo();
		jobLogInfo.setJobConfig(conf);
		jobLogInfo.setNextFireTime(nextFireTime);
		jobLogInfo.setException(e);
		jobLogInfo.setSuccess(conf.isResult());
		save(conf, jobLogInfo);
	}

	@Override
	public void persist(JobConfig conf) {
		JobLogInfo jobLogInfo  = new JobLogInfo();
		jobLogInfo.setJobConfig(conf);
		jobLogInfo.setSuccess(conf.isResult());
		save(conf, jobLogInfo);
	}

	private void save (JobConfig conf, JobLogInfo jobLogInfo) {

		try {
			if (!zkClient.exists(ZkConstant.ROOT_LOG)) {
				zkClient.createPersistent(ZkConstant.ROOT_LOG, false);
			}

			String groupNode = String.format("%s/%s", ZkConstant.ROOT_LOG, conf.getGroupName());
			if (!zkClient.exists(groupNode)) {
				zkClient.createPersistent(groupNode, false);
			}

			String taskNode = String.format("%s/%s/%s", ZkConstant.ROOT_LOG, conf.getGroupName(), conf.getJobName());
			if (!zkClient.exists(taskNode)) {
				zkClient.createPersistent(taskNode, false);
			}
			zkClient.writeData(taskNode, GfJsonUtil.toJSONString(jobLogInfo));

//		} catch (ZkTimeoutException ex) {
//			logger.error("job log save error:{}", LogExceptionStackTrace.erroStackTrace(ex));
//			SchedulerFactoryBean schedulerFactoryBean = InstanceFactory.getInstance(SchedulerFactoryBean.class);
//			try {
//				schedulerFactoryBean.destroy();
//			} catch (SchedulerException e) {
//				e.printStackTrace();
//			}
//			zkClient.close();

		} catch (Exception e) {
			logger.error("job log save error:{}", LogExceptionStackTrace.erroStackTrace(e));
		}
	}

	public void setZkClient(ZkClient zkClient) {
		this.zkClient = zkClient;
	}
}
