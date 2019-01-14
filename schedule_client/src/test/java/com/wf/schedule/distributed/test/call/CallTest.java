package com.wf.schedule.distributed.test.call;

import com.wf.schedule.common.context.ZkConstant;
import com.wf.schedule.common.util.GfJsonUtil;
import com.wf.schedule.common.zk.serializer.StringSerializer;
import com.wf.schedule.model.JobConfig;
import com.wf.schedule.model.JobGroupInfo;
import com.wf.schedule.monitor.MonitorCommond;
import com.wf.schedule.monitor.SchedulerMonitor;
import org.I0Itec.zkclient.ZkClient;
import org.junit.Test;

import java.util.List;

/**
 * Created by chenpengfei on 2017/9/14.
 */
public class CallTest {

    @Test
    public void test () {
        SchedulerMonitor schedulerMonitor = new SchedulerMonitor(ZkConstant.ZK_CONNECTION);
        List<JobGroupInfo> list = schedulerMonitor.getAllJobGroups();
        list.forEach(group-> {
            /**
             * if you can not get it
             * fuck you
             * to pdl
             *
             * fuck you too, bitch ! to cpf
             * **/
            if (group.getName().equals("ticket_task")) {
                /** 更新cron **/
                MonitorCommond monitorCmd = new MonitorCommond(MonitorCommond.TYPE_CRON_MOD, group.getName(), group.getJobs().get(0).getJobName(), "0/55 * * * * ?");
                schedulerMonitor.publishEvent(monitorCmd);

                /** 停止/恢复 任务 0:停止；1：恢复；**/
//            MonitorCommond stopCmd = new MonitorCommond(MonitorCommond.TYPE_STATUS_MOD, group.getName(), group.getJobs().get(0).getJobName(), "1");
//            schedulerMonitor.publishEvent(stopCmd);
                /** 立即执行 **/
//                MonitorCommond executeCmd = new MonitorCommond(MonitorCommond.TYPE_EXEC, group.getName(), group.getJobs().get(0).getJobName(), null);
//                schedulerMonitor.publishEvent(executeCmd);

                /** 修改额外参数（未完成）　**/
                MonitorCommond paramCmd = new MonitorCommond(MonitorCommond.TYPE_PARAM_MOD, group.getName(), group.getJobs().get(0).getJobName(), "我的修改");
                schedulerMonitor.publishEvent(paramCmd);

            }

        });
        /** add job
         * 1.增加zk节点
         * 2.订阅节点变更
         * 3.加载到job context
         * **/


    }

    @Test
    public void updateNode () {
        String path = String.format("%s%s/%s", ZkConstant.ROOT_NODE, "ticket_task", "com.wf.schedule.distributed.test.bean.DemoTask5");
        ZkClient zkClient = new ZkClient(ZkConstant.ZK_CONNECTION);
        zkClient.setZkSerializer(new StringSerializer());
        Object obj = zkClient.readData(path);
        JobConfig jobConfig = GfJsonUtil.parseObject(obj.toString(), JobConfig.class);
        jobConfig.setCurrentNodeId("192.168.211.1");
        zkClient.writeData(path, GfJsonUtil.toJSONString(jobConfig));
    }


}
