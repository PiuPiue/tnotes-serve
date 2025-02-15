package com.hao.tnotes.common.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteBookDto {

    private Long id;
    private String title;
    private String cover;
    private String description;
    private Integer visible;

}
