package com.hao.tnotes.common.advice;


import com.hao.tnotes.common.util.common.AjaxResult;
import com.hao.tnotes.common.util.common.Exceptions;
import com.hao.tnotes.common.util.common.TException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
public class CommonExceptionAdvice {


    @ExceptionHandler(TException.class)
    /*如果不加，则会导致无法进行异常处理*/
    @ResponseBody
    public AjaxResult error(TException e) {
        e.printStackTrace();
        return new AjaxResult(e.getCode(), e.getMessage(),null);
    }
}