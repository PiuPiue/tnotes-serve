package com.hao.tnotes.note.controller;


import com.hao.tnotes.common.bean.dto.*;
import com.hao.tnotes.common.bean.vo.NoteVo;
import com.hao.tnotes.common.util.common.AjaxResult;
import com.hao.tnotes.note.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/note")
public class NoteController {

    @Autowired
    private NoteService noteService;


    @GetMapping("/list")
    public AjaxResult list(Long id,Integer reqResource) {
        List<NoteVo> list = noteService.list(id,reqResource);
        return AjaxResult.success(list);
    }

    @PostMapping("/addNoteContent")
    public AjaxResult addNoteContent(@RequestBody NoteContentDto noteContentDto) {
        noteService.addNoteContent(noteContentDto);
        return AjaxResult.success();
    }


    @GetMapping("/getNote")
    public AjaxResult getNote(Long id,Integer reqResource) {
        NoteVo note = noteService.getNote(id,reqResource);
        return AjaxResult.success(note);
    }

    @PostMapping("/addNote")
    public AjaxResult addNote(@RequestBody NoteDto noteDto) {
        Long id = noteService.addNote(noteDto);
        return AjaxResult.success(id);
    }

    @PostMapping("/getShareCode")
    public AjaxResult getShareCode(@RequestBody NotesShareDto notesShareDto){
        return AjaxResult.success(noteService.getShareCode(notesShareDto));
    }

    @PostMapping("/authCode")
    public AjaxResult authCode(@RequestBody NotesCodeAuthDto notesCodeAuthDto){
        noteService.authCode(notesCodeAuthDto);
        return AjaxResult.success();
    }

    @GetMapping("/deleteNote")
    public AjaxResult deleteNote(Long id){
        noteService.deleteNote(id);
        return AjaxResult.success();
    }

    @GetMapping("/deleteJoinNote")
    public AjaxResult deleteJoinNote(Long id){
        noteService.deleteJoinNote(id);
        return AjaxResult.success();
    }

    @GetMapping("/getNoteCoopUser")
    public AjaxResult getNoteCoopUser(Long id){
        return AjaxResult.success(noteService.getNoteCoopUser(id));
    }

    @PostMapping("/changePermission")
    public AjaxResult changePermission(@RequestBody PermissionDto permissionDto){
        noteService.changePermission(permissionDto);
        return AjaxResult.success();
    }

    @GetMapping("/deletePermission")
    public AjaxResult deletePermission(Long userId,Long noteId){
        noteService.deletePermission(userId,noteId);
        return AjaxResult.success();
    }

    @GetMapping("/getCoopNote")
    public AjaxResult getCoopNote(){
        return AjaxResult.success(noteService.getCoopNote());
    }

    @PostMapping("/updateNote")
    public AjaxResult updateNote(@RequestBody NoteDto noteDto){
        noteService.updateNote(noteDto);
        return AjaxResult.success();
    }
}
