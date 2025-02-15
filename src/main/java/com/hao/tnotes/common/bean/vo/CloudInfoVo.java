package com.hao.tnotes.common.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CloudInfoVo {

    private Long rootId;
    private Long AllSize;
    private Long UsedSize;

}
