package com.wf.schedule.common.util;

import com.wf.schedule.log.LogExceptionStackTrace;
import com.wf.schedule.model.JobConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;


/**
 * @author chenlongfei
 */
public class TaskUtils {
    public final static Logger log = LoggerFactory.getLogger(TaskUtils.class);

    /**
     * 通过反射调用scheduleJob中定义的方法
     *
     * @param scheduleJob
     */
    public static boolean invokMethod(JobConfig scheduleJob) {
        scheduleJob.setLastFireTime(new Date());
        Object object = null;
        Class clazz = null;
        if (StringUtils.isNotBlank(scheduleJob.getJobName())) {
            try {
                clazz = Class.forName(scheduleJob.getJobName());
                object = clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                scheduleJob.setErrorMsg(LogExceptionStackTrace.erroStackTrace(e).toString());
                scheduleJob.setLastEndTime(new Date());
                scheduleJob.setResult(false);
                return false;
            }

        }
        if (object == null) {
            scheduleJob.setErrorMsg("未启动成功，请检查BeanClass是否配置正确！！！");
            scheduleJob.setLastEndTime(new Date());
            scheduleJob.setResult(false);
            log.error("任务名称 = [" + scheduleJob.getJobName() + "]---------------未启动成功，请检查是否配置正确！！！");
            return false;
        }
        clazz = object.getClass();
        Method method = null;
        try {
            if (StringUtils.isBlank(scheduleJob.getExtraData())) {
                method = clazz.getDeclaredMethod(scheduleJob.getJobMethod());
            } else {
                method = clazz.getDeclaredMethod(scheduleJob.getJobMethod(), String.class);
            }

        } catch (NoSuchMethodException e) {
            scheduleJob.setErrorMsg("未启动成功，方法名设置错误！！！");
            scheduleJob.setLastEndTime(new Date());
            scheduleJob.setResult(false);
            log.error("任务名称 = [" + scheduleJob.getJobName() + "]---------------未启动成功，方法名设置错误！！！");
            return false;
        } catch (SecurityException e) {
            scheduleJob.setErrorMsg(e.getMessage());
            scheduleJob.setLastEndTime(new Date());
            scheduleJob.setResult(false);
            e.printStackTrace();
            return false;
        }
        if (method != null) {
            try {
                if (StringUtils.isBlank(scheduleJob.getExtraData())) {
                    method.invoke(object);
                } else {
                    method.invoke(object, new Object[]{scheduleJob.getExtraData()});
                }
                scheduleJob.setLastEndTime(new Date());
                scheduleJob.setResult(true);
            } catch (IllegalAccessException e) {
                log.error("任务名称 = [" + scheduleJob.getJobName() + "]--------------执行失败 ex={}", ExceptionUtils.getStackTrace(e));
                scheduleJob.setErrorMsg(LogExceptionStackTrace.erroStackTrace(e).toString());
                scheduleJob.setLastEndTime(new Date());
                scheduleJob.setResult(false);
                return false;
            } catch (IllegalArgumentException e) {
                log.error("任务名称 = [" + scheduleJob.getJobName() + "]---------------执行失败 ex={}", ExceptionUtils.getStackTrace(e));
                scheduleJob.setErrorMsg(LogExceptionStackTrace.erroStackTrace(e).toString());
                scheduleJob.setLastEndTime(new Date());
                scheduleJob.setResult(false);
                return false;
            } catch (InvocationTargetException e) {
                log.error("任务名称 = [" + scheduleJob.getJobName() + "]---------------执行失败 ex={}", ExceptionUtils.getStackTrace(e));
                scheduleJob.setErrorMsg(LogExceptionStackTrace.erroStackTrace(e).toString());
                scheduleJob.setLastEndTime(new Date());
                scheduleJob.setResult(false);
                return false;
            } catch (Exception e) {
                log.error("任务名称 = [" + scheduleJob.getJobName() + "]---------------执行失败 ex={}", ExceptionUtils.getStackTrace(e));
                scheduleJob.setErrorMsg(LogExceptionStackTrace.erroStackTrace(e).toString());
                scheduleJob.setLastEndTime(new Date());
                scheduleJob.setResult(false);
                return false;
            }
        }
        return true;
    }

}
