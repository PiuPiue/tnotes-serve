package com.hao.tnotes.common.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {



    private String avatar;

    private String name;

    private String description;

    private String password;

    private String email;

    private String authCode;


}
