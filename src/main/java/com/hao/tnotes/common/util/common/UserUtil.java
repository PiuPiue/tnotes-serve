package com.hao.tnotes.common.util.common;

import cn.dev33.satoken.stp.StpUtil;

public class UserUtil {

    public static String getUserId() {
        //首先获取用户的id
        String loginId = (String) StpUtil.getLoginId();
        if (loginId == null) {
            //用户未登录
            throw new TException(Exceptions.USER_IS_NOT_LOGIN);
        }
        return loginId;
    }


}
