package com.hao.tnotes.common.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteVo {
    //作为随笔出现时需要有什么
    private Long id;
    private String title;
    private Object content;
    private String description;
    private Integer visible;//两种状态0.不公开1.公开到新世界
    private Integer status;//两种状态0.仅自己2.协作
    private Integer type;
    private String user;
    //当前人权限
    private Integer permission;
    //当前参与的协作者
    private List<UserVo> coopUser;
    private Date createTime;
    private Date updateTime;
}
