package com.hao.tnotes.common.bean.domain;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 
 * @TableName note
 */
@Data
@TableName(value ="note")
public class Note implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 
     */
    private String title;

    /**
     * 对应mongodb中的主键
     */
    @TableField("content")
    private String contentId;

    /**
     * 描述，对应为共享至新世界时的描述，创建时不需要
     */
    private String description;

    /**
     * 0为仅自己，1为协作
     */
    private Integer status;

    /**
     * 0为笔记，1为随笔
     */
    private Integer type;

    /**
     * 封面，用于作为单篇文章公开到新世界时使用
     */
    private String cover;

    /**
     * 可见性
     */
    private Integer visible;

    /**
     * 创建者id,当类型为随笔时就是用户创建者id当类型为笔记时就是所属笔记id,属于
                                    冗余字段，但新世界时可以直接使用，简化操作
     */
    @TableField("user_id")
    private Long user;

    /**
     * 创建时间
     */
    @TableField(fill=FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill=FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 是否删除0为未删除，1为删除
     */
    @TableField(value = "is_delete",fill = FieldFill.INSERT)
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;


}