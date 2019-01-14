package com.wf.schedule.core.handler.bean;

import com.wf.schedule.common.spring.InstanceFactory;
import com.wf.schedule.core.handler.JobLogPersistHandler;
import com.wf.schedule.core.handler.impl.ZkJobLogPersistHandler;
import org.I0Itec.zkclient.ZkClient;

public class ZkJobPersistBean {

    static class SingletonHolder {
        static JobLogPersistHandler instance = new ZkJobLogPersistHandler(InstanceFactory.getInstance(ZkClient.class, "zkClient"));
    }

    public static JobLogPersistHandler getInstance() {
        return SingletonHolder.instance;
    }
}
