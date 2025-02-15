package com.hao.tnotes.note.service;

import com.hao.tnotes.common.bean.dto.LoginDto;
import com.hao.tnotes.common.bean.dto.UserDto;
import com.hao.tnotes.common.bean.vo.UserVo;

public interface UserService {

    //用户登录
    UserVo login(LoginDto loginDto);

    void logout();

    void register(UserDto userDto);

    void sendAuthCode(String email);

    UserVo updateUser(UserDto userDto);

    void modifyPassword(UserDto userDto);

    void sendPwdCode();

    void sendEmailCode();

    void modifyEmail(UserDto userDto);

    void unbindEmail(String code);

    void sendNewEmailCode(String email);
}
