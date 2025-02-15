package com.hao.tnotes.note.service;

import com.hao.tnotes.common.bean.dto.NoteContentDto;
import com.hao.tnotes.common.bean.dto.NotesCodeAuthDto;
import com.hao.tnotes.common.bean.dto.NotesShareDto;
import com.hao.tnotes.common.bean.dto.PermissionDto;
import com.hao.tnotes.common.bean.vo.CoopUserVo;
import com.hao.tnotes.common.bean.vo.NoteVo;
import com.hao.tnotes.common.bean.dto.NoteDto;

import java.util.List;

public interface NoteService {


    /**
     * 获取笔记本或专栏中的所有笔记
     * @param id
     * @param reqResource 1为新世界 2为笔记模块
     * @return
     */
    List<NoteVo> list(Long id,Integer reqResource);

    /**
     * 笔记内容的变更
     * @param noteContentDto
     */
    void addNoteContent(NoteContentDto noteContentDto);

    /**
     * 获取笔记内容
     * @param id
     * @param reqReSource 1为新世界 2为笔记模块
     * @return
     */
    NoteVo getNote(Long id,Integer reqReSource);

    /**
     * 新增笔记或随笔
     * @param noteDto
     * @return
     */
    Long addNote(NoteDto noteDto);

    /**
     * 获取分享码
     * @param notesShareDto
     * @return
     */
    String getShareCode(NotesShareDto notesShareDto);

    /**
     * 分享码验证
     * @param notesCodeAuthDto
     */
    void authCode(NotesCodeAuthDto notesCodeAuthDto);

    void deleteNote(Long id);

    List<CoopUserVo> getNoteCoopUser(Long id);

    void changePermission(PermissionDto permissionDto);

    void deletePermission(Long userId, Long noteId);

    List<NoteVo> getCoopNote();

    void updateNote(NoteDto noteDto);

    void deleteJoinNote(Long id);
}
