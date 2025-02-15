package com.hao.tnotes.common.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteDto {

    private Long id;
    private Long notebookId;
    private String title;
    private String description;//描述
    private String cover;//封面，用于公开时添加
    private Integer type;//笔记类型0：普通笔记，1：随笔
    private Integer status;//状态 0.尽自己 1.协作
    private Integer visible;//互联网状态 0.尽自己 1.公开
}
