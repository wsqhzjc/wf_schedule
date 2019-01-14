package com.wf.schedule.monitor;

import com.wf.schedule.common.util.GfJsonUtil;
import com.wf.schedule.model.JobConfig;
import com.wf.schedule.monitor.zk.TaskCacheHandler;
import org.apache.curator.framework.recipes.cache.NodeCache;

public class NodeCacheHandler implements Runnable {

    private NodeCache taskNodeCache;

    private String threadName;
    private TaskCacheHandler taskCacheHandler;

    public NodeCacheHandler(String path, TaskCacheHandler taskCacheHandler) throws Exception {
        this.taskNodeCache = taskCacheHandler.getNodeCache(path);
        this.taskCacheHandler = taskCacheHandler;
    }

    @Override
    public void run() {
        taskNodeCache.getListenable().addListener(() -> {
            String data = new String(taskNodeCache.getCurrentData().getData(), "utf-8");
            JobConfig jobConfig = GfJsonUtil.parseObject(data, JobConfig.class);
            threadName = jobConfig.getGroupName() + jobConfig.getJobName();
            System.out.println(threadName + " " + data);
            if (!jobConfig.isResult()) {

            }
        });
    }
}
