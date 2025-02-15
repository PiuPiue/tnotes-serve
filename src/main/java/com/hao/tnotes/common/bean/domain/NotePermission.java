package com.hao.tnotes.common.bean.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value ="note_permission")
public class NotePermission {

    @TableId(type = IdType.ASSIGN_ID)
    public Long id;
    private Long userId;
    private Long noteId;
    private Integer permission;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableLogic(value = "0", delval = "1")
    @TableField(value = "is_delete",fill = FieldFill.INSERT_UPDATE)
    private Integer isDelete;

}
