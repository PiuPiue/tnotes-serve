package com.hao.tnotes.common.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckFileVo {

    private Boolean error;
    private String message;
    private boolean skipUpload;
    private Integer[] uploaded;


}
