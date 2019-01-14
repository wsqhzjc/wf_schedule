package com.wf.schedule.monitor.zk;

import com.wf.schedule.common.context.ZkConstant;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CuratorConnection {

    @Value("${zk.server}")
    private String zkServer;

    private CuratorFramework rootNameCf;

    public CuratorFramework getRootNameSpaceCurator() {
        if (rootNameCf == null) connectRootName();
        return rootNameCf;
    }

    private void connectRootName() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
        rootNameCf = CuratorFrameworkFactory.builder()
                .connectString(zkServer)
                .sessionTimeoutMs(20000)
                .retryPolicy(retryPolicy)
                .namespace(ZkConstant.ROOT_NAME_SPACE)
                .build();
        rootNameCf.start();
    }

}
