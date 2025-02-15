package com.hao.tnotes.common.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 请求笔记分享链接
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotesShareDto {

    //笔记本或者笔记的id
    private Long id;
    //分享链接的类型 1为阅读 2为编辑 3为管理（笔记用不上）
    private Integer type;
    //分享链接的密码
    public String password;

}
