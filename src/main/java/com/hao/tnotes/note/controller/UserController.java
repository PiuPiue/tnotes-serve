package com.hao.tnotes.note.controller;

import com.hao.tnotes.common.bean.dto.LoginDto;
import com.hao.tnotes.common.bean.dto.UserDto;
import com.hao.tnotes.common.bean.vo.UserVo;
import com.hao.tnotes.common.util.common.AjaxResult;
import com.hao.tnotes.note.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginDto loginDto) {
        UserVo userInfo = userService.login(loginDto);
        return AjaxResult.success(userInfo);
    }

    @GetMapping("/logout")
    public AjaxResult logout() {
        userService.logout();
        return AjaxResult.success();
    }

    @PostMapping("/register")
    public AjaxResult register(@RequestBody UserDto userDto) {
        userService.register(userDto);
        return AjaxResult.success();
    }

    @GetMapping("/sendAuthCode")
    public AjaxResult sendAuthCode(String email) {
        userService.sendAuthCode(email);
        return AjaxResult.success();
    }

    @GetMapping("/sendPwdCode")
    public AjaxResult sendPwdCode() {
        userService.sendPwdCode();
        return AjaxResult.success();
    }

    @GetMapping("/sendEmailCode")
    public AjaxResult sendEmailCode() {
        userService.sendEmailCode();
        return AjaxResult.success();
    }
    //修改个人资料和修改密码以及密码的存储
    @PostMapping("/updateUser")
    public AjaxResult updateUser(@RequestBody UserDto userDto) {
        UserVo userVo = userService.updateUser(userDto);
        return AjaxResult.success(userVo);
    }

    @PostMapping("/modifyPassword")
    public AjaxResult modifyPassword(@RequestBody UserDto userDto) {
        userService.modifyPassword(userDto);
        return AjaxResult.success();
    }

    @GetMapping("/unbindEmail")
    public AjaxResult unbindEmail(String code) {
        userService.unbindEmail(code);
        return AjaxResult.success();
    }
    //修改邮箱
    @PostMapping("/modifyEmail")
    public AjaxResult modifyEmail(@RequestBody UserDto userDto) {
        userService.modifyEmail(userDto);
        return AjaxResult.success();
    }

    //新邮箱验证码生成
    @GetMapping("/sendNewEmailCode")
    public AjaxResult sendNewEmailCode(String email) {
        userService.sendNewEmailCode(email);
        return AjaxResult.success();
    }




}
