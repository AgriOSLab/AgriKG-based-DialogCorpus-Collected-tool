package com.lychee.dialog.Neo4jPack.Mapper;

import com.alibaba.fastjson2.JSON;
import com.lychee.dialog.Neo4jPack.Mapper.Proxy.Neo4jProxyFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Neo4jQuery {
    private Neo4jQueryMapper neo4jQueryMapper = (Neo4jQueryMapper) Neo4jProxyFactory.getProxy();

    /**
     *
     * @param key
     * @return
     */
    public Map querySubGraph(String key){
        Map map = neo4jQueryMapper.queryGraph(key, true);
        return map;
    }

    public Map querySubGraphWithPdno(String key, String pdno){
        Map map = neo4jQueryMapper.querySubGraphWithPdno(key, pdno, true);

        return map;
    }

    public Map queryEntityInfo(String entityName, String pdno){

        HashMap<String, Object> map = new HashMap<>();
        HashMap<String, String> result;
        if (pdno!=null) {
            result = neo4jQueryMapper.queryEntityInfoWithPdno(entityName, pdno);
        }
        else {
            result = neo4jQueryMapper.queryEntityInfo(entityName);
        }
        map.putAll(JSON.parseObject(result.get("props")));
        map.put("label", result.get("labels"));
        return map;
    }
}
