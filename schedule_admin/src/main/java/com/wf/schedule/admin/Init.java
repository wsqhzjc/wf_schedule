package com.wf.schedule.admin;

import com.alibaba.fastjson.JSONObject;
import com.wf.schedule.admin.mapper.TaskLogMapper;
import com.wf.schedule.admin.po.TaskLogPo;
import com.wf.schedule.admin.zk.LogCacheHandler;
import com.wf.schedule.log.LogExceptionStackTrace;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class Init implements ApplicationListener<ContextRefreshedEvent> {
    Logger logger = LoggerFactory.getLogger(Init.class);

    @Autowired
    private TaskLogMapper taskLogMapper;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LogCacheHandler logCuratorConnection = event.getApplicationContext().getBean(LogCacheHandler.class);
        try {
            PathChildrenCache jobGroupCache = logCuratorConnection.getNameSpaceChildrenCache();
            jobGroupCache.getListenable().addListener((cf, jobGroupEvent) -> {
                switch (jobGroupEvent.getType()) {
                    case CHILD_ADDED:
                        PathChildrenCache logChildrenCache = logCuratorConnection.getLogChildrenCache(jobGroupEvent.getData().getPath());
                        logChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                            @Override
                            public void childEvent(CuratorFramework cf, PathChildrenCacheEvent event) throws Exception {
                                switch (event.getType()) {
                                    case CHILD_ADDED:
                                        String log = new String(event.getData().getData(), "UTF-8");
                                        saveLog(log);
                                        break;
                                    case CHILD_UPDATED:
                                        String log2 = new String(event.getData().getData(), "UTF-8");
                                        saveLog(log2);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });
                        break;
                    default:
                        break;
                }
            });

            for (ChildData child : jobGroupCache.getCurrentData()) {
                String jobGroup = child.getPath();
                PathChildrenCache logChildrenCache = logCuratorConnection.getLogChildrenCache(jobGroup);
                logChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework cf, PathChildrenCacheEvent event) throws Exception {
                        switch (event.getType()) {
                            case CHILD_ADDED:
                                String log = new String(event.getData().getData(), "UTF-8");
                                saveLog(log);
                                break;
                            case CHILD_UPDATED:
                                String log2 = new String(event.getData().getData(), "UTF-8");
                                saveLog(log2);
                                break;
                            default:
                                break;
                        }
                    }
                });

            }
        } catch (Exception e) {
            logger.error("记录任务执行日志异常 {}", LogExceptionStackTrace.erroStackTrace(e));
        }
    }

    private void saveLog(String log) {
        JSONObject jsonObject = JSONObject.parseObject(log);
        JSONObject jobConfig = jsonObject.getJSONObject("jobConfig");
        Boolean success = jsonObject.getBoolean("success");

        TaskLogPo logPo = new TaskLogPo();
        logPo.setGroupName(jobConfig.getString("groupName"));
        logPo.setJobName(jobConfig.getString("jobName"));
        logPo.setCurrentNodeId(jobConfig.getString("currentNodeId"));
        logPo.setErrorMsg(jobConfig.getString("errorMsg"));
        logPo.setLastFireTime(jobConfig.getDate("lastFireTime"));
        logPo.setLastEndTime(jobConfig.getDate("lastEndTime"));
        logPo.setSuccess(success ? 1: 0);

        taskLogMapper.saveTaskLog(logPo);
    }

}
