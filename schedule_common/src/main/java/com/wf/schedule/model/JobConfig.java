package com.wf.schedule.model;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author chenpengfei
 */
public class JobConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	private String groupName;

	private String jobName; //class name

	private String schedulerName;

	private boolean running = false;// 是否运行中

	private boolean active = true;// 是否启用

	private String cronExpr; //

	private String jobMethod;

	private String extraData;

	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date lastFireTime;// 上一次运行开始时间

	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date lastEndTime; // 上次运行结束时间

	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date nextFireTime;// 下一次运行开始时间

	// 当前执行节点id
	private String currentNodeId;

	private long modifyTime;
	
	private String errorMsg;

	private boolean result;

	public JobConfig() {
	}

	/**
	 * @param cronExpr
	 */
	public JobConfig(String groupName, String jobName, String jobMethod, String cronExpr, String schedulerName, String extraData) {
		super();
		this.groupName = groupName;
		this.jobName = jobName;
		this.cronExpr = cronExpr;
		this.modifyTime = System.currentTimeMillis();
		this.jobMethod = jobMethod;
		this.schedulerName = schedulerName;
		this.extraData = extraData;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getJobName() {
		return jobName;
	}

	public void seJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getSchedulerName() {
		return schedulerName;
	}

	public void setSchedulerName(String schedulerName) {
		this.schedulerName = schedulerName;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getCronExpr() {
		return cronExpr;
	}

	public void setCronExpr(String cronExpr) {
		this.cronExpr = cronExpr;
	}

	public Date getLastFireTime() {
		return lastFireTime;
	}

	public void setLastFireTime(Date lastFireTime) {
		this.lastFireTime = lastFireTime;
	}

	public Date getLastEndTime() {
		return lastEndTime;
	}

	public void setLastEndTime(Date lastEndTime) {
		this.lastEndTime = lastEndTime;
	}

	public Date getNextFireTime() {
		return nextFireTime;
	}

	public void setNextFireTime(Date nextFireTime) {
		this.nextFireTime = nextFireTime;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public String getCurrentNodeId() {
		return currentNodeId;
	}

	public void setCurrentNodeId(String currentNodeId) {
		this.currentNodeId = currentNodeId;
	}

	public long getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(long modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getExtraData() {
		return extraData;
	}

	public void setExtraData(String extraData) {
		this.extraData = extraData;
	}

	private static long allowDeviation = 1000 * 60 * 10;
	public String getErrorMsg() {
//		if(errorMsg == null){
//			if(lastFireTime != null && nextFireTime != null){
//				long interval = nextFireTime.getTime() - lastFireTime.getTime();
//				long nextFireTimeMils;
//				if(running){
//					nextFireTimeMils = lastFireTime.getTime() + interval;
//				}else{
//					nextFireTimeMils = nextFireTime.getTime();
//				}
//				if(Calendar.getInstance().getTimeInMillis() - nextFireTimeMils > (interval < allowDeviation ? interval : allowDeviation)){
//					errorMsg = "nextFireTime Abnormal";
//				}else{
//					errorMsg = "";
//				}
//			}
//		}
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	public boolean error(){
		return errorMsg != null;
	}

	public String getJobMethod() {
		return jobMethod;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public void setJobMethod(String jobMethod) {
		this.jobMethod = jobMethod;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}
}
