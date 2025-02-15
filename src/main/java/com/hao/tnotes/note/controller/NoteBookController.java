package com.hao.tnotes.note.controller;

import com.hao.tnotes.common.bean.dto.NoteBookDto;
import com.hao.tnotes.common.bean.dto.NotesCodeAuthDto;
import com.hao.tnotes.common.bean.dto.NotesShareDto;
import com.hao.tnotes.common.bean.dto.PermissionDto;
import com.hao.tnotes.common.util.common.AjaxResult;
import com.hao.tnotes.note.service.NoteBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notebook")
public class NoteBookController {

    @Autowired
    private NoteBookService noteBookService;


    @GetMapping("/list")
    public AjaxResult list(){
        return AjaxResult.success(noteBookService.list());
    }

    //两种选项：1.修改2.修改或新增
    @PostMapping("/addOrUpdateNoteBook")
    public AjaxResult addOrUpdateNoteBook(@RequestBody NoteBookDto noteBookDto){
        noteBookService.addOrUpdateNoteBook(noteBookDto);
        return AjaxResult.success();
    }

    @GetMapping("/deleteNoteBook")
    public AjaxResult deleteNoteBook(Long id,Integer deleteType){
        noteBookService.deleteNoteBook(id,deleteType);
        return AjaxResult.success();
    }

    /**
     * 单个笔记本的获取
     * @param id
     * @return
     */
    @GetMapping("/getNoteBook")
    public AjaxResult getNoteBook(Long id,Integer reqResource){
        return AjaxResult.success(noteBookService.getNoteBook(id,reqResource));
    }

    @PostMapping("/getShareCode")
    public AjaxResult getShareCode(@RequestBody NotesShareDto notesShareDto){
        return AjaxResult.success(noteBookService.getShareCode(notesShareDto));
    }

    @PostMapping("/authCode")
    public AjaxResult authCode(@RequestBody NotesCodeAuthDto notesCodeAuthDto){
        noteBookService.authCode(notesCodeAuthDto);
        return AjaxResult.success();
    }

    @PostMapping("/changePermission")
    public AjaxResult changePermission(@RequestBody PermissionDto permissionDto){
        noteBookService.changePermission(permissionDto);
        return AjaxResult.success();
    }

    @GetMapping("/deleteCoopUser")
    public AjaxResult deleteCoopUser(Long userId,Long notebookId){
        noteBookService.deleteCoopUser(userId,notebookId);
        return AjaxResult.success();
    }

}
