/**
 * 
 */
package com.wf.schedule.core.handler;


import com.wf.schedule.model.JobConfig;

import java.util.Date;

public interface JobLogPersistHandler {

	public void onSucess(JobConfig conf, Date nextFireTime);
	
	public void onError(JobConfig conf, Date nextFireTime, Exception e);

	void persist(JobConfig conf);
}
