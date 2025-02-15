package com.hao.tnotes.note.service;

import com.hao.tnotes.common.bean.dto.NoteBookDto;
import com.hao.tnotes.common.bean.dto.NotesCodeAuthDto;
import com.hao.tnotes.common.bean.dto.NotesShareDto;
import com.hao.tnotes.common.bean.dto.PermissionDto;
import com.hao.tnotes.common.bean.vo.NoteBookVo;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;

import java.util.List;

public interface NoteBookService {

    /**
     * 获取用户的笔记本列表
     * @return
     */
    List<NoteBookVo> list();

    /**
     * 添加或更新笔记本
     * @param noteBookDto
     */
    void addOrUpdateNoteBook(NoteBookDto noteBookDto);

    /**
     * 删除笔记本
     * @param id
     */
    void deleteNoteBook(Long id, Integer deleteType);

    /**
     * 获取单个笔记本
     * @param id 笔记本id
     * @param reqResource 访问来源，新世界或笔记模块
     * @return
     */
    NoteBookVo getNoteBook(Long id,Integer reqResource);

    /**
     * 获取分享码
     * @param notesShareDto
     * @return
     */
    String getShareCode(NotesShareDto notesShareDto);

    /**
     * 验证分享码
     * @param code
     * @return
     */
    void authCode(NotesCodeAuthDto code);

    void changePermission(PermissionDto permissionDto);

    void deleteCoopUser(Long userId, Long notebookId);
}
