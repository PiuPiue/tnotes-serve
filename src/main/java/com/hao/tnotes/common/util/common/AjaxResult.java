package com.hao.tnotes.common.util.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AjaxResult {

    //返回码
    private int code;
    //返回消息
    private String message;
    //返回数据
    private Object data;

    //成功后返回数据
    public static AjaxResult success(Object data) {
        return new AjaxResult(200, "success", data);
    }

    //无结果时返回数据
    public static AjaxResult success() {
        return new AjaxResult(200, "success", null);
    }

    //失败后返回信息
    public static AjaxResult error(String message) {
        return new AjaxResult(500, message, null);
    }

    //失败后返回信息加代码
    public static AjaxResult error(int code, String message) {
        return new AjaxResult(code, message, null);
    }
    //失败后返回
    public static AjaxResult error() {
        return new AjaxResult(500, "error", null);
    }


}
