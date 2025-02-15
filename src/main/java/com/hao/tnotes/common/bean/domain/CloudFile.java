package com.hao.tnotes.common.bean.domain;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 
 * @TableName cloud_file
 */
@Data
@TableName(value ="cloud_file")
public class CloudFile implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String md5;

    private String name;

    private String url;

    /**
     * 文件大小,单位为字节
     */
    private Long size;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 是否删除0为未删除，1为删除
     */
    @TableField(value = "is_delete", fill = FieldFill.INSERT)
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;

}