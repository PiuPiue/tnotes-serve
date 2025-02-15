package com.hao.tnotes.cloud.controller;

import com.hao.tnotes.cloud.service.DirectoryService;
import com.hao.tnotes.common.util.common.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dict")
public class DirectoryController {

    @Autowired
    private DirectoryService directoryService;

    @GetMapping("/list")
    public AjaxResult list(){
        return directoryService.list();
    }

    @GetMapping("/getChildDict")
    public AjaxResult getChildDict(Long id){
        return directoryService.getChildDict(id);
    }

    @GetMapping("/createDict")
    public AjaxResult createDict(Long parentId, String name){
       directoryService.createDict(parentId, name);
       return AjaxResult.success();
    }

    @GetMapping("/deleteDict")
    public AjaxResult deleteDict(Long id){
        directoryService.deleteDict(id);
        return AjaxResult.success();
    }

    @GetMapping("/getCloudInfo")
    public AjaxResult getCloudInfo(){
        return directoryService.getCloudInfo();
    }

    @GetMapping("/updateDict")
    public AjaxResult updateDict(Long id, String name){
        directoryService.updateDict(id, name);
        return AjaxResult.success();
    }

    //获取删除的文件和文件夹的记录
    @GetMapping("/getDeletedDict")
    public AjaxResult getDeletedDict(){
        return directoryService.getDeletedDict();
    }

    @GetMapping("/recycleDict")
    public AjaxResult recycleDict(Long id){
        directoryService.recycleDict(id);
        return AjaxResult.success();
    }

    //彻底删除
    @GetMapping("/completeDelete")
    public AjaxResult completeDelete(Long id){
        directoryService.completeDelete(id);
        return AjaxResult.success();
    }

    //清空回收站
    @GetMapping("/clearRecycle")
    public AjaxResult clearRecycle(){
        directoryService.clearRecycle();
        return AjaxResult.success();
    }

}
