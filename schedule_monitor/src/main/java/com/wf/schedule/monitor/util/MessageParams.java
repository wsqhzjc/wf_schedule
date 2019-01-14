package com.wf.schedule.monitor.util;

import java.util.Date;
import java.util.List;


/**
 * 发送短信参数
 *
 * @author pdl
 * @date 2016-12-12 11:07
 **/
public class MessageParams {
    //    SiteID：站点ID，目前没有有效使用，默认传0或者1；
//    Type：暂无明确用处，传0或1都可以；
//    From：来源，暂无明确用处，传空即可；
//    mobile：接收短信的手机号码，若有多个，中间用英文逗号隔开；
//    Body：短信内容，传过来的短信内容不用带签名或其他内容；
//    UserID：用户ID，若有指定用户ID可传，若无可传0
//    SMSType：短信类型：1-通知短信，譬如验证码，2-营销短信，譬如广告或活动短信

    private String systemCode;
    private Long siteId = 0L;
    private Integer type = 0;
    private Integer from;
    private String mobileList;
    private String body;
    private String userIds;
    private Integer SMSType;
    private Date createTime;
    private String failReasion;
    private List<Long> SmsMessageIdList;
    //短信批次号，批量发送时可以生产一个唯一的uuid
    private String taskId;
    //状态报告成功（成功到达手机）数量
    private Integer deliverSuccessQty = 0;

    public String getSystemCode() {
        return systemCode;
    }

    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public String getMobileList() {
        return mobileList;
    }

    public void setMobileList(String mobileList) {
        this.mobileList = mobileList;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUserIds() {
        return userIds;
    }

    public void setUserIds(String userIds) {
        this.userIds = userIds;
    }

    public Integer getSMSType() {
        return SMSType;
    }

    public void setSMSType(Integer SMSType) {
        this.SMSType = SMSType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getFailReasion() {
        return failReasion;
    }

    public void setFailReasion(String failReasion) {
        this.failReasion = failReasion;
    }

    public List<Long> getSmsMessageIdList() {
        return SmsMessageIdList;
    }

    public void setSmsMessageIdList(List<Long> smsMessageIdList) {
        SmsMessageIdList = smsMessageIdList;
    }

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
}
