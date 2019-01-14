package com.wf.schedule.monitor.util;

import java.io.Serializable;

/**
 * Created by jdd on 2017/8/22.
 */
public class YunRongMsgResponse implements Serializable {
    //分批发送id,uuid唯一
    String taskId;
    //状态报告成功（成功到达手机）数量
    Integer deliverSuccessQty = 0;
    //返回状态值
    String resultCode;
    //错误码
    String error;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Integer getDeliverSuccessQty() {
        return deliverSuccessQty;
    }

    public void setDeliverSuccessQty(Integer deliverSuccessQty) {
        this.deliverSuccessQty = deliverSuccessQty;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
