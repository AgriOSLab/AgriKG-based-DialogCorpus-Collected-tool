package com.lychee.dialog.Neo4jPack.Server;

import ch.qos.logback.core.joran.conditional.ElseAction;
import com.lychee.dialog.Neo4jPack.Mapper.Neo4jQuery;
import com.lychee.dialog.Neo4jPack.Mapper.Neo4jQueryMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 负责将查询结果转化为指定格式，便于便后端解析
 * Controller直接调用
 */
@Component
public class Neo4jServer {

    private Neo4jQuery neo4jQuery = new Neo4jQuery();

    /**
     * 查询以 entityName 为中心的子图
     * @param entityName
     * @return
     */
    public Map querySubGraph(String entityName, String pdno){
        Map map;
        if (pdno==null)
            map = (Map) neo4jQuery.querySubGraph(entityName);
        else
            map = (Map) neo4jQuery.querySubGraphWithPdno(entityName, pdno);

        map.put("entity", entityName);
        return map;
    }

    public Map queryEntityInfo(String entityName, String pdno){
        return neo4jQuery.queryEntityInfo(entityName, pdno);
    }
}
