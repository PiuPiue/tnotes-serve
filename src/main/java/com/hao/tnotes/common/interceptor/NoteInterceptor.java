package com.hao.tnotes.common.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.hao.tnotes.common.util.common.Exceptions;
import com.hao.tnotes.common.util.common.TException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Component
public class NoteInterceptor  extends HandlerInterceptorAdapter {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(request.getMethod().equals("OPTIONS")){
            return true;
        }
        String authorization = request.getHeader("Authorization");
        Object loginIdByToken = StpUtil.getLoginIdByToken(authorization);
        if(loginIdByToken!=null){
            return true;
        }
        throw new TException(Exceptions.USER_HAS_NOT_LOGIN);
    }


}
