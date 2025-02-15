package com.hao.tnotes.common.bean.domain;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;


@Data
@TableName("note_share")
public class NoteShare implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 分享码
     */
    private String code;

    /**
     * 创建者id
     */
    private Long userId;

    /**
     * 笔记id
     */
    private Long noteId;

    /**
     * 密码
     */
    private String password;

    private Integer type;


    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic(value = "0", delval = "1")
    @TableField(value = "is_delete",fill = FieldFill.INSERT_UPDATE)
    private Integer isDelete;


}