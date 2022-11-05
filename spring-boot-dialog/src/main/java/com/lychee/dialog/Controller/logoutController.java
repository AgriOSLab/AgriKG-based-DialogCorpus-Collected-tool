package com.lychee.dialog.Controller;

import com.lychee.dialog.common.Result;
import com.lychee.dialog.redis.Redis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/logout")
public class logoutController {
    @Autowired
    Redis redis;

    @PostMapping
    public Result logout(@RequestBody Map map){
        String username = (String) map.get("username");
        if (map.containsKey("id")) {
            int record_id = (int) map.get("id");
            redis.delUser(username, record_id);
        }
        return Result.success();
    }
}
