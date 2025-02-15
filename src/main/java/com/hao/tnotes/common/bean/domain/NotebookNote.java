package com.hao.tnotes.common.bean.domain;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 
 * @TableName notebook_note
 */
@Data
@TableName(value ="notebook_note")
public class NotebookNote implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 笔记本id
     */
    private Long notebookId;

    /**
     * 笔记id
     */
    private Long noteId;

    /**
     * 是否删除0为未删除，1为删除
     */
    @TableField("is_delete")
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;


}