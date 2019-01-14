package com.wf.schedule.admin.zk;

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

    private CuratorFramework logRootNameCf;

    public CuratorFramework getRootNameSpaceCurator() {
        if (rootNameCf == null) {
            connectRootName();
        }
        return rootNameCf;
    }

    public CuratorFramework getLogRootNameCurator() {
        if (logRootNameCf == null) {
            connectLogRootName();
        }

        return logRootNameCf;
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

    private void connectLogRootName() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
        logRootNameCf = CuratorFrameworkFactory.builder()
                .connectString(zkServer)
                .sessionTimeoutMs(20000)
                .retryPolicy(retryPolicy)
                .namespace(ZkConstant.ROOT_LOG_NAME_SPACE)
                .build();
        logRootNameCf.start();
    }
}
