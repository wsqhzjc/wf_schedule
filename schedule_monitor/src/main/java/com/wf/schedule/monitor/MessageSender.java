package com.wf.schedule.monitor;


import com.wf.schedule.monitor.util.EmailModel;
import com.wf.schedule.monitor.util.EmailUtils;
import com.wf.schedule.monitor.util.MessageParams;
import com.wf.schedule.monitor.util.YunRongSmsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageSender {

    @Autowired
    private YunRongSmsUtil yunRongSmsUtil;
    @Autowired
    private EmailUtils emailUtils;

    private final Logger logger = LoggerFactory.getLogger(MessageSender.class);


    public void send(MessageParams messageParams) throws Exception {
        yunRongSmsUtil.batchSend(messageParams);
    }

    public void sendEmal(EmailModel emailModel) {
        emailUtils.sendEmail(emailModel);
    }

}