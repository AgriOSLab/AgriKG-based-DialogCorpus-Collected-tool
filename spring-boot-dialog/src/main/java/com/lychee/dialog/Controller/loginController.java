package com.lychee.dialog.Controller;

import com.lychee.dialog.common.Result;
import com.lychee.dialog.redis.Redis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping({"/login"})
public class loginController {
    @Autowired
    Redis redis;

    @PostMapping
    public Result getLogin(@RequestBody String username, HttpServletRequest request){
        HttpSession session = request.getSession();
        username = username.substring(1, username.length()-1);
        session.setAttribute("username", username);
        redis.userLogin(username);
        return Result.success();
    }


}
