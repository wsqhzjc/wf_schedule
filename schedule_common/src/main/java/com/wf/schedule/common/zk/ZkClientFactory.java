package com.wf.schedule.common.zk;

import com.wf.schedule.common.zk.serializer.StringSerializer;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

/**
 * Created by chenpengfei on 2017/9/19.
 */
public class ZkClientFactory implements FactoryBean<ZkClient>, DisposableBean {

    private String connectionString;

    private ZkClient zkClient;

    private final int connectionTimeOut = 10*1000;

    private final int sessionTimeout = 3*1000;

    private final int operationRetryTimeout = 3*1000;


    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    @Override
    public void destroy() throws Exception {
        if (zkClient != null) {zkClient.close();}
    }

    public void init() throws Exception {
        ZkConnection zkConnection = new ZkConnection(connectionString);
        if (zkClient == null) {
//            zkClient = new ZkClient(connectionString, connectionTimeOut, sessionTimeout);
//            zkClient.setZkSerializer(new StringSerializer());
            zkClient = new ZkClient(zkConnection, connectionTimeOut, new StringSerializer(), operationRetryTimeout);
        }
    }

    @Override
    public ZkClient getObject() throws Exception {
        return zkClient;
    }

    @Override
    public Class<?> getObjectType() {
        return (this.zkClient != null ? this.zkClient.getClass() : ZkClient.class);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
