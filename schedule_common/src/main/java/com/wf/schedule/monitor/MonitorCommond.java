/**
 * 
 */
package com.wf.schedule.monitor;

import java.io.Serializable;

/**
 *
 * @author chenpengfei
 */
public class MonitorCommond implements Serializable {

	private static final long serialVersionUID = 1L;


	public final static byte TYPE_EXEC = 0x01; 	//执行定时任务
	public final static byte TYPE_STATUS_MOD = 0x02;//开停状态修改 
	public final static byte TYPE_CRON_MOD = 0x03;//执行时间策略修改
	public final static byte TYPE_DELETE_MOD = 0x04;//任务删除
	public final static byte TYPE_PARAM_MOD = 0x05;//修改参数
	
	
	private byte cmdType;
	private String jobGroup; 
	private String jobName; 
	private Object body;
	
	public MonitorCommond() {}

	public MonitorCommond(byte cmdType, String jobGroup, String jobName, Object body) {
		super();
		this.cmdType = cmdType;
		this.jobGroup = jobGroup;
		this.jobName = jobName;
		this.body = body;
	}
	public byte getCmdType() {
		return cmdType;
	}
	public void setCmdType(byte cmdType) {
		this.cmdType = cmdType;
	}
	public String getJobGroup() {
		return jobGroup;
	}
	public void setJobGroup(String jobGroup) {
		this.jobGroup = jobGroup;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "MonitorCommond [cmdType=" + cmdType + ", jobGroup=" + jobGroup + ", jobName=" + jobName + "]";
	}
	
	
}
