package com.wf.schedule.distributed.test;

import com.wf.schedule.common.context.ZkConstant;
import com.wf.schedule.common.zk.serializer.StringSerializer;
import com.wf.schedule.model.JobConfig;
import org.I0Itec.zkclient.ZkClient;
import org.junit.Test;

import java.util.List;

/**
 * Created by chenpengfei on 2017/9/18.
 */
public class AddTaskTest {

    private String jobClass = "DemoTask2";
    private String jobMethod = "print";

    private String group = "ticket_task";

    @Test
    public void addNEWTask2ZK () throws Exception {
        ZkClient zkClient = new ZkClient(ZkConstant.ZK_CONNECTION);
        zkClient.setZkSerializer(new StringSerializer());
        JobConfig jobConfg = new JobConfig(group, jobClass, jobMethod,"0/10 * * * * ?", "测试demo1", "aa");

        String nodePath = ZkConstant.ROOT_NODE+group+ "/" +ZkConstant.SERVER_NODES;
        if (zkClient.exists(nodePath)) {
            List<String> nodeLlist =  zkClient.getChildren(ZkConstant.ROOT_NODE+group+ "/" +ZkConstant.SERVER_NODES);
            if (nodeLlist != null && nodeLlist.size() > 0) {
                jobConfg.setCurrentNodeId(nodeLlist.get(0));
            }
        }

    }

    @Test
    public void conn () {
        ZkClient zkClient = new ZkClient(ZkConstant.ZK_CONNECTION, 3000, 3000);
        zkClient.getChildren("/disconf");
    }
}
