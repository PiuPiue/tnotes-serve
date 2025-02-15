package com.hao.tnotes.common.bean.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value ="notebook_share")
public class NoteBookShare {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long notebookId;
    private String code;
    private String password;
    private Integer type;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableLogic(value = "0", delval = "1")
    @TableField(value = "is_delete",fill = FieldFill.INSERT_UPDATE)
    private Integer isDelete;

}
