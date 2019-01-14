package com.wf.schedule.distributed.test;

import com.wf.schedule.monitor.MonitorCommond;
import com.wf.schedule.monitor.SchedulerMonitor;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by chenpengfei on 2017/9/13.
 */
public class MainTest {

    @Test
    public void  test () throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("test-schduler.xml");
        Thread.sleep(Long.MAX_VALUE);
    }

    private void executeCommand () throws InterruptedException {
        Thread.sleep(1000);
        SchedulerMonitor schedulerMonitor = new SchedulerMonitor();
//        JobContext.getContext().addJob(new AbstractJob() {
//            @Override
//            public boolean parallelEnabled() {
//                return false;
//            }
//
//            @Override
//            public void doJob(JobContext context) throws Exception {
//                System.out.println("add task.........................");
//            }
//        });
        MonitorCommond executeCmd = new MonitorCommond(MonitorCommond.TYPE_STATUS_MOD, "new_push_task", "task2", null);
        schedulerMonitor.publishEvent(executeCmd);
    }


    private Properties  getPropertie(String path) {
        Properties prop = new Properties();
        InputStream in = MainTest.class.getClassLoader().getResourceAsStream(path);
        try {
            prop.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }


}
