package com.wf.schedule.monitor.util;

/**
 * Created by jdd on 2016/10/13.
 */
public class SmsContext {

    public static String SMS_TEMPLATE = "系统掉线通知\n系统[%s]\n告警主机:%s\n告警时间:%s\n告警信息:系统掉线";

    public static String YUNRONG_SUCCESS = "0"; //提交成功
    public static String YUNRONG_ERROR_NULL_USER = "-9"; //用户名为空（可能提交的请求可是有误，系统这次GET和POST两种请求）
    public static String YUNRONG_ERROR_PASSWD = "-1"; //用户名或口令错误
    public static String YUNRONG_ERROR_IP = "-2"; //IP验证错误 
    public static String YUNRONG_ERROR_TIME_SEND_DATE = "-3"; //定时日期错误
    public static String YUNRONG_ERROR_NO_MONEY = "-10"; //余额不足
    public static String YUNRONG_ERROR_NULL_USER_ID = "-101"; //userId为空
    public static String YUNRONG_ERROR_NULL_PHONE_NUMBER = "-102"; //目标号码为空
    public static String YUNRONG_ERROR_ERROR_PHONE_NUMBER_COUNT = "-103"; //内容为空
    public static String YUNRONG_ERROR_SEND_LIMIT = "-104"; //群发手机号码大于200个或短信群发号码个数不能大于100条
    public static String YUNRONG_ERROR_ERROR_PHONE_NUMBER = "200"; //目标号码错误
    public static String YUNRONG_ERROR_BLACKLIST = "201"; //目标号码在黑名单中 
    public static String YUNRONG_ERROR_BAD_WORDS = "202"; //内容包含敏感单词
    public static String YUNRONG_ERROR_NO_SPECIAL_SERVER = "203"; //特服号未分配 
    public static String YUNRONG_ERROR_PRIVATE_CHANNEL = "204"; //优先级错误(可以不传只进行发送)或分配通道错误
    public static String YUNRONG_ERROR_OTHER = "999"; //其他异常
    public static String YUNRONG_ERROR_NULL_OTHER = "-999"; //其它异常，短信内容可能为空
}
