/**
 * 
 */
package com.wf.schedule.core;

import com.wf.schedule.common.spring.InstanceFactory;
import com.wf.schedule.core.context.JobContext;
import com.wf.schedule.model.JobConfig;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.Calendar;
import java.util.Date;

/**
 * 基类
 * @author chenpengfei
 */
public abstract class AbstractJob implements DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(AbstractJob.class);

    protected String group;
    protected String jobName;
    protected String cronExpr;
    protected String jobMethod;
    protected String triggerName;
    private Scheduler scheduler;
    private CronTriggerImpl cronTrigger;
    private TriggerKey triggerKey;
    private String schedulerName;
    private long jobFireInterval = 0;//任务执行间隔（毫秒）
	private String extraData;
    
    //默认允许多个节点时间误差
    private static final long DEFAULT_ALLOW_DEVIATION = 1000 * 60 * 15;
    
    private boolean executeOnStarted;//启动是否立即执行
    
    private int retries = 0;//失败重试次数

	public void setGroup(String group) {
		this.group = StringUtils.trimToNull(group);
	}
	
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = StringUtils.trimToNull(jobName);
	}

	public String getCronExpr() {
		return cronExpr;
	}

	public void setCronExpr(String cronExpr) {
		this.cronExpr = StringUtils.trimToNull(cronExpr);
	}

	public boolean isExecuteOnStarted() {
		return executeOnStarted;
	}

	public void setExecuteOnStarted(boolean executeOnStarted) {
		this.executeOnStarted = executeOnStarted;
	}

	public String getJobMethod() {
		return jobMethod;
	}

	public void setJobMethod(String jobMethod) {
		this.jobMethod = jobMethod;
	}

	public String getTriggerName() {
		return triggerName;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	public String getExtraData() {
		return extraData;
	}

	public void setExtraData(String extraData) {
		this.extraData = extraData;
	}

	public String getGroup() {
		return group;
	}

	protected Scheduler getScheduler() {
        if (scheduler == null)
            scheduler = InstanceFactory.getInstance(Scheduler.class);
        return scheduler;
    }

	public String getSchedulerName() {
		return schedulerName;
	}

	public void setSchedulerName(String schedulerName) {
		this.schedulerName = schedulerName;
	}

	/**
	 * quartz驱动的方法
	 */
	public void execute() {
		// 避免InstanceFactory还未初始化，就执行
		InstanceFactory.waitUtilInitialized();

		JobConfig schConf = JobContext.getContext().getRegistry().getConf(jobName,false);
		if (currentNodeIgnore(schConf))
			return;
		Date beginTime = null;
		Exception exception = null;
		try {
			// 更新状态
			beginTime = getPreviousFireTime();
			JobContext.getContext().getRegistry().setRuning(jobName, beginTime);
			logger.debug("Job_{} at node[{}] execute begin...", jobName, JobContext.getContext().getNodeId());
			// 执行
			doJob(JobContext.getContext());
			logger.debug("Job_{} at node[{}] execute finish", jobName, JobContext.getContext().getNodeId());
		} catch (Exception e) {
			//重试
			if(retries > 0)JobContext.getContext().getRetryProcessor().submit(this, retries);
			logger.error("Job_" + jobName + " execute error", e);
			exception = e;
		}
		Date nextFireTime = getTrigger().getNextFireTime();
		JobContext.getContext().getRegistry().setStoping(jobName, nextFireTime,exception);
		//运行日志持久化
//		doJobLogPersist(schConf, exception, nextFireTime);
		// 重置cronTrigger，重新获取才会更新previousFireTime，nextFireTime
		cronTrigger = null;
	}

	/**
	 * @param schConf
	 * @param exception
	 * @param nextFireTime
	 */
	private void doJobLogPersist(JobConfig schConf, Exception exception, Date nextFireTime) {
		if(JobContext.getContext().getJobLogPersistHandler() != null){
			try {
				if(exception == null){
					JobContext.getContext().getJobLogPersistHandler().onSucess(schConf, nextFireTime);
				}else{
					JobContext.getContext().getJobLogPersistHandler().onError(schConf, nextFireTime, exception);
				}
			} catch (Exception e) {
				logger.warn("JobLogPersistHandler run error",e);
			}
		}
	}

    
    private Date getPreviousFireTime(){
    	return getTrigger().getPreviousFireTime() == null ? new Date() : getTrigger().getPreviousFireTime();
    }
  
    
    protected boolean currentNodeIgnore(JobConfig schConf) {
    	if(parallelEnabled())return false;
    	if (schConf == null) return true;  //任务删除时触发
        try {
            if (!schConf.isActive()) {
            	logger.debug("Job_{} 已禁用,终止执行", jobName);
                return true;
            }
            
            //执行间隔（秒）
           // long interval = getJobFireInterval();
            long currentTimes = Calendar.getInstance().getTime().getTime();
            
            if(schConf.getNextFireTime() != null){
            	//下次执行时间 < 当前时间强制执行
            	if(currentTimes - schConf.getNextFireTime().getTime() > DEFAULT_ALLOW_DEVIATION){
                	logger.debug("Job_{} NextFireTime[{}] before currentTime[{}],re-join-execute task ",jobName,currentTimes,schConf.getNextFireTime().getTime());
                	return false;
                }
            	//如果多个节点做了时间同步，那么误差应该为0才触发任务执行，但是考虑一些误差因素，可以做一个误差容错
//            	if(schConf.getLastFireTime() != null){            		
//            		long deviation = Math.abs(currentTimes - schConf.getLastFireTime().getTime() - interval);
//            		if(interval > 0 && deviation > DEFAULT_ALLOW_DEVIATION){
//            			logger.info("Job_{} interval:{},currentTimes:{},expect tiggertime:{}", jobName,interval,currentTimes, schConf.getLastFireTime().getTime());
//            			return true;
//            		}
//            	}
            }
			
            
          //如果执行节点不为空,且不等于当前节点
            if(StringUtils.isNotBlank(schConf.getCurrentNodeId()) ){
            	if(!JobContext.getContext().getNodeId().equals(schConf.getCurrentNodeId())){
            		logger.debug("Job_{} 指定执行节点:{}，不匹配当前节点:{}", jobName,schConf.getCurrentNodeId(),JobContext.getContext().getNodeId());
            		return true;
            	}
            	//如果分配了节点，则可以保证本节点不会重复执行则不需要判断runing状态
            }else{  
            	if (schConf.isRunning()) {
            		//如果某个节点开始了任务但是没有正常结束导致没有更新任务执行状态
            		logger.info("Job_{} 其他节点[{}]正在执行,终止当前执行", schConf.getCurrentNodeId(),jobName);
            		return true;
            	}
            }

            
            this.cronExpr = schConf.getCronExpr();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return false;
    }
    
    public void resetTriggerCronExpr(String newCronExpr) {  
        try {   
        	if(getTrigger() == null)return;
            String originConExpression = getTrigger().getCronExpression();  
            //判断任务时间是否更新过  
            if (!originConExpression.equalsIgnoreCase(newCronExpr)) {  
            	getTrigger().setCronExpression(newCronExpr);  
                getScheduler().rescheduleJob(triggerKey, getTrigger()); 
                getScheduler().resumeTrigger(triggerKey);
                logger.info("Job_{} CronExpression changed, origin:{},current:{}",jobName,originConExpression,newCronExpr);
            }  
        } catch (Exception e) {
        	logger.error("checkConExprChange error",e);
        }  
         
    }  
    
   
    
    /**
     * 获取任务执行间隔
     * @return
     * @throws SchedulerException
     */
    private long getJobFireInterval(){
    	if(jobFireInterval == 0){   
    		try {				
    			Date nextFireTime = getTrigger().getNextFireTime();
    			Date previousFireTime = getTrigger().getPreviousFireTime();
    			jobFireInterval = nextFireTime.getTime() - previousFireTime.getTime();
			} catch (Exception e) {}
    	}
    	return jobFireInterval;
    }

    
    private CronTriggerImpl getTrigger() {
    	try {
    		if(this.cronTrigger == null){   
    			if(getScheduler() == null)return null;
        		Trigger trigger = getScheduler().getTrigger(triggerKey);
        		this.cronTrigger = (CronTriggerImpl)trigger;
        	}
		} catch (SchedulerException e) {
			logger.error("Job_"+jobName+" Invoke getTrigger error",e);
		}
        return cronTrigger;
    }

	/**
	 * 删除任务
	 * @param jobConfig
	 */
	public void deleteJob (JobConfig jobConfig) {
    	String jobDetailName = String.format("%s%s", jobConfig.getJobName(),"JobDetail");
		try {
			JobKey jobKey = new JobKey(jobDetailName);
			getScheduler().deleteJob(jobKey);
		} catch (SchedulerException e) {
			logger.error("[schedule] delete job error: {}", e);
		}
	}

    @Override
	public void destroy() throws Exception {
    	JobContext.getContext().getRegistry().unregister(jobName);
    }

	public void init()  {
		
		triggerName = jobName + "Trigger";
		
		triggerKey = new TriggerKey(triggerName, group);
		
		JobConfig jobConfg = new JobConfig(group,jobName, jobMethod, cronExpr, schedulerName, extraData);

		//从持久化配置合并
			if(JobContext.getContext().getConfigPersistHandler() != null){
				JobContext.getContext().getConfigPersistHandler().merge(jobConfg);
		}

		JobContext.getContext().getRegistry().register(jobConfg);

		logger.info("Initialized Job_{} OK!!", jobName);
	}

	public void afterInitialized()  {
		//启动重试任务
		if(retries > 0){
			JobContext.getContext().startRetryProcessor();
		}
		//这里不能提前去拿下一次更新时间，否则真正执行后下次执行时间不更新
//		if(executeOnStarted)return;
//		JobConfig conf = JobContext.getContext().getRegistry().getConf(jobName,false);
//		Date nextFireTime = getNextFireTime();
//		if(nextFireTime != null){			
//			conf.setNextFireTime(nextFireTime);
//			JobContext.getContext().getRegistry().updateJobConfig(conf);
//		}
		
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jobName == null) ? 0 : jobName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractJob other = (AbstractJob) obj;
		if (jobName == null) {
			if (other.jobName != null)
				return false;
		} else if (!jobName.equals(other.jobName))
			return false;
		return true;
	}

	/**
	 * 是否开启并行处理
	 * @return
	 */
	public abstract boolean  parallelEnabled();
	
	public abstract JobConfig doJob(JobContext context) throws Exception;

}
