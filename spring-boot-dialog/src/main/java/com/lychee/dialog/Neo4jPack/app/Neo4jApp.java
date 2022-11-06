package com.lychee.dialog.Neo4jPack.app;

import com.alibaba.fastjson2.JSON;
import org.neo4j.driver.*;
import org.neo4j.driver.internal.value.MapValue;

import java.util.*;

public class Neo4jApp {
    private String uri = 
    private String username = "neo4j";
    private String passward = 

    private Driver driver;

    private Neo4jApp() {
    }

    /**
     * 静态内部类实现单例模式
     * 保证 Driver 实例化一次，即程序仅连接、登录一次 Neo4j
     */
    private static class Neo4jAppInstance{
        private static Neo4jApp instance = new Neo4jApp();
        static {
            instance.driver = GraphDatabase.driver(instance.uri, AuthTokens.basic(instance.username, instance.passward));
        }
    }

    public static Neo4jApp getInstance(){
        return Neo4jAppInstance.instance;
    }

    public <T> T query(String query, boolean isGraph){
        try (Session session = driver.session()){
            T value = (T)session.readTransaction((tx) -> {
                Result result = tx.run(query);

                if (isGraph){
                    Map<String, Set> map = new HashMap<>();
                    Set<Map<String, String>> nodes = new HashSet<>();
                    Set<Map<String, String>> edges = new HashSet<>();

                    while (result.hasNext()){
                        Record next = result.next();
                        final List<Value> values = next.values();

                        HashMap<String, String> item = new HashMap<>();

                        String firstNode = next.get(0).asMap().get("name").toString();
                        String secondNode = next.get(2).asMap().get("name").toString();


                        item.put("id", firstNode);
                        item.put("label", firstNode);

                        nodes.add(new HashMap<>(item));

                        item.put("id", secondNode);
                        item.put("label", secondNode);

                        nodes.add(new HashMap<>(item));

                        item.clear();

                        item.put("from", next.get(1).asRelationship().startNodeId()==next.get(0).asNode().id()?firstNode:secondNode);
                        item.put("to", next.get(1).asRelationship().startNodeId()==next.get(2).asNode().id()?firstNode:secondNode);
                        item.put("label", next.get(1).asRelationship().type());

                        edges.add(new HashMap<>(item));
                    }
                    map.put("nodes", nodes);
                    map.put("edges", edges);
                    return map;
                }

                List<String> results = new ArrayList<>();
                while (result.hasNext()) {
                    final Value value1 = result.next().get(0);
                    if (value1 instanceof MapValue) {
                        results.add(JSON.toJSONString(value1.asMap()));
                    } else
                        results.add(value1.asString());
                }
                return results;
            });

            return (T) value;
        }
    }

    public <T> T queryInfo(String query){
        try (Session session = driver.session()){
            T value = (T)session.readTransaction((tx) -> {
                Result result = tx.run(query);

                HashMap<String, String> results = new HashMap<>();
                while (result.hasNext()) {
                    Record next = result.next();
                    Value value1 = next.get(0);
                    results.put("props", JSON.toJSONString(value1.asMap()));
                    value1 = next.get(1);
                    results.put("labels", (String) value1.asList().get(0));
                }
                return results;
            });

            return (T) value;
        }
    }

}
