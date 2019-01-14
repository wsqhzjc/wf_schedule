package com.wf.schedule.monitor.util;

/**
 * @author pdl
 * @date 2016/11/10
 * @Description
 **/

public class EmailModel {
    //发给谁
    private String toEmail;
    //谁发的
    private String fromEmail;
    //主题
    private String subject;
    //内容
    private String text;

    public EmailModel(String toEmail, String fromEmail, String subject, String text) {
        this.toEmail = toEmail;
        this.fromEmail = fromEmail;
        this.subject = subject;
        this.text = text;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
