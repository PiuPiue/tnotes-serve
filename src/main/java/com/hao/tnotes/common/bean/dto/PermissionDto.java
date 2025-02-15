package com.hao.tnotes.common.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionDto {

    private Long userId;
    private Long id;//笔记本id或笔记id
    private Integer permission;

}
