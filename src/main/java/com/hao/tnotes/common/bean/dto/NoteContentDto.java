package com.hao.tnotes.common.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteContentDto {

    //笔记的id
    private Long id;
    private String title;
    private Map<String, Object> content;

}
