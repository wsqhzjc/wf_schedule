package com.wf.schedule.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.wf.schedule.common.util.GfJsonUtil;
import com.wf.schedule.common.util.IpUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

import java.sql.Timestamp;

public class Log4RedisAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	JedisPool pool;

	String host = "localhost";
	int port = Protocol.DEFAULT_PORT;
	String key = null;
	int timeout = Protocol.DEFAULT_TIMEOUT;
	String password = null;
	int database = Protocol.DEFAULT_DATABASE;
    String appName = "";

	@Override
	protected void append(ILoggingEvent event) {
		Jedis client = pool.getResource();
		try {
            LogCommand lc = new LogCommand();
            String ip = IpUtils.getIp();
            String hostName = IpUtils.getHostName();
            lc.setIp(ip);
            lc.setAppName(appName);
            lc.setContent(event.getFormattedMessage());
            Timestamp ts = new Timestamp(event.getTimeStamp());
            lc.setDtNow(ts);
            lc.setHostName(hostName);
            lc.setLogType(appName);
            lc.setSite("");
            lc.setSiteID(0);
            lc.setSubject(event.getLoggerName());
            lc.setUrl("http://");
            int levelLog = event.getLevel().toInt();
            switch (levelLog) {
                case 10000:
                    lc.setLogLevel(LogLevel.Debug);
                    break;
                case 20000:
                    lc.setLogLevel(LogLevel.Info);
                    break;
                case 30000:
                    lc.setLogLevel(LogLevel.Warn);
                    break;
                case 40000:
                    lc.setLogLevel(LogLevel.Error);
                    break;
                case 50000:
                    lc.setLogLevel(LogLevel.Fatal);
                    break;
                default:
                    break;
            }
			client.rpush(key, GfJsonUtil.toJSONString(lc));
		} catch (Exception e) {
			e.printStackTrace();
			pool.close();
			client = null;
		} finally {
			if (client != null) {
				pool.close();
			}
		}
	}


	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getDatabase() {
		return database;
	}

	public void setDatabase(int database) {
		this.database = database;
	}

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

	@Override
	public void start() {
		super.start();
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setTestOnBorrow(true);
		pool = new JedisPool(config, host, port, timeout, password, database);
	}

	@Override
	public void stop() {
		super.stop();
		pool.destroy();
	}

}
