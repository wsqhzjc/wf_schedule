package com.wf.schedule.log;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;

/**
 * @author chenlongfei
 * @date 2016-09-28
 * 日志输出到redis格式类
 */
public class LogCommand {

    private String logType;
    private Integer siteID;
    private String site;
    private String url;
    private Integer logLevel;
    private String ip;
    private String subject;
    private String content;
    private String appName;
    private Date dtNow;
    private String hostName;

    @JSONField(name="LogType")
    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }
    @JSONField(name="SiteID")
    public Integer getSiteID() {
        return siteID;
    }

    public void setSiteID(Integer siteID) {
        this.siteID = siteID;
    }
    @JSONField(name="Site")
    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }
    @JSONField(name="Url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    @JSONField(name="LogLevel")
    public Integer getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Integer logLevel) {
        this.logLevel = logLevel;
    }
    @JSONField(name="Ip")
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    @JSONField(name="Subject")
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
    @JSONField(name="Content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    @JSONField(name="AppName")
    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
    @JSONField(name="DtNow",format="yyyy-MM-dd HH:mm:ss")
    public Date getDtNow() {
        return dtNow;
    }

    public void setDtNow(Date dtNow) {
        this.dtNow = dtNow;
    }
    @JSONField(name="HostName")
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
