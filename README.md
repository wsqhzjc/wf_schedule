分布式调度系统
====

## springboot 问题
- 集成springboot admin  
heal-url问题：在不加后缀时无法访问；
client端指定health_url可解决

- springboot admin status down
集成springboot admin后一直线上down；
查看details发现 redis 有一次；
在pom文件中引用了redis-start但是未配置redis相关信息导致；

- spring-cache 问题  
spring-cache 以来了spring-context 
 
client 接入指南
---------------
 
 * `去除原有montior_task_sdk的包引用及prop-monitor-task-config.properties, TaskClientInit的注册`
 
 * `prop-common-config.properties文件增加配置：task_group=xxx `
 
 * `app-spring-config-xxx.xml添加一行`
 ~~~
 <import resource="classpath:schedule/app-scheduler.xml"/>
 ~~~
 * `http://schedule.jdd.com上对应分组下添加任务`