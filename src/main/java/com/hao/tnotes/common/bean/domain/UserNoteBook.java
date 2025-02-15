package com.hao.tnotes.common.bean.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user_notebook")
public class UserNoteBook {


    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long notebookId;
    private int source;
    private Date createTime;

    @TableField("is_delete")
    @TableLogic(value = "0", delval = "1")
    private int isDelete;
}
