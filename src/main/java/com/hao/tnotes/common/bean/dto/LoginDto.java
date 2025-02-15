package com.hao.tnotes.common.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {

    private String name;
    private String password;
    private String email;
    private int loginType;//登录类型0为用户名1为邮箱

}
