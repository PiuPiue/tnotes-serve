package com.hao.tnotes.common.util.minio;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MinioReturn {

    private Integer success;
    /**
     * 文件地址
     */
    private String path;
 
    /**
     * 原始文件名
     */
    private String inputName;
 
    /**
     * 最终文件名
     */
    private String outPutName;

}