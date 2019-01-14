package com.wf.schedule.monitor.util;

import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author pdl
 * @date 2016/11/10
 * @Description
 **/

@Component
public class EmailUtils {
    private static Logger logger = LoggerFactory.getLogger(EmailUtils.class);
    @Value("${smtp.host}")
    private String smtpHost;
    @Value("${smtp.username}")
    private String smtpUsername;
    @Value("${smtp.password}")
    private String smtpPassword;
    @Value("${smtp.frommail}")
    private String frommail;

    public void sendEmail(EmailModel mail) {
        try {
            HtmlEmail email = new HtmlEmail();
            email.setHostName(smtpHost);
            email.setAuthentication(smtpUsername, smtpPassword);
            email.addTo(mail.getToEmail());
            email.setFrom(frommail);
            email.setCharset("UTF-8");
            email.setSubject(mail.getSubject());
            email.setHtmlMsg(mail.getText());
            email.send();
        } catch (Exception e) {
            logger.error("发送邮件异常!! StackTrace = {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
