package com.hao.tnotes.common.bean.domain;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 
 * @TableName notebook
 */
@Data
@TableName("notebook")
public class Notebook implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 
     */
    private String title;

    /**
     * 
     */
    private String cover;

    /**
     * 
     */
    private String description;

    /**
     * 创建者id
     */
    @TableField("user_id")
    private Long user;

    /**
     * 可见性，是否公开到新世界被他人查看
     */
    private Integer visible;

    /**
     * 0为仅自己可查看,1为他人可查看,2为他人可编辑,3为共享至新世界
     */
    private Integer status;

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
    @TableField(value = "is_delete", fill=FieldFill.INSERT)
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;


}