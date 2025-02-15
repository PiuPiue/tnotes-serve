package com.hao.tnotes.common.bean.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EssayVo {

    private Long id;
    private String title;
    private String content;
    private String user;
    private Integer visible;
    private Date updateTime;
    private Date createTime;
}
