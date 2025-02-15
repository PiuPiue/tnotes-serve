package com.hao.tnotes.common.util.common;

import lombok.Getter;

@Getter
public class TException extends RuntimeException{

    private int code;//错误代码

    //普通构造
    public TException(int code, String message) {
        super(message);
        this.code = code;
    }

    //使用枚举类型构造
    public TException(Exceptions exceptions) {
        super(exceptions.getMessage());
        this.code = exceptions.getCode();
    }



}
