package com.wf.schedule.monitor;

import com.wf.schedule.common.context.ZkConstant;
import com.wf.schedule.log.LogExceptionStackTrace;
import com.wf.schedule.monitor.zk.TaskCacheHandler;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class Init implements ApplicationListener<ContextRefreshedEvent> {
    Logger logger = LoggerFactory.getLogger(Init.class);
    public static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(30);
    private TaskCacheHandler taskCacheHandler;
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        taskCacheHandler = event.getApplicationContext().getBean(TaskCacheHandler.class);
        try {
            logger.info("----listener job and node start ----");
            // 监控taskGroup
            fixedThreadPool.execute(new TaskGroupCacheHandler("/", taskCacheHandler));

            PathChildrenCache groupCache = taskCacheHandler.getNameSpaceChildrenCache();
            for (ChildData jobGroupData : groupCache.getCurrentData()) {
                PathChildrenCache taskCache = taskCacheHandler.getChildrenCache(jobGroupData.getPath());
                List<ChildData> nodes = taskCache.getCurrentData();
                for (ChildData node : nodes) {
                    if (node.getPath().lastIndexOf(ZkConstant.SERVER_NODES) > 0) {
                        List<String> nodeList = taskCacheHandler.getCuratorConnection().getRootNameSpaceCurator().getChildren().forPath(node.getPath());
                        logger.info("开始监听 {} 节点: {}", node.getPath(), nodeList);
                        fixedThreadPool.execute(new NodeGroupCacheHandler(node.getPath(), taskCacheHandler));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("记录任务执行日志异常 {}", LogExceptionStackTrace.erroStackTrace(e));
        }
    }

}
