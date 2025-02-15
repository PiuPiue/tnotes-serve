package com.hao.tnotes.common.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * 目录返回类，主要结构为父目录、子目录、笔记
 * 所以说，对于前端来说，这里进行设计时需要考虑的是返回的是一个文件吗，这就涉及到前端如何进行预览的问题了
 * 是依靠于后端所传的内容还是依靠于前端访问特定网址的问题，同时还需要记录的有前端使用的
 */
public class DictVo {

    private Long id;
    //文件名称或文件夹名称
    private String name;
    //1为文件夹 0为文件
    private Integer type;
    //真实的业务应该是点击之后有前端进行预览或者下载
    private Integer size;
    private String url;
    private Long parentId;
    private Date updateTime;
    private Date createTime;

}
