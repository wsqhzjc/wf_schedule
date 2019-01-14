package com.wf.schedule.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by chenpengfei on 2017/9/19.
 */
public class JobLogInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private JobConfig jobConfig;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date nextFireTime;

    private Exception exception;

    private boolean success;

    public JobConfig getJobConfig() {
        return jobConfig;
    }

    public void setJobConfig(JobConfig jobConfig) {
        this.jobConfig = jobConfig;
    }

    public Date getNextFireTime() {
        return nextFireTime;
    }

    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
