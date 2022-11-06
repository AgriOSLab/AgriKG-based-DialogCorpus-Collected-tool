package com.lychee.dialog.myBatis;

import com.lychee.dialog.myBatis.Entitys.Dialog;
import com.lychee.dialog.myBatis.Entitys.Entity;
import com.lychee.dialog.myBatis.Entitys.Intention;
import com.lychee.dialog.redis.Redis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Component
public class myBatisUtils {
    @Autowired
    Redis redis;

    @Autowired
    EntityMapper entityMapper;

    private Date time = new Date();


    /**
     * 向语料库中插入新的数据
     * 首先获得记录对应的record_id（加锁）
     * 然后向语料库中插入记录
     * 接着更新Redis中的record_id，即+1
     * 最后释放锁
     * @return
     */
    public boolean insertCorpusRecord(ArrayList<Dialog> dialogRecords, int recordId){
        long end = time.getTime() + 1000;
        Jedis jedis = redis.getJedis();

        while (time.getTime()<end){
            if (redis.acquire_lock("dialog.corpus.count.lock")){
                int record_id = Integer.parseInt(jedis.get("dialog.corpus.count"));
                for (Dialog dialogRecord : dialogRecords) {
                    dialogRecord.setRecord_id(record_id);
                    entityMapper.insertRecord(dialogRecord);
                    entityMapper.setEntityUsed(recordId);
                }
                jedis.incr("dialog.corpus.count");
                redis.release_lock("dialog.corpus.count.lock");
                return true;
            }
        }
        return false;
    }

    public Entity getNewEntity(String username, boolean isWantAnother, int oldId, boolean isKill){
        int id = redis.getNewEntityId(username, isWantAnother, oldId, isKill);
        return entityMapper.getEntityById(id);
    }

    public List<Intention> getIntentions(){
        List<Intention> intentions = entityMapper.getIntentions();
        return intentions;
    }
    @PostConstruct
    public void setDefault(){
        Integer maxRecordId = entityMapper.getMaxRecordId();
        Jedis jedis = redis.getJedis();
        if (maxRecordId==null)
            jedis.set("dialog.corpus.count", "1");
        else
            jedis.set("dialog.corpus.count", String.valueOf(maxRecordId+1));
    }
}
