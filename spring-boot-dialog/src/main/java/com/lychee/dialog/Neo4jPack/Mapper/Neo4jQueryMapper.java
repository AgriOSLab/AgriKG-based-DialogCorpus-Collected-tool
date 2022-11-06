package com.lychee.dialog.Neo4jPack.Mapper;

import com.lychee.dialog.Neo4jPack.Annotations.Select;

import java.util.List;
import java.util.Map;

public interface Neo4jQueryMapper {

    @Select("match (n{name:\"#{name}\"})-[r]-(m) return n,r,m, n.登记证号, m.登记证号")
    Map queryGraph(String name, boolean isGraph);

    @Select("match (n{name:\"#{name}\", 登记证号:\"#{pdno}\"})-[r]-(m) return n,r,m")
    Map querySubGraphWithPdno(String name, String pdno, boolean isGraph);

    @Select("match (n{name:\"#{name}\", 登记证号:\"#{pdno}\"}) return properties(n) as props, labels(n) as labels")
    <T> T queryEntityInfoWithPdno(String name, String pdno);

    @Select("match (n{name:\"#{name}\"}) return properties(n) as props, labels(n) as labels")
    <T> T queryEntityInfo(String name);
}
