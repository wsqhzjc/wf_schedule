/**
 * 
 */
package com.wf.schedule.distributed.test.handler;


import com.wf.schedule.core.handler.ConfigPersistHandler;
import com.wf.schedule.model.JobConfig;

public class DbConfigPersistHandler implements ConfigPersistHandler {


	@Override
	public void merge(JobConfig config) {
		// load config from db
		System.out.println("========>>假装从数据库load一下配置");
	}

	@Override
	public void persist(JobConfig config) {
		// save config to db
		System.out.println("========>>假装保存配置到数据库");
	}

}
