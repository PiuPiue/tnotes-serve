package com.hao.tnotes.common.bean.vo;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class NoteBookVo {

    //笔记本id
    private Long id;

    private String title;

    private String cover;

    private String description;

    private Integer noteCount;

    //创建者名称
    private String user;

    //0为仅自己，1为协作
    private Integer status;

    //0为仅自己，1为新世界公开
    private Integer visible;

    //添加一个当前笔记的协作者列表
    private List<UserVo> coopUser;

    private Date createTime;

    private Date updateTime;

    //类型，是1.自己创作的，3.收藏的，还是2.加入协作的
    private Integer resType;
    //当前人权限
    private Integer permission;

}
