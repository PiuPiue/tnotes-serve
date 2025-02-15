package com.hao.tnotes.cloud.service;

import com.hao.tnotes.common.util.common.AjaxResult;

public interface DirectoryService {
    AjaxResult list();

    AjaxResult getChildDict(Long id);

    /**
     * 创建文件夹
     * @param parentId
     * @param name
     */
    void createDict(Long parentId, String name);

    void deleteDict(Long id);

    AjaxResult getCloudInfo();


    void updateDict(Long id, String name);

    AjaxResult getDeletedDict();


    void recycleDict(Long id);

    void completeDelete(Long id);

    void clearRecycle();

}
