package com.wf.schedule.monitor.util;


import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class YunRongSmsUtil {
    private static final Logger logger = LoggerFactory.getLogger(YunRongSmsUtil.class);

    @Value("${yunrong.sms.verify.url}")
    private String url;
    @Value("${yunrong.sms.verify.un}")
    private String account;
    @Value("${yunrong.sms.verify.pw}")
    private String pswd;

    /*
     * params 填写的URL的参数 encode 字节编码
     */
    public String sendPostMessage(String url, List<NameValuePair> params) {
        HttpPost httpRequest = new HttpPost(url);
        String strResult = "";
        try {
            /* 添加请求参数到请求对象 */
            httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            /* 发送请求并等待响应 */
            HttpResponse httpResponse = new DefaultHttpClient()
                    .execute(httpRequest);
            /* 若状态码为200 ok */
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                /* 读返回数据 */
                strResult = EntityUtils.toString(httpResponse
                        .getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpRequest.releaseConnection();
        }
        return strResult;
    }

    /*
     * 批量发送同内容短信
     */
    private List<NameValuePair> sendBatchMessage(String account, String pswd, MessageParams messageParams) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("cmd", "sendBatchMessage"));
        params.add(new BasicNameValuePair("userName", account));
        params.add(new BasicNameValuePair("passWord", pswd));
        params.add(new BasicNameValuePair("mobilePhones", messageParams.getMobileList()));
        if (StringUtils.isNotBlank(messageParams.getTaskId())) {
            //批量发送的批次号，由批次发送方生成的uuid
            params.add(new BasicNameValuePair("clientMessageBatchId", messageParams.getTaskId()));
        }
        params.add(new BasicNameValuePair("body", messageParams.getBody()));
        return params;
    }

    /*
     * 批量发送不同内容短信
     */
    private List<NameValuePair> sendBatchMessageX() {

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("cmd", "sendBatchMessageX"));
        params.add(new BasicNameValuePair("userName", "ceshi"));
        params.add(new BasicNameValuePair("passWord", "888888"));
        params.add(new BasicNameValuePair("messageQty", "3"));

        params.add(new BasicNameValuePair("messageId1", "20160417000006"));
        params.add(new BasicNameValuePair("phoneNumber1", "18610809756"));
        params.add(new BasicNameValuePair("body1", "奔跑吧 少年"));

        params.add(new BasicNameValuePair("messageI2", "20160417000006"));
        params.add(new BasicNameValuePair("phoneNumber2", "18636632712"));
        params.add(new BasicNameValuePair("body2", "奔跑吧 少年"));

        params.add(new BasicNameValuePair("messageId3", "20160417000006"));
        params.add(new BasicNameValuePair("phoneNumber3", "18610809756"));
        params.add(new BasicNameValuePair("body3", "奔跑吧 少年"));

        return params;

    }

    public void batchSend(MessageParams messageParams) throws Exception {
        List<NameValuePair> params = sendBatchMessage(account, pswd, messageParams);

        String response = sendPostMessage(url, params);

        logger.info("response = {}", response);
        String resultCode = "";
        String errorCode = "";

        Document doc = DocumentHelper.parseText(response);
        Element root = doc.getRootElement();
        Element body = root.element("body");
        List<Element> fields = body.elements("field");
        for (Element field : fields) {
            String name = field.attributeValue("name");
            if ("resultCode".equals(name)) {
                resultCode = field.getText();
            } else if ("errorCode".equals(name)) {
                errorCode = field.getText();
            }
        }


        if (SmsContext.YUNRONG_SUCCESS.equals(resultCode)) {
        } else if (SmsContext.YUNRONG_ERROR_NULL_USER.equals(errorCode)) {
            messageParams.setFailReasion("用户名为空");
        } else if (SmsContext.YUNRONG_ERROR_PASSWD.equals(errorCode)) {
            messageParams.setFailReasion("用户名或口令错误!");
        } else if (SmsContext.YUNRONG_ERROR_IP.equals(errorCode)) {
            messageParams.setFailReasion("IP验证错误!");
        } else if (SmsContext.YUNRONG_ERROR_TIME_SEND_DATE.equals(errorCode)) {
            messageParams.setFailReasion("定时日期错误!");
        } else if (SmsContext.YUNRONG_ERROR_NO_MONEY.equals(errorCode)) {
            messageParams.setFailReasion("余额不足!");
        } else if (SmsContext.YUNRONG_ERROR_NULL_USER_ID.equals(errorCode)) {
            messageParams.setFailReasion("userId为空");
        } else if (SmsContext.YUNRONG_ERROR_NULL_PHONE_NUMBER.equals(errorCode)) {
            messageParams.setFailReasion("目标号码为空");
        } else if (SmsContext.YUNRONG_ERROR_ERROR_PHONE_NUMBER_COUNT.equals(errorCode)) {
            messageParams.setFailReasion("内容为空");
        } else if (SmsContext.YUNRONG_ERROR_SEND_LIMIT.equals(errorCode)) {
            messageParams.setFailReasion("群发手机号码大于200个或短信群发号码个数不能大于100条");
        } else if (SmsContext.YUNRONG_ERROR_ERROR_PHONE_NUMBER.equals(errorCode)) {
            messageParams.setFailReasion("目标号码错误");
        } else if (SmsContext.YUNRONG_ERROR_BLACKLIST.equals(errorCode)) {
            messageParams.setFailReasion("目标号码在黑名单中");
        } else if (SmsContext.YUNRONG_ERROR_BAD_WORDS.equals(errorCode)) {
            messageParams.setFailReasion("内容包含敏感单词");
        } else if (SmsContext.YUNRONG_ERROR_NO_SPECIAL_SERVER.equals(errorCode)) {
            messageParams.setFailReasion("特服号未分配 ");
        } else if (SmsContext.YUNRONG_ERROR_PRIVATE_CHANNEL.equals(errorCode)) {
            messageParams.setFailReasion("优先级错误(可以不传只进行发送)或分配通道错误");
        } else if (SmsContext.YUNRONG_ERROR_OTHER.equals(errorCode)) {
            messageParams.setFailReasion("其他异常");
        } else if (SmsContext.YUNRONG_ERROR_NULL_OTHER.equals(errorCode)) {
            messageParams.setFailReasion("其它异常，短信内容可能为空");
        }
    }


}
