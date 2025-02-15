package com.hao.tnotes.common.bean.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user_note")
public class UserNote {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long user_id;
    private Long note_id;
    private int source;

    @TableLogic(value = "0", delval = "1")
    @TableField(value = "is_delete",fill = FieldFill.INSERT)
    private int is_delete;
    @TableField(value = "create_time",fill = FieldFill.INSERT_UPDATE)
    private Date create_time;

}
