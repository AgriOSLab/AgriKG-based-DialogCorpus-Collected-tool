package com.lychee.dialog.Neo4jPack.Mapper.Proxy;

import com.lychee.dialog.Neo4jPack.Annotations.Select;
import com.lychee.dialog.Neo4jPack.app.Neo4jApp;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class Neo4jProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Select select = method.getAnnotation(Select.class);
        String query = "";

        if (method.getName().equals("queryEntityInfoWithPdno")||
        method.getName().equals("queryEntityInfo")){
            String entityName = (String) args[0];
            query = select.value().replace("#{name}", entityName);
            if (args.length==2) {
                String pdno = (String) args[1];
                query = query.replace("#{pdno}", pdno);
            }
            return Neo4jApp.getInstance().queryInfo(query);
        }

        if (args[0] instanceof String){
            String entityName = (String) args[0];
            query = select.value().replace("#{name}", entityName);
            if (args.length==3){
                String pdno = (String) args[1];
                query = query.replace("#{pdno}", pdno);
            }
        }

        if (args.length==3&&(boolean) args[2])
            return Neo4jApp.getInstance().query(query, (boolean) args[2]);
        return Neo4jApp.getInstance().query(query, false);
    }
}
