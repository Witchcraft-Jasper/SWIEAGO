package com.example.demo.Controller;

import com.example.demo.Entity.User;
import com.example.demo.Response.ResponseBody;
import com.example.demo.Response.ResponseCode;
import com.example.demo.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Witchcraft
 */
@RestController
@RequestMapping(path = "/User")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping(value = "/Sign/in")//用户登录
    public ResponseBody signIn(@RequestBody User user) {
        if (userService.exists(user)) {
            //找不到说明密码错误
            User u = userService.findByNameAndPassword(user);
            return new ResponseBody(u != null ? ResponseCode.SIGN_IN_SUCCESS : ResponseCode.SIGN_IN_FAILED, u != null ? u : "");
        }
        return new ResponseBody(ResponseCode.SIGN_IN_FAILED, "");
    }

    @PostMapping(value = "/Sign/up")//用户注册
    public ResponseBody addUser(@RequestBody User user) {
        if (userService.exists(user)) {
            return new ResponseBody(ResponseCode.SIGN_UP_FAILED, "");
        }
        return new ResponseBody(userService.insert(user) ? ResponseCode.SIGN_UP_SUCCESS : ResponseCode.SIGN_UP_FAILED, user);
    }

    @PostMapping(path = "/update")
    public ResponseBody modify(@RequestBody User user) {
        return new ResponseBody(userService.update(user) ? ResponseCode.UPDATE_SUCCESS : ResponseCode.UPDATE_FAILED, "");
    }


}