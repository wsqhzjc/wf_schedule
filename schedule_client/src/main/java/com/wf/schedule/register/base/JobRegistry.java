/**
 * 
 */
package com.wf.schedule.register.base;


import com.wf.schedule.model.JobConfig;

import java.util.Date;
import java.util.List;

public interface JobRegistry {

	void register(JobConfig conf);
	
	void updateJobConfig(JobConfig conf);
	
	void setRuning(String jobName, Date fireTime);

	void setStoping(String jobName, Date nextFireTime, Exception e);

	JobConfig getConf(String jobName, boolean forceRemote);
	
	void unregister(String jobName);
	
	List<JobConfig> getAllJobs();
	
	void onRegistered();

	/**
	 * 注册group订阅
	 * @param groupName
	 */
	void registerGroup(String groupName);

	void createGroup(String groupName);

	/**
	 * zk的状态订阅
	 */
	void subcribeState();
}
