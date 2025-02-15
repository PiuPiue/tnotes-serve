package com.hao.tnotes.common.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVo {

    private Long id;
    private String avatar;
    private String name;
    private String email;
    private String description;
    private Integer isVip;
    private Date createTime;
    /**
     * 协作类型
     * 这里可以分别作为笔记本的和笔记的所以还需要再加一个字段进行区分
     */
    private Integer coopType;
    /**
     * 协作来源
     * 0.笔记本 1.笔记
     */
    private Integer coopResource;
    private String token;

}
