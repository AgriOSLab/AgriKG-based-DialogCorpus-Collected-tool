package com.lychee.dialog.redis.Configs;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;

@Component
public class JedisPoolConfig extends JedisPool {

    public JedisPoolConfig(@Autowired RedisProperties redisProperties) {
        super(redisProperties.getObjectGenericObjectPoolConfig(), redisProperties.getHost(), redisProperties.getPort(), 5000, redisProperties.getPassword());
    }

}
