/**
 * 
 */
package com.wf.schedule.core;

import com.google.common.collect.Lists;
import com.wf.schedule.common.spring.InstanceFactory;
import com.wf.schedule.common.spring.SpringAopHelper;
import com.wf.schedule.common.spring.SpringInstanceProvider;
import com.wf.schedule.common.util.TaskUtils;
import com.wf.schedule.core.context.JobContext;
import com.wf.schedule.core.handler.ConfigPersistHandler;
import com.wf.schedule.core.handler.JobLogPersistHandler;
import com.wf.schedule.core.handler.bean.ZkJobPersistBean;
import com.wf.schedule.model.JobConfig;
import com.wf.schedule.model.JobGroupInfo;
import com.wf.schedule.monitor.SchedulerMonitor;
import com.wf.schedule.register.ZkJobRegistry;
import com.wf.schedule.register.base.JobRegistry;
import org.apache.commons.lang3.Validate;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

public class SchedulerFactoryBeanWrapper implements ApplicationContextAware,InitializingBean,DisposableBean,PriorityOrdered {

	protected static final Logger logger = LoggerFactory.getLogger(SchedulerFactoryBeanWrapper.class);

	private ApplicationContext context;
	
	private String groupName;

	List<AbstractJob> schedulers = new ArrayList<>();
	
	private int threadPoolSize;

	private boolean firstInitFlag = true;
	
	public void setGroupName(String groupName) {
		this.groupName = groupName;
		JobContext.getContext().setGroupName(groupName);
	}

	public void setSchedulers(List<AbstractJob> schedulers) {
		this.schedulers = schedulers;
	}

	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	public void setConfigPersistHandler(ConfigPersistHandler configPersistHandler) {
		JobContext.getContext().setConfigPersistHandler(configPersistHandler);
	}
	
	public void setRegistry(JobRegistry registry) {
		JobContext.getContext().setRegistry(registry);
	}
	
	public void setJobLogPersistHandler(JobLogPersistHandler jobLogPersistHandler) {
		JobContext.getContext().setJobLogPersistHandler(jobLogPersistHandler);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
		InstanceFactory.setInstanceProvider(new SpringInstanceProvider(context));
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (firstInitFlag) {
			setJobLogPersistHandler(ZkJobPersistBean.getInstance());
			firstInitFlag = false;
		}
		logger.info("[scheduler] reload start:{}", Calendar.getInstance().getTimeInMillis());
		schedulers.clear();
		Validate.notBlank(groupName);
		JobContext.getContext().getRegistry().createGroup(groupName);
		//JobContext.getContext().getRegistry().subcribeState();

		DefaultListableBeanFactory acf = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();
		
		List<Trigger> triggers = new ArrayList<>();

		/** 获取已注册的任务 start **/
		SchedulerMonitor monitor = new SchedulerMonitor();
		JobGroupInfo jobGroupInfo = monitor.getJobGroupInfo(groupName);
		List<JobConfig> jobList = jobGroupInfo.getJobs();
		jobList.forEach(jobConfig -> {
			AbstractJob sch  = new AbstractJob() {
				@Override
				public boolean parallelEnabled() {
					return false;
				}

				@Override
				public JobConfig doJob(JobContext context) throws Exception {
					/**  如需要动态变更参数，需将变更后的Jobconfig数带入到jobcontext **/
					JobRegistry jobRegistry = InstanceFactory.getInstance(ZkJobRegistry.class, "jobRegistry");
					if (jobRegistry != null) {
						JobConfig newJobConfig = jobRegistry.getConf(jobConfig.getJobName(), false);
						logger.info("job start.....,jobname:{},nodeId:{}, extraData:{}",
								newJobConfig.getJobName(), context.getNodeId(), newJobConfig.getExtraData());
						TaskUtils.invokMethod(newJobConfig);
						JobContext.getContext().getJobLogPersistHandler().persist(newJobConfig);
						return newJobConfig;
					} else {
						logger.info("job start.....,jobname:{},nodeId:{}, extraData:{}",
								jobConfig.getJobName(), context.getNodeId(), jobConfig.getExtraData());
						TaskUtils.invokMethod(jobConfig);
						JobContext.getContext().getJobLogPersistHandler().persist(jobConfig);
					}
					return jobConfig;
				}
			};
			sch.setCronExpr(jobConfig.getCronExpr());
			sch.setExecuteOnStarted(false);
			sch.setJobName(jobConfig.getJobName());
			sch.setJobMethod(jobConfig.getJobMethod());
			sch.setSchedulerName(jobConfig.getSchedulerName());
			sch.setExtraData(jobConfig.getExtraData());
			schedulers.add(sch);
		});
		/** 获取已注册的任务 start **/
		
		/** 更新zk配置；注册任务到trigger start **/
		List<String> allJobNames = Lists.newArrayList();
		List<JobConfig> jobConfigs = JobContext.getContext().getRegistry().getAllJobs();
		jobConfigs.forEach(jobConfig -> allJobNames.add(jobConfig.getJobName()));
		// 初始化节点的订阅
		if (schedulers.size() > 0) {
			for (AbstractJob sch : schedulers) {
				if (!allJobNames.contains(sch.getJobName())) {
					sch.setGroup(groupName);
					sch.init();
					triggers.add(registerSchedulerTriggerBean(acf,sch));
				}

			}
		} else {
			JobContext.getContext().getRegistry().registerGroup(groupName);
		}
		/** 更新zk配置；注册任务到trigger end **/

		/** 注册任务到quartz上下文 start**/
		SchedulerFactoryBean schedulerFactoryBean = InstanceFactory.getInstance(SchedulerFactoryBean.class);
		if (schedulerFactoryBean == null) {
			String beanName = "schedulerFactory";
			BeanDefinitionBuilder beanDefBuilder = BeanDefinitionBuilder.genericBeanDefinition(SchedulerFactoryBean.class);
			beanDefBuilder.addPropertyValue("triggers", triggers);

			Properties quartzProperties = new Properties();

			threadPoolSize = threadPoolSize > 0 ? threadPoolSize :
					(schedulers.size() > 10 ? (schedulers.size()/2 + 1)  : 1); //poolSize必须大于0
			quartzProperties.setProperty(SchedulerFactoryBean.PROP_THREAD_COUNT, String.valueOf(threadPoolSize));
			beanDefBuilder.addPropertyValue("quartzProperties", quartzProperties);
			logger.info("init Scheduler threadPoolSize:"+threadPoolSize);

			acf.registerBeanDefinition(beanName, beanDefBuilder.getRawBeanDefinition());

			for ( AbstractJob sch : schedulers) {

				final AbstractJob job = (AbstractJob) SpringAopHelper.getTarget(sch);
				//
				JobContext.getContext().addJob(job);
				//
				JobContext.getContext().submitSyncTask(new Runnable() {
					@Override
					public void run() {
						InstanceFactory.waitUtilInitialized();
						job.afterInitialized();
						if(job.isExecuteOnStarted()){
							logger.info("<<Job[{}] execute on startup....",job.jobName);
							job.execute();
							logger.info(">>Job[{}] execute on startup ok!",job.jobName);
						}
					}
				});

				logger.info(">>>>>>> Job[{}][{}]-Class[{}]  initialized finish ",job.group,job.jobName,job.getClass().getName());
			}
			/** 注册任务到quartz上下文 end **/

			//

			} else {
				for ( AbstractJob sch : schedulers) {
				final AbstractJob job = (AbstractJob) SpringAopHelper.getTarget(sch);
				JobContext.getContext().addJob(job);
			}
			triggers.forEach(trigger -> {
				try {
					JobDetail jobDetail = InstanceFactory.getInstance(JobDetail.class, trigger.getJobKey().getName());
					schedulerFactoryBean.getScheduler().scheduleJob(jobDetail, trigger);
				} catch (SchedulerException e) {
					e.printStackTrace();
				}
			});
		}

		JobContext.getContext().getRegistry().onRegistered();
		logger.info("[scheduler] reload end:{}", Calendar.getInstance().getTimeInMillis());
	}

	/**
	 * 注册到TriggerBean
	 * @param acf
	 * @param sch
	 * @return
	 */
	public Trigger registerSchedulerTriggerBean(DefaultListableBeanFactory acf, AbstractJob sch) {
		//注册JobDetail
		String jobDetailBeanName = sch.getJobName() + "JobDetail";
		if(context.containsBean(jobDetailBeanName)){
			logger.warn("duplicate jobName["+sch.getJobName()+"] defined!!");
			acf.removeBeanDefinition(jobDetailBeanName);
		}
		BeanDefinitionBuilder beanDefBuilder = BeanDefinitionBuilder.genericBeanDefinition(MethodInvokingJobDetailFactoryBean.class);
		beanDefBuilder.addPropertyValue("targetObject", sch);
		beanDefBuilder.addPropertyValue("targetMethod", "execute");
		beanDefBuilder.addPropertyValue("concurrent", false);
		acf.registerBeanDefinition(jobDetailBeanName, beanDefBuilder.getRawBeanDefinition());

		//注册Trigger
		String triggerBeanName = sch.getJobName() + "Trigger";
		beanDefBuilder = BeanDefinitionBuilder.genericBeanDefinition(CronTriggerFactoryBean.class);
		beanDefBuilder.addPropertyReference("jobDetail", jobDetailBeanName);
		beanDefBuilder.addPropertyValue("cronExpression", sch.getCronExpr());
		beanDefBuilder.addPropertyValue("group", groupName);
		acf.registerBeanDefinition(triggerBeanName, beanDefBuilder.getRawBeanDefinition());
		
		return (Trigger) context.getBean(triggerBeanName);
		
	}
	
	@Override
	public void destroy() throws Exception {
		JobContext.getContext().close();
	}
	
	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

}
