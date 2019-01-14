package com.wf.schedule.monitor;

import com.wf.schedule.common.context.ZkConstant;
import com.wf.schedule.monitor.zk.TaskCacheHandler;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskGroupCacheHandler implements Runnable {
    private Logger logger = LoggerFactory.getLogger(TaskGroupCacheHandler.class);
    private PathChildrenCache pathChildrenCache;

    private TaskCacheHandler taskCacheHandler;

    private String threadName;

    public TaskGroupCacheHandler(String path, TaskCacheHandler taskCacheHandler) throws Exception {
        this.pathChildrenCache = taskCacheHandler.getChildrenCache(path);
        this.taskCacheHandler = taskCacheHandler;
    }

    @Override
    public void run() {
        pathChildrenCache.getListenable().addListener((cf, event) -> {
            threadName = event.getData().getPath();
            String p = event.getData().getPath();
            switch (event.getType()) {
                case CHILD_ADDED:
                    logger.info( "节点{}新增", p);
                    Stat stat = cf.checkExists().forPath(String.format("%s/%s", p, ZkConstant.SERVER_NODES));
                    if(stat == null) {
                        cf.create().forPath(String.format("%s/%s", p, ZkConstant.SERVER_NODES));
                    }
                    Init.fixedThreadPool.execute(new NodeGroupCacheHandler(p, taskCacheHandler));
                    break;
                case CHILD_REMOVED:
                    logger.info( "节点{}删除", p);
                    taskCacheHandler.clearCache();
                    break;
                default:
                    break;
            }
        });
    }
}
