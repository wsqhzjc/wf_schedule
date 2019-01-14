package com.wf.schedule.normal.test;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by chenpengfei on 2017/9/15.
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration("classpath:spring-quartz.xml")
public class QuartzTest {
    @Test
    public void test() throws InterruptedException {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-quartz.xml");
        Thread.sleep(Long.MAX_VALUE);
    }

}
