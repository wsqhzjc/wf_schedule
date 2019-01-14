/**
 * 
 */
package com.wf.schedule.core.handler;


import com.wf.schedule.model.JobConfig;

public interface ConfigPersistHandler {
	
	/**
	 * 启动时合并配置
	 * @return
	 */
	void merge(JobConfig config);
	
	/**
	 * 持久化配置
	 * @param config
	 */
	void persist(JobConfig config);
}
