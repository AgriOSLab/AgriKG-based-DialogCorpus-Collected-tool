package com.lychee.dialog.redis;

import com.lychee.dialog.myBatis.EntityMapper;
import com.lychee.dialog.redis.Configs.JedisPoolConfig;
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
public class Redis{
    private static Date time = new Date();

    @Autowired
    EntityMapper entityMapper;

    @Autowired
    JedisPoolConfig jedisPool;


    @PostConstruct
    public void init(){
        setDefaultkeys();
        resetAllUserState();
    }

    /**
     * 清空所有在线用户登陆状态
     */
    public void resetAllUserState(Jedis jedis){
        Set<String> keys = jedis.keys("dialog.user.login.*");
        for (String key : keys) {
            jedis.del(key);
        }
    }
    /**
     * 清空所有在线用户登陆状态
     */
    public void resetAllUserState(){
        Jedis jedis = jedisPool.getResource();
        resetAllUserState(jedis);
        jedis.close();
    }
    /**
     *
     */
    private void setDefaultkeys(){
        /**
         * 初次部署 Redis 时自动添加初始化id
         */
        Jedis jedis = jedisPool.getResource();
        setDefaultkeys(jedis);
        jedis.close();
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
        int id = getEntityId(jedis);
        jedis.close();
        return id;
    }

    public int getEntityId(Jedis jedis){
        long end = getTime(500);

        while (time.getTime()<end) {
            if (acquire_lock("dialog.entity.id.lock")) {
                int entityId = Integer.parseInt(jedis.get("dialog.entity.id"));
                jedis.incr("dialog.entity.id");
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
        Jedis jedis = getJedis();
        if (isChanged) {
            jedis.incr("dialog.user."+username+".changetimes");
            jedis.incr("dialog.entity.temporary." + changedId);
            if (Integer.parseInt(jedis.get("dialog.entity.temporary." + changedId))==1)
                jedis.rpush("dialog.entity.temporary", String.valueOf(changedId));
        }

        if (isKill){
            jedis.del("dialog.entity.temporary." + changedId);
            jedis.lrem("dialog.entity.temporary", 0,  String.valueOf(changedId));
            entityMapper.setEntityRejected(changedId);
        }

        int userChangeTimes = 0;
        if(jedis.exists("dialog.user."+username+".changetimes"))
            userChangeTimes = Integer.parseInt(jedis.get("dialog.user."+username+".changetimes"));
        else
            userLogin(username);

        long changedListLength = jedis.llen("dialog.entity.temporary");

        if (changedListLength==0||userChangeTimes>5||userChangeTimes>0.5*changedListLength){
            jedis.close();
            return getEntityId();
        }
        else{
            /**
             * 判断列表中弹出的记录id已被替换的次数
             * 被替换次数过多，则直接抛弃
             */
            String tempId = null;
            for (int i=0;i<changedListLength+1;i++){
                tempId = jedis.lpop("dialog.entity.temporary");

                if (tempId==null)
                    break;

                int id_times = Integer.parseInt(jedis.get("dialog.entity.temporary." + tempId));

                if (id_times>=4){
                    entityMapper.setEntityRejected(Integer.parseInt(tempId));
                    jedis.del("dialog.entity.temporary." + tempId);
                }
                else{
                    break;
                }
            }

            jedis.close();
            if (tempId==null) {
                return getEntityId();
            }

            return Integer.parseInt(tempId);
        }
    }
    /**
     * 获得指定的锁
     * @param lockName
     * @return
     */
    public boolean acquire_lock(String lockName)  {

        long end = getTime(500);

        try (Jedis jedis = getJedis()){
            while (time.getTime() < end) {
                if (jedis.setnx(lockName, "1") == 1) {
                    jedis.expire(lockName, 1);
                    jedis.close();
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
        Jedis jedis = getJedis();
        jedis.del(lockName);
        jedis.close();
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
        Jedis jedis = getJedis();
        jedis.setnx("dialog.user.login."+username, "1");
        jedis.expire("dialog.user.login." + username, 24 * 60 * 60);
        /**
         * 设置切换次数及其过期时间
         */
        jedis.setnx("dialog.user."+username+".changetimes", "0");
        jedis.expire("dialog.user." + username + ".changetimes", 24 * 60 * 60);

        jedis.close();
    }

    /**
     * 存储被用户舍弃的记录id
     * @param id
     */
    public void restoreTempRecordId(int id){
        long end = getTime(5);

        Jedis jedis = getJedis();

        while (time.getTime()<end) {
            if (acquire_lock("dialog.entity.id.lock")) {
                int entityId = Integer.parseInt(jedis.get("dialog.entity.id"));

                if (id<entityId){
                    jedis.rpush("dialog.entity.temporary", String.valueOf(id));
                    jedis.setnx("dialog.entity.temporary." + id, "0");
                }
                release_lock("dialog.entity.id.lock");
                jedis.close();
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
        Jedis jedis = getJedis();
        restoreTempRecordId(record_id);
        boolean flag = jedis.del("dialog.user.login."+key)==0?false:true;
        jedis.close();
        return flag;
    }

    private long getTime(int timeout){
        return time.getTime() + timeout;
    }


    public Jedis getJedis(){
        return jedisPool.getResource();
    }

}
