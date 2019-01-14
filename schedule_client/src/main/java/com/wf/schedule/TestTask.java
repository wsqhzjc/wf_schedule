package com.wf.schedule;

public class TestTask {

    public void test(String aa) throws InterruptedException {
        System.out.println("=========task parm ======");
        System.out.println("========" + aa + "========");
        System.out.println("=========task start=======");
        System.out.println("=========test task========");
        System.out.println("=========         ========");
        System.out.println("=========         ========");
        System.out.println("=========         ========");
        System.out.println("=========         ========");
        Thread.sleep(5000);
        System.out.println("=========task stop========");
    }
}
