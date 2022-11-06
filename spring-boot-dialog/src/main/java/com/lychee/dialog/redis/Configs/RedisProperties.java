package com.lychee.dialog.redis.Configs;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

@Component
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
/**
 * 读取配置文件，获取相关Redis配置信息
 */
public class RedisProperties {

    private String host;
    private int port;
    private String password;
    private GenericObjectPoolConfig objectGenericObjectPoolConfig;
    public GenericObjectPoolConfig<Jedis> getObjectGenericObjectPoolConfig() {
        return objectGenericObjectPoolConfig;
    }

    public void setObjectGenericObjectPoolConfig(GenericObjectPoolConfig<Object> objectGenericObjectPoolConfig) {
        this.objectGenericObjectPoolConfig = objectGenericObjectPoolConfig;
    }

    public RedisProperties() {
        objectGenericObjectPoolConfig = new GenericObjectPoolConfig<>();
        objectGenericObjectPoolConfig.setMaxIdle(20);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

}
