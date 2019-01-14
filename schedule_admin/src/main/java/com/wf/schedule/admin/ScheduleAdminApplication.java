package com.wf.schedule.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by pdl on 2017/6/27.
 */
@SpringBootApplication
@MapperScan("com.wf.schedule.admin.mapper")
public class ScheduleAdminApplication {
    public static void main(String[] args) {

        SpringApplication.run(ScheduleAdminApplication.class, args);

    }
}
