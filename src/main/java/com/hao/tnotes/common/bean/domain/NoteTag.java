package com.hao.tnotes.common.bean.domain;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 
 * @TableName note_tag
 */
@Data
@TableName(value ="note_tag")
public class NoteTag implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 笔记id
     */
    private Long noteId;

    /**
     * 标签id
     */
    private Long tagId;

    /**
     * 是否删除0为未删除，1为删除
     */
    @TableField("is_delete")
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;


}