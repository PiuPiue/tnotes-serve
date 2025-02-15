package com.hao.tnotes.common.bean.domain;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 
 * @TableName cloud_file_share
 */
@Data
@TableName("cloud_file_share")
public class CloudFileShare implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 分享码
     */
    private String code;

    /**
     * 创建者id
     */
    private Long userId;

    /**
     * 文件id
     */
    private Long fileId;

    /**
     * 密码
     */
    private String password;

    /**
     * 有效时间,单位为天数，几天内有效，0为永远有效可查看
     */
    private Integer validTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}