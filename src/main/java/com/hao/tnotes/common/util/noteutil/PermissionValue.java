package com.hao.tnotes.common.util.noteutil;


import lombok.Data;

@Data
public class PermissionValue {


    public static final Integer NO_VISIBLE = 0;
    public static final Integer VISIBLE = 1;
    public static final Integer ONLY_READ = 1;
    public static final Integer READ_WRITE = 2;
    public static final Integer ADMIN = 3;
    public static final Integer MASTER_ADMIN = 0;//本人


}
