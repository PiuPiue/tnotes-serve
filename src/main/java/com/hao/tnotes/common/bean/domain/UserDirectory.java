package com.hao.tnotes.common.bean.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;
@Data
@TableName(value = "user_directory")
public class UserDirectory {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long directoryId;
    private Long allSize;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "is_delete", fill = FieldFill.INSERT)
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;

}
