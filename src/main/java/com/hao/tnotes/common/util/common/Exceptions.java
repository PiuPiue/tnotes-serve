package com.hao.tnotes.common.util.common;

import lombok.Data;


public enum Exceptions {

    USER_HAS_NOT_LOGIN(401, "用户未登录"),
    USER_IS_NOT_VIP(402, "非会员用户只能新建5本笔记"),
    USER_HAS_NO_PERMISSION(403, "用户没有权限"),
    HAS_NOT_UNBIND_EMAIL(406,"邮箱还未解绑" ),
    AUTH_CODE_ERROR(400, "验证码错误"),
    USER_NAME_HAS_EXIST(400, "用户名已存在"),
    USER_EMAIL_HAS_EXIST(400, "邮箱已被绑定"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    USER_IS_NOT_LOGIN(401, "用户未登录"),
    NAME_OR_PASSWORD_IS_EMPTY(400, "用户名或密码不能为空"),
    USER_OR_EMAIL_IS_EMPTY(400, "用户名或邮箱不能为空"),
    USER_OR_PASSWORD_IS_ERROR(400, "用户名或密码错误"),
    NOTEBOOK_IS_NOT_FOUND(601, "笔记本不存在"),
    NOTEBOOK_IS_NOT_EMPTY(602, "笔记本不为空"),
    SHARE_CODE_IS_NOT_FOUND(603, "分享码不存在"),
    NOTE_IS_NOT_FOUND(701,"笔记不存在"),
    PARAM_ERROR(402, "参数错误"),
    PASSWORD_ERROR(405, "密码错误"),
    DIRECTORY_IS_NOT_FOUND(801, "目录不存在"),
    DIRECTORY_IS_NOT_EMPTY(802, "目录不为空"),
    DIRECTORY_IS_NOT_EMPTY_WITH_FILE(803, "目录不为空，有文件"),
    DIRECTORY_IS_NOT_EMPTY_WITH_DIRECTORY(804, "目录不为空，有子目录"),
    DIRECTORY_IS_NOT_EMPTY_WITH_FILE_AND_DIRECTORY(805, "目录不为空，有文件与子目录"),
    FILE_UPLOAD_ERROR(806, "文件上传失败"),
    FILE_MERGE_ERROR(807, "文件合并失败"),
    FILE_PREVIEW_ERROR(808, "文件预览失败"),
    FILE_IS_NOT_FOUND(809,"文件不存在" ),
    USER_HAS_NO_SPACE(810,"用户空间不足" );


    private final int code;
    private final String message;

    Exceptions(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
