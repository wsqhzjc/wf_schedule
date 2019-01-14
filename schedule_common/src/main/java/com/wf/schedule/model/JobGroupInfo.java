/**
 * 
 */
package com.wf.schedule.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenpengfei
 */
public class JobGroupInfo {

	private String name;

	private Integer haveCluster;
	
	List<JobConfig> jobs = new ArrayList<>();
	
	List<String > clusterNodes = new ArrayList<>();
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<JobConfig> getJobs() {
		return jobs;
	}

	public void setJobs(List<JobConfig> jobs) {
		this.jobs = jobs;
	}

	public List<String> getClusterNodes() {
		return clusterNodes;
	}

	public void setClusterNodes(List<String> clusterNodes) {
		this.clusterNodes = clusterNodes;
	}

	public Integer getHaveCluster() {
		return clusterNodes.size();
	}

	public void setHaveCluster(Integer haveCluster) {
		this.haveCluster = haveCluster;
	}
}
