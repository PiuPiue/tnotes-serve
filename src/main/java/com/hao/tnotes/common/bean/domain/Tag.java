package com.hao.tnotes.common.bean.domain;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 
 * @TableName tag
 */
@Data
@TableName(value ="tag")
public class Tag implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 标签名
     */
    private String name;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除0为未删除，1为删除
     */
    @TableField("is_delete")
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;


}