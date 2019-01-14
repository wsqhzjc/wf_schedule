package com.wf.schedule.monitor;

import com.wf.schedule.monitor.util.EmailModel;
import com.wf.schedule.monitor.util.MessageParams;
import com.wf.schedule.monitor.util.SmsContext;
import com.wf.schedule.monitor.zk.TaskCacheHandler;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class NodeGroupCacheHandler implements Runnable {
    private Logger logger = LoggerFactory.getLogger(NodeGroupCacheHandler.class);
    private PathChildrenCache pathChildrenCache;

    private TaskCacheHandler taskCacheHandler;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String threadName;

    public NodeGroupCacheHandler(String path, TaskCacheHandler taskCacheHandler) throws Exception {
        this.pathChildrenCache = taskCacheHandler.getChildrenCache(path);
        this.taskCacheHandler = taskCacheHandler;
    }

    @Override
    public void run() {
        pathChildrenCache.getListenable().addListener((cf, event) -> {
            threadName = event.getData().getPath();
            String[] split = threadName.split("/");
            switch (event.getType()) {
                case CHILD_ADDED:
                    logger.info("运行节点 {} 上线", threadName);
                    break;
                case CHILD_REMOVED:
                    logger.info("运行节点 {} 下线", threadName);

                    //短信通知
                    String msg = String.format(SmsContext.SMS_TEMPLATE, split[1], split[3], dateFormat.format(new Date()));
                    MessageParams messageParams = new MessageParams();
                    messageParams.setMobileList(taskCacheHandler.getMobileList());
                    messageParams.setTaskId(UUID.randomUUID().toString());
                    messageParams.setBody(msg);
                    taskCacheHandler.getMessageSender().send(messageParams);

                    //E-mail 通知
                    String[] mailList = taskCacheHandler.getMailList().split(",");
                    for (String mail : mailList) {
                        EmailModel emailModel = new EmailModel(mail, "", "【任务调度中心系统掉线通知】", msg);
                        taskCacheHandler.getMessageSender().sendEmal(emailModel);
                    }

                    taskCacheHandler.clearCache();
                    break;
                default:
                    break;
            }


            List<String> nodeList = cf.getChildren().forPath(String.format("/%s/%s", split[1], split[2]));
            if(nodeList.size() == 0) {
                logger.info("任务{}无可运行节点", split[1]);
            } else {
                logger.info("任务{}可运行节点{}", split[1], nodeList);
            }
        });
    }
}
