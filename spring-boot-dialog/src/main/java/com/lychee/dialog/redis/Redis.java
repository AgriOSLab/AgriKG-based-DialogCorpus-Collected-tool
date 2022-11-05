package com.lychee.dialog.redis;

import com.lychee.dialog.myBatis.EntityMapper;
import com.lychee.dialog.redis.Configs.RedisProperties;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.Set;

@Component
public class Redis extends Jedis {
    private static Date time = new Date();

    @Autowired
    EntityMapper entityMapper;

    @Autowired
    RedisProperties redisProperties;

    private JedisPool jedisPool;

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public Redis(){
//        this("127.0.0.1", 6379);
    }

    @PostConstruct
    public void initRedis(){
        jedisPool = new JedisPool(new GenericObjectPoolConfig<>(), redisProperties.getHost(), redisProperties.getPort(), 5000, redisProperties.getPassword());
        Jedis jedis = jedisPool.getResource();
        setDefaultkeys(jedis);
        resetAllUserState(jedis);
    }

    /**
     * 清空所有在线用户登陆状态
     */
    public void resetAllUserState(Jedis jedis){
        Set<String> keys = jedis.keys("dialog.user.login.*");
        for (String key : keys) {
            this.del(key);
        }
    }
    /**
     * 清空所有在线用户登陆状态
     */
    public void resetAllUserState(){
        Jedis jedis = jedisPool.getResource();
        Set<String> keys = this.keys("dialog.user.login.*");
        for (String key : keys) {
            this.del(key);
        }
    }
    /**
     *
     */
    private void setDefaultkeys(){
        /**
         * 初次部署 Redis 时自动添加初始化id
         */
        Jedis jedis = jedisPool.getResource();
        jedis.setnx("dialog.entity.id", "1");
    }
    /**
     *
     */
    private void setDefaultkeys(Jedis jedis){
        /**
         * 初次部署 Redis 时自动添加初始化id
         */
        jedis.setnx("dialog.entity.id", "1");
    }

    /**
     * 获取Redis中保存的记录id
     * @return
     */
    public int getEntityId(){
        Jedis jedis = jedisPool.getResource();
        long end = time.getTime() + 500;
        while (time.getTime()<end) {
            if (acquire_lock("dialog.entity.id.lock")) {
                int entityId = Integer.parseInt(this.get("dialog.entity.id"));
                jedis.incr("dialog.entity.id");
                release_lock("dialog.entity.id.lock");
                return entityId;
            }
        }
        return 0;
    }

    public int getEntityId(Jedis jedis){
        long end = time.getTime() + 500;
        while (time.getTime()<end) {
            if (acquire_lock("dialog.entity.id.lock")) {
                int entityId = Integer.parseInt(this.get("dialog.entity.id"));
                this.incr("dialog.entity.id");
                release_lock("dialog.entity.id.lock");
                return entityId;
            }
        }
        return 0;
    }

    /**
     * 获取需要访问实体的id
     * 即从暂时被淘汰列表中的记录id和 dialog.entity.id选择
     * @param username
     * @param isChanged
     * @param changedId
     * @return
     */
    public int getNewEntityId(String username, boolean isChanged, int changedId, boolean isKill){
        /**
         * 判断是否是用户点击“换一个”请求
         * 将该用户点击“换一个”次数+1
         * 将被换掉的记录的被换掉次数+1
         * 将被换掉的ID压入列表
         */
        Jedis jedis = jedisPool.getResource();
        if (isChanged) {
            this.incr("dialog.user."+username+".changetimes");
            this.incr("dialog.entity.temporary." + changedId);
            if (Integer.parseInt(this.get("dialog.entity.temporary." + changedId))==1)
                this.rpush("dialog.entity.temporary", String.valueOf(changedId));
        }

        if (isKill){
            this.del("dialog.entity.temporary." + changedId);
            this.lrem("dialog.entity.temporary", 0,  String.valueOf(changedId));
            entityMapper.setEntityRejected(changedId);
        }

        int userChangeTimes = 0;
        if(this.exists("dialog.user."+username+".changetimes"))
            userChangeTimes = Integer.parseInt(this.get("dialog.user."+username+".changetimes"));
        else
            userLogin(username);

        long changedListLength = this.llen("dialog.entity.temporary");

        if (changedListLength==0||userChangeTimes>5||userChangeTimes>0.5*changedListLength){
            return getEntityId();
        }
        else{
            /**
             * 判断列表中弹出的记录id已被替换的次数
             * 被替换次数过多，则直接抛弃
             */
            String tempId = null;
            for (int i=0;i<changedListLength+1;i++){
                tempId = this.lpop("dialog.entity.temporary");

                if (tempId==null)
                    break;

                int id_times = Integer.parseInt(this.get("dialog.entity.temporary." + tempId));

                if (id_times>=4){
                    entityMapper.setEntityRejected(Integer.parseInt(tempId));
                    this.del("dialog.entity.temporary." + tempId);
                }
                else{
                    break;
                }
            }
            if (tempId==null)
                return getEntityId();

            return Integer.parseInt(tempId);
        }
    }
    /**
     * 获得指定的锁
     * @param lockName
     * @return
     */
    public boolean acquire_lock(String lockName)  {

        long end = time.getTime() + 500;
        
        try {
            while (time.getTime() < end) {
                if (this.setnx(lockName, "1") == 1) {
                    this.expire(lockName, 1);
                    return true;
                }
                Thread.sleep(1);
            }
        }
        catch (Exception e){
            return false;
        }

        return false;
    }

    public void release_lock(String lockName){
        this.del(lockName);
    }

    /**
     * 用户登录，记录用户相关信息
     * 1. 用户名，过期时间 dialog.user.login.username
     * 2. 用户点击换一个次数 dialog.user.username.changetimes
     * @param username
     */
    public void userLogin(String username){
        /**
         * 用户名及其过期时间
         */
        this.setnx("dialog.user.login."+username, "1");
        this.expire("dialog.user.login."+username, 24*60*60);
        /**
         * 设置切换次数及其过期时间
         */
        this.setnx("dialog.user."+username+".changetimes", "0");
        this.expire("dialog.user."+username+".changetimes", 24*60*60);
    }

    /**
     * 存储被用户舍弃的记录id
     * @param id
     */
    public void restoreTempRecordId(int id, Jedis jedis){
        long end = time.getTime() + 500;
        while (time.getTime()<end) {
            if (acquire_lock("dialog.entity.id.lock")) {
                int entityId = Integer.parseInt(jedis.get("dialog.entity.id"));

                if (id<entityId){
                    jedis.rpush("dialog.entity.temporary", String.valueOf(id));
                    jedis.setnx("dialog.entity.temporary." + id, "0");
                }
                release_lock("dialog.entity.id.lock");
                return;
            }
        }
    }

    /**
     * 删除指定键
     * @param key
     * @return
     */
    public boolean delUser(String key, int record_id){
        Jedis jedis = jedisPool.getResource();
        restoreTempRecordId(record_id, jedis);
        return jedis.del("dialog.user.login."+key)==0?false:true;
    }

}
