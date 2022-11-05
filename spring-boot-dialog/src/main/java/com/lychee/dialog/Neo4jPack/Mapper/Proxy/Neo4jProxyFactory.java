package com.lychee.dialog.Neo4jPack.Mapper.Proxy;

import com.lychee.dialog.Neo4jPack.Mapper.Neo4jQueryMapper;

import java.lang.reflect.Proxy;

public class Neo4jProxyFactory {

    public static Object getProxy(){
        return Proxy.newProxyInstance(Neo4jQueryMapper.class.getClassLoader(),
                new Class[]{Neo4jQueryMapper.class},
                new Neo4jProxy());
    }
}
