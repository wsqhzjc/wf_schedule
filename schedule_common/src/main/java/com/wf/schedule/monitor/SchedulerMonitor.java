/**
 * 
 */
package com.wf.schedule.monitor;

import com.wf.schedule.common.context.ZkConstant;
import com.wf.schedule.common.spring.InstanceFactory;
import com.wf.schedule.common.util.GfJsonUtil;
import com.wf.schedule.common.zk.serializer.StringSerializer;
import com.wf.schedule.model.JobConfig;
import com.wf.schedule.model.JobGroupInfo;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenpengfei
 */
public class SchedulerMonitor implements Closeable{
	
	private static final Logger logger = LoggerFactory.getLogger(SchedulerMonitor.class);

	private ZkClient zkClient;

	public SchedulerMonitor() {
		zkClient = InstanceFactory.getInstance(ZkClient.class);
	}

	public SchedulerMonitor(String servers) {
		ZkConnection zkConnection = new ZkConnection(servers);
		zkClient = new ZkClient(zkConnection);
		zkClient.setZkSerializer(new StringSerializer());
	}


	@Override
	public void close() throws IOException {
		if (zkClient != null) {
			zkClient.close();
		}
	}
	
	public JobGroupInfo getJobGroupInfo(String groupName){

		JobGroupInfo groupInfo = new JobGroupInfo();
		groupInfo.setName(groupName);
		//
		String path = ZkConstant.ROOT_NODE + groupName;
		List<String> children = zkClient.getChildren(path);
		for (String child : children) {
			if(ZkConstant.SERVER_NODES.equals(child)){
				path = ZkConstant.ROOT_NODE + groupName + String.format("/%s", ZkConstant.SERVER_NODES);
				groupInfo.setClusterNodes(zkClient.getChildren(path));
			}else{
				path = ZkConstant.ROOT_NODE + groupName + "/" + child;
				Object data = zkClient.readData(path);
				if(data != null){
					if (data.toString().contains("jobName")) {
						JobConfig jobConfig = GfJsonUtil.parseObject(data.toString(), JobConfig.class);
						groupInfo.getJobs().add(jobConfig);
					}
				}
			}
		}
		
//		if(groupInfo.getClusterNodes().size() > 0){
			return groupInfo;
//		}
		
//		return null;
	
	}
	
	public List<String> getGroups(){
		String path = ZkConstant.ROOT_NODE.substring(0,ZkConstant.ROOT_NODE.length() - 1);
		return zkClient.getChildren(path);
	}
	
	public List<JobGroupInfo> getAllJobGroups(){
		//zk registry
		List<JobGroupInfo> result = new ArrayList<>();
		List<String> groupNames = getGroups();
		if (groupNames == null) {
			return result;
		}
		for (String groupName : groupNames) {
			JobGroupInfo groupInfo = getJobGroupInfo(groupName);
			
			if(groupInfo != null){				
				result.add(groupInfo);
			}
		}
		return result;
	}
	
	public void publishEvent(MonitorCommond cmd){
		String path = String.format("%s%s/%s", ZkConstant.ROOT_NODE, cmd.getJobGroup(), ZkConstant.SERVER_NODES);
		List<String> nodeIds = zkClient.getChildren(path);
		for (String node : nodeIds) {
				String nodePath = path + "/" + node;
				zkClient.writeData(nodePath, GfJsonUtil.toJSONString(cmd));
				logger.info("publishEvent finish，path:{},content:{}",nodePath,cmd);
				break;
		}
	}
	
	public void clearInvalidGroup(){

    	List<String> groups = zkClient.getChildren(ZkConstant.ROOT_NODE.substring(0, ZkConstant.ROOT_NODE.length() - 1));
    	logger.info("==============clear Invalid jobs=================");
    	for (String group : groups) {
    		String groupPath = ZkConstant.ROOT_NODE + group;
    		String nodeStateParentPath = String.format("%s/%s", groupPath, ZkConstant.SERVER_NODES);
    		try {
    			if(zkClient.exists(nodeStateParentPath) == false || zkClient.countChildren(nodeStateParentPath) == 0){
    				List<String> jobs = zkClient.getChildren(groupPath);
    				for (String job : jobs) {
    					zkClient.delete(groupPath + "/" + job);
    					logger.info("delete path:{}/{}",groupPath,job);
    				}
    				zkClient.delete(groupPath);
    				logger.info("delete path:{}",groupPath);
    			}
			} catch (Exception e) {}
		}
    	logger.info("==============clear Invalid jobs end=================");
    	
    
	}
	
	public static void main(String[] args) throws IOException {
		SchedulerMonitor monitor = new SchedulerMonitor();
		
		List<JobGroupInfo> groups = monitor.getAllJobGroups();
		System.out.println(GfJsonUtil.toJSONString(groups));
		
		monitor.close();
	}

}
