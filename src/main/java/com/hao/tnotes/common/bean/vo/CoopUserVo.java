package com.hao.tnotes.common.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoopUserVo {


    private Long userId;
    private String userName;
    private String avatar;
    private Integer permission;//用户权限
    private Integer resource;//协作者来源1.笔记本协作2.笔记协作

}
