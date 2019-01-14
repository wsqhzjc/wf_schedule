/**
 * 
 */
package com.wf.schedule.core.context;

import com.wf.schedule.common.util.ConsistencyHash;
import com.wf.schedule.common.util.IpUtils;
import com.wf.schedule.core.AbstractJob;
import com.wf.schedule.core.TaskRetryProcessor;
import com.wf.schedule.core.handler.ConfigPersistHandler;
import com.wf.schedule.core.handler.JobLogPersistHandler;
import com.wf.schedule.register.NullJobRegistry;
import com.wf.schedule.register.base.JobRegistry;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobContext {

	private static JobContext context = new JobContext();
	
	private Set<String> activeNodes = new HashSet<String>();
	
	private ConsistencyHash hash = new ConsistencyHash();
	
	private String groupName;
	
	private Map<String, AbstractJob> allJobs = new HashMap<>();
	
	private ConfigPersistHandler configPersistHandler;
	
	private TaskRetryProcessor retryProcessor;
	
	private JobLogPersistHandler jobLogPersistHandler;
	
	private JobRegistry registry;
	
	private ExecutorService syncExecutor = Executors.newFixedThreadPool(1);
	
	public void startRetryProcessor(){
		if(retryProcessor == null){
			synchronized (context) {
				if(retryProcessor != null)return;
				retryProcessor = new TaskRetryProcessor(1);
			}
		}
	}

	public static JobContext getContext() {
		return context;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getNodeId() {
		return IpUtils.getIp();
	}
	
	public ConfigPersistHandler getConfigPersistHandler() {
		return configPersistHandler;
	}

	public void setConfigPersistHandler(ConfigPersistHandler configPersistHandler) {
		this.configPersistHandler = configPersistHandler;
	}

	public JobLogPersistHandler getJobLogPersistHandler() {
		return jobLogPersistHandler;
	}

	public void setJobLogPersistHandler(JobLogPersistHandler jobLogPersistHandler) {
		this.jobLogPersistHandler = jobLogPersistHandler;
	}

	public JobRegistry getRegistry() {
		if(registry == null){
			registry = new NullJobRegistry();
		}
		return registry;
	}

	public void setRegistry(JobRegistry registry) {
		this.registry = registry;
	}

	public TaskRetryProcessor getRetryProcessor() {
		return retryProcessor;
	}

	public void refreshNodes(List<String> nodes){
		activeNodes.clear();
		activeNodes.addAll(nodes);
		if(activeNodes.isEmpty())return;
		hash.refresh(nodes);
	}
	
	public void addNode(String node){
		activeNodes.add(node);
		hash.refresh(new ArrayList<>(activeNodes));
	}
	
	public void removeNode(String node){
		activeNodes.remove(node);
		if(activeNodes.isEmpty())return;
		hash.refresh(new ArrayList<>(activeNodes));
	}
	
	public boolean matchCurrentNode(Object shardFactor){
		if(activeNodes.size() == 1)return true;
		String expectNodeId = hash.getAssignedRealNode(shardFactor, getNodeId());
		return expectNodeId.equals(getNodeId());
	}
	
	public void addJob(AbstractJob job){
		String key = job.getGroup() + ":" + job.getJobName();
		allJobs.put(key, job);
	}

	public Map<String, AbstractJob> getAllJobs() {
		return allJobs;
	}

	public Set<String> getActiveNodes() {
		return activeNodes;
	}
	
	public void submitSyncTask(Runnable task){
		syncExecutor.execute(task);
	}
	
	public void close(){
		if(retryProcessor != null){
			retryProcessor.close();
		}
		syncExecutor.shutdown();
	}

}
