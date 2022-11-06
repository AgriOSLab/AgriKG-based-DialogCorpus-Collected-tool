package com.lychee.dialog.Controller;

import com.lychee.dialog.Neo4jPack.Server.Neo4jServer;
import com.lychee.dialog.common.Result;
import com.lychee.dialog.common.Utils;
import com.lychee.dialog.myBatis.Entitys.Dialog;
import com.lychee.dialog.myBatis.Entitys.Entity;
import com.lychee.dialog.myBatis.Entitys.Intention;
import com.lychee.dialog.myBatis.myBatisUtils;
import com.lychee.dialog.redis.Redis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.print.attribute.standard.ReferenceUriSchemesSupported;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/collect-dialog-corpus")
public class demoController {
    @Autowired
    Neo4jServer neo4jServer;

    @Autowired
    Redis redis;

    @Autowired
    myBatisUtils mybatisUtils;

    @Autowired
    Utils utils;

    @GetMapping("/{username}")
    public Result getHome(@PathVariable("username") String username){
        Entity newEntity = mybatisUtils.getNewEntity(username, false, -1, false);
        Map map = neo4jServer.querySubGraph(newEntity.getName(), newEntity.getPdno());
        map.put("id", newEntity.getId());
        map.put("pdno", newEntity.getPdno());
        return Result.success(map);
    }

    @PostMapping
    public Result changeAnotherEntity(@RequestBody Map record){
        Entity newEntity;
        if (!record.containsKey("id"))
            newEntity = mybatisUtils.getNewEntity((String) record.get("username"), false, 0, false);
        else
            newEntity = mybatisUtils.getNewEntity((String) record.get("username"), true, (int) record.get("id"), false);
        Map map = neo4jServer.querySubGraph(newEntity.getName(), newEntity.getPdno());
        map.put("id", newEntity.getId());
        map.put("pdno", newEntity.getPdno());
        return Result.success(map);
    }

    @PostMapping("/saveRecords")
    public Result saveRecords(@RequestBody Map records){

        ArrayList<ArrayList> dialogs = (ArrayList) records.get("dialog");
        ArrayList<HashMap<String, String>> triples = (ArrayList<HashMap<String, String>>)records.get("triples");
        ArrayList<ArrayList<String>>  formatTriples = utils.formatTriples(triples);

        HashMap<String, Object> map = (HashMap<String, Object>) records.get("properties");
        String labels = (String) map.get("label");
        map.remove("label");
        String entityName = (String) map.get("name");
        map.remove("name");
        formatTriples.addAll(utils.formatProperties(map, entityName));

        int recordId = (int) records.get("id");

        int turn_id = 1;
        ArrayList<Dialog> dialogRecords = new ArrayList<>();
        for (ArrayList<HashMap<String, Object>> item : dialogs) {
            Dialog dialog = new Dialog();
            dialog.setTurn_id(turn_id++);
            dialog.setQuestion((String) item.get(0).get("sentence"));
            dialog.setQuestion_entity(item.get(0).get("entity").toString());
            dialog.setAnswer((String) item.get(1).get("sentence"));
            dialog.setAnswer_entities(item.get(1).get("entity").toString());
            dialog.setIntention((String) item.get(0).get("intention"));
            dialog.setTriples(formatTriples.toString());
            dialog.setLabels(labels);
            dialog.setSubmitted_person((String) records.get("username"));
            dialogRecords.add(dialog);
        }

        mybatisUtils.insertCorpusRecord(dialogRecords, recordId);
        return Result.success();
    }

    @PostMapping("/queryInfo")
    public Result queryEntityInfo(@RequestBody Map<String, String> map){
        if (map.get("entityName")!=null)
            return Result.success(neo4jServer.queryEntityInfo(map.get("entityName"), map.get("pdno")));
        else
            return Result.success();
    }

    @PostMapping("/killEntity")
    public Result killEntity(@RequestBody Map record){
        Entity newEntity = mybatisUtils.getNewEntity((String) record.get("username"), true, (int) record.get("id"), true);
        Map map = neo4jServer.querySubGraph(newEntity.getName(), newEntity.getPdno());
        map.put("id", newEntity.getId());
        return Result.success(map);
    }


    @GetMapping("/properties/intentions")
    public Result getIntentions(){
        List<Intention> intentions = mybatisUtils.getIntentions();

        return Result.success(intentions);
    }
}
