package com.wf.schedule.admin.controller;

import com.alibaba.fastjson.JSONObject;
import com.wf.schedule.admin.mapper.TaskLogMapper;
import com.wf.schedule.admin.model.ExtModel;
import com.wf.schedule.admin.zk.JobCacheHandler;
import com.wf.schedule.admin.zk.LogCacheHandler;
import com.wf.schedule.common.context.ZkConstant;
import com.wf.schedule.common.util.GfJsonUtil;
import com.wf.schedule.model.JobConfig;
import com.wf.schedule.model.JobGroupInfo;
import com.wf.schedule.monitor.MonitorCommond;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.zookeeper.data.Stat;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/schedule/private/ScheduleController")
public class ScheduleController extends BaseController {
    Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    @Autowired
    private JobCacheHandler jobCacheHandler;

    @Autowired
    private LogCacheHandler logCacheHandler;

    @Autowired
    private TaskLogMapper taskLogMapper;

    @RequestMapping("listGroup")
    public Object listGroup() throws Exception {
        ExtModel em = new ExtModel();
        List<JobGroupInfo> osList = new ArrayList<>();
        PathChildrenCache childData = jobCacheHandler.getNameSpaceChildrenCache();
        for (ChildData jobGroup : childData.getCurrentData()) {
            JobGroupInfo jobGroupInfo = new JobGroupInfo();
            jobGroupInfo.setName(jobGroup.getPath().replace("/", ""));

            PathChildrenCache taskCache = jobCacheHandler.getChildrenCache(jobGroup.getPath());
            List<ChildData> nodes = taskCache.getCurrentData();
            List<String> nodeList = new ArrayList<>();
            for (ChildData node : nodes) {
                if (node.getPath().lastIndexOf(ZkConstant.SERVER_NODES) > 0) {
                    PathChildrenCache nodeIpCache = jobCacheHandler.getChildrenCache(node.getPath());
                    for (ChildData nodeIp : nodeIpCache.getCurrentData()) {
                        String p = nodeIp.getPath();
                        String[] split = p.split("/");
                        nodeList.add(split[split.length-1]);
                    }
                }
            }
            jobGroupInfo.setClusterNodes(nodeList);
            osList.add(jobGroupInfo);
        }
        em.setData(osList);
        return em;
    }

    @RequestMapping("listTask")
    public Object listTask(@RequestBody JSONObject jsonObject) throws Exception {
        ExtModel em = new ExtModel();
        String groupName = jsonObject.getJSONObject("data").getString("groupName");
        List<String> nodeList = jobCacheHandler.getNodeList(groupName);

        PathChildrenCache childrenCache = jobCacheHandler.getChildrenCache(String.format("/%s", groupName));
        List<JobConfig> taskList = new ArrayList<>();
        for (ChildData childData : childrenCache.getCurrentData()) {
            if (childData.getPath().lastIndexOf(ZkConstant.SERVER_NODES) > 0) {

            } else {
                String data = new String(childData.getData(), "utf-8");
                JobConfig jobConfig = GfJsonUtil.parseObject(data, JobConfig.class);
                if(nodeList.size() == 0) {
                    jobConfig.setCurrentNodeId("");
                }
                taskList.add(jobConfig);
            }
        }
        em.setData(taskList);
        return em;
    }

    @RequestMapping("showTask")
    public Object showTask(@RequestBody JSONObject jsonObject) throws Exception {
        String groupName = jsonObject.getString("groupName");
        String jobName = jsonObject.getString("jobName");
        NodeCache jobNodeCache = jobCacheHandler.getNodeCache(String.format("/%s/%s", groupName, jobName));
        String data = new String(jobNodeCache.getCurrentData().getData(), "utf-8");
        JobConfig jobConfig = GfJsonUtil.parseObject(data, JobConfig.class);

        PathChildrenCache nodesCache = jobCacheHandler.getChildrenCache(String.format("/%s/%s", groupName, jobName));
        List<String> nodesList = new ArrayList<>();
        for (ChildData node : nodesCache.getCurrentData()) {
            String nodeId = node.getPath().replace("/", "");
            nodesList.add(nodeId);
        }

        Map<String, Object> ret = new HashMap<>();
        ret.put("jobConfig", jobConfig);
        ret.put("nodes", nodesList);
        return ret;
    }

    @RequestMapping("validateMultiple")
    public Object validateMultiple(@RequestBody JSONObject jsonObject) throws Exception {
        ExtModel em = new ExtModel();
        String groupName = jsonObject.getString("groupName");
        String jobName = jsonObject.getString("jobName");
        CuratorFramework cf = jobCacheHandler.getZKConnection().getRootNameSpaceCurator();
        Stat stat = cf.checkExists().forPath(String.format("/%s/%s", groupName, jobName));
        if (stat == null) {
            em.setSuccess(true);
        } else {
            em.setSuccess(false);
        }
        return em;
    }

    @RequestMapping("addTask")
    public Object addTask(@RequestBody JobConfig jobConfig) throws Exception {
        ExtModel em = new ExtModel();
        if (!CronExpression.isValidExpression(jobConfig.getCronExpr())) {
            em.setSuccess(false);
            em.setData("定时表达式错误");
            return em;
        }
        CuratorFramework cf = jobCacheHandler.getZKConnection().getRootNameSpaceCurator();
        cf.create().forPath(String.format("/%s/%s", jobConfig.getGroupName(), jobConfig.getJobName()), GfJsonUtil.toJSONString(jobConfig).getBytes());
        return em;
    }


    @RequestMapping("updateTask")
    public Object updateTask(@RequestBody JobConfig jobConfig) throws Exception {
        ExtModel em = new ExtModel();
        if (!CronExpression.isValidExpression(jobConfig.getCronExpr())) {
            em.setSuccess(false);
            em.setData("定时表达式错误");
            return em;
        }
        List<String> nodeList = jobCacheHandler.getNodeList(jobConfig.getGroupName());
        if(nodeList.size() == 0) {
            em.setSuccess(false);
            em.setData("当前无可运行节点");
            return em;
        }
        NodeCache jobNodeCache = jobCacheHandler.getNodeCache(String.format("/%s/%s", jobConfig.getGroupName(), jobConfig.getJobName()));
        String jobCache = new String(jobNodeCache.getCurrentData().getData(), "utf-8");
        JobConfig jobConfigCache = GfJsonUtil.parseObject(jobCache, JobConfig.class);
        if(!jobConfigCache.getCronExpr().equals(jobConfig.getCronExpr())) {
            CuratorFramework cf = jobCacheHandler.getZKConnection().getRootNameSpaceCurator();
            MonitorCommond monitorCommond = new MonitorCommond(MonitorCommond.TYPE_CRON_MOD, jobConfig.getGroupName(), jobConfig.getJobName(), jobConfig.getCronExpr());
            for (String nodeId : nodeList) {
                cf.setData().forPath(String.format("/%s/%s/%s", jobConfig.getGroupName(),ZkConstant.SERVER_NODES, nodeId), GfJsonUtil.toJSONString(monitorCommond).getBytes());
            }
        }

        if(!jobConfigCache.getExtraData().equals(jobConfig.getExtraData())) {
            CuratorFramework cf = jobCacheHandler.getZKConnection().getRootNameSpaceCurator();
            MonitorCommond monitorCommond = new MonitorCommond(MonitorCommond.TYPE_PARAM_MOD, jobConfig.getGroupName(), jobConfig.getJobName(), jobConfig.getExtraData());
            for (String nodeId : nodeList) {
                cf.setData().forPath(String.format("/%s/%s/%s", jobConfig.getGroupName(),ZkConstant.SERVER_NODES, nodeId), GfJsonUtil.toJSONString(monitorCommond).getBytes());
            }
        }

        CuratorFramework cf = jobCacheHandler.getZKConnection().getRootNameSpaceCurator();
        cf.setData().forPath(String.format("/%s/%s", jobConfig.getGroupName(), jobConfig.getJobName()), GfJsonUtil.toJSONString(jobConfig).getBytes());
        return em;
    }

    @RequestMapping("startAllTaskJob")
    public Object startAllTaskJob(@RequestBody JSONObject jsonObject) throws Exception {
        ExtModel em = new ExtModel();
        String groupName = jsonObject.getString("groupName");
        PathChildrenCache childrenCache = jobCacheHandler.getChildrenCache(String.format("/%s", groupName));
        List<JobConfig> taskList = new ArrayList<>();
        for (ChildData childData : childrenCache.getCurrentData()) {
            if (childData.getPath().lastIndexOf(ZkConstant.SERVER_NODES) > 0) {

            } else {
                String data = new String(childData.getData(), "utf-8");
                taskList.add(GfJsonUtil.parseObject(data, JobConfig.class));
            }
        }
        for (JobConfig jobConfig : taskList) {
            startTask(jobConfig.getGroupName(), jobConfig.getJobName());
        }
        return em;
    }

    @RequestMapping("stopAllTaskJob")
    public Object stopAllTaskJob(@RequestBody JSONObject jsonObject) throws Exception {
        ExtModel em = new ExtModel();
        String groupName = jsonObject.getString("groupName");

        PathChildrenCache childrenCache = jobCacheHandler.getChildrenCache(String.format("/%s", groupName));
        List<JobConfig> taskList = new ArrayList<>();
        for (ChildData childData : childrenCache.getCurrentData()) {
            if (childData.getPath().lastIndexOf(ZkConstant.SERVER_NODES) > 0) {

            } else {
                String data = new String(childData.getData(), "utf-8");
                taskList.add(GfJsonUtil.parseObject(data, JobConfig.class));
            }
        }

        for (JobConfig jobConfig : taskList) {
            stopTask(jobConfig.getGroupName(), jobConfig.getJobName());
        }
        return em;
    }

    @RequestMapping("executeTaskJobNow")
    public Object executeTaskJobNow(@RequestBody JSONObject jsonObject) throws Exception {
        ExtModel em = new ExtModel();
        String groupName = jsonObject.getString("groupName");
        String jobName = jsonObject.getString("jobName");

        List<String> nodeList = jobCacheHandler.getNodeList(groupName);
        if(nodeList.size() == 0) {
            em.setSuccess(false);
            em.setData("当前无可运行节点");
            return em;
        }
        CuratorFramework cf = jobCacheHandler.getZKConnection().getRootNameSpaceCurator();
        MonitorCommond monitorCommond = new MonitorCommond(MonitorCommond.TYPE_EXEC, groupName, jobName, null);
        for (String nodeId : nodeList) {
            cf.setData().forPath(String.format("/%s/%s/%s", groupName,ZkConstant.SERVER_NODES, nodeId), GfJsonUtil.toJSONString(monitorCommond).getBytes());
        }
        return em;
    }

    @RequestMapping("stopTaskJobNow")
    public Object stopTaskJobNow(@RequestBody JSONObject jsonObject) throws Exception {
        ExtModel em = new ExtModel();
        String groupName = jsonObject.getString("groupName");
        String jobName = jsonObject.getString("jobName");
        stopTask(groupName, jobName);
        return em;
    }

    @RequestMapping("startTaskJobNow")
    public Object startTaskJobNow(@RequestBody JSONObject jsonObject) throws Exception {
        ExtModel em = new ExtModel();
        String groupName = jsonObject.getString("groupName");
        String jobName = jsonObject.getString("jobName");
        List<String> nodeList = jobCacheHandler.getNodeList(groupName);
        if(nodeList.size() == 0) {
            em.setSuccess(false);
            em.setData("当前无可运行节点");
            return em;
        }
        startTask(groupName, jobName);
        return em;
    }

    @RequestMapping("deleteGroup")
    public Object deleteGroup(@RequestBody JSONObject jsonObject) throws Exception {
        ExtModel em = new ExtModel();
        String groupName = jsonObject.getString("groupName");
        CuratorFramework cf = jobCacheHandler.getZKConnection().getRootNameSpaceCurator();
        cf.delete().deletingChildrenIfNeeded().forPath(String.format("/%s", groupName));
        CuratorFramework logRootNameCurator = logCacheHandler.getCuratorConnection().getLogRootNameCurator();
        logRootNameCurator.delete().deletingChildrenIfNeeded().forPath(String.format("/%s", groupName));
        jobCacheHandler.clearCache();
        logCacheHandler.clearCache();
        return em;
    }

    @RequestMapping("deleteTaskJob")
    public Object deleteTaskJob(@RequestBody JobConfig jobConfig) throws Exception {
        ExtModel em = new ExtModel();
        CuratorFramework cf = jobCacheHandler.getZKConnection().getRootNameSpaceCurator();
        cf.delete().deletingChildrenIfNeeded().forPath(String.format("/%s/%s", jobConfig.getGroupName(), jobConfig.getJobName()));
        CuratorFramework logRootNameCurator = logCacheHandler.getCuratorConnection().getLogRootNameCurator();
        logRootNameCurator.delete().deletingChildrenIfNeeded().forPath(String.format("/%s/%s", jobConfig.getGroupName(), jobConfig.getJobName()));
        jobCacheHandler.clearCache();
        logCacheHandler.clearCache();
        return em;
    }

    @RequestMapping("getTaskResultList")
    public Object getTaskResultList(@RequestBody JSONObject search) throws Exception {
        ExtModel em = new ExtModel();
        Map<String, Object> param = new HashMap<>();
        JSONObject data = search.getJSONObject("data");
        param.put("start", search.getInteger("start"));
        param.put("limit", search.getInteger("limit"));
        String groupName = data.getString("groupName");
        String jobName = data.getString("jobName");
        param.put("groupName", groupName);
        param.put("jobName", jobName);
        em.setData(taskLogMapper.listTaskLog(param));
        return em;

    }

    @RequestMapping("deleteTaskResultList")
    public Object deleteTaskResultList(@RequestBody JSONObject jsonObject) throws Exception {
        ExtModel em = new ExtModel();
        String groupName = jsonObject.getString("groupName");
        String jobName = jsonObject.getString("jobName");
        taskLogMapper.deleteTaskResultList(groupName, jobName);
        return em;
    }

    private void startTask(String groupName, String jobName) throws Exception {
        CuratorFramework cf = jobCacheHandler.getZKConnection().getRootNameSpaceCurator();
        NodeCache jobNodeCache = jobCacheHandler.getNodeCache(String.format("/%s/%s", groupName, jobName));
        String data = new String(jobNodeCache.getCurrentData().getData(), "utf-8");
        JobConfig jobConfig = GfJsonUtil.parseObject(data, JobConfig.class);
        jobConfig.setActive(true);
        cf.setData().forPath(String.format("/%s/%s", groupName, jobName), GfJsonUtil.toJSONString(jobConfig).getBytes());
    }

    private void stopTask(String groupName, String jobName) throws Exception {
        CuratorFramework cf = jobCacheHandler.getZKConnection().getRootNameSpaceCurator();
        NodeCache jobNodeCache = jobCacheHandler.getNodeCache(String.format("/%s/%s", groupName, jobName));
        String data = new String(jobNodeCache.getCurrentData().getData(), "utf-8");
        JobConfig jobConfig = GfJsonUtil.parseObject(data, JobConfig.class);
        jobConfig.setActive(false);
        cf.setData().forPath(String.format("/%s/%s", groupName, jobName), GfJsonUtil.toJSONString(jobConfig).getBytes());
    }


}
