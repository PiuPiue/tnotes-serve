package com.hao.tnotes.note.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hao.tnotes.common.bean.domain.*;
import com.hao.tnotes.common.bean.dto.NoteContentDto;
import com.hao.tnotes.common.bean.dto.NotesCodeAuthDto;
import com.hao.tnotes.common.bean.dto.NotesShareDto;
import com.hao.tnotes.common.bean.dto.PermissionDto;
import com.hao.tnotes.common.bean.vo.CoopUserVo;
import com.hao.tnotes.common.bean.vo.NoteVo;
import com.hao.tnotes.common.bean.dto.NoteDto;
import com.hao.tnotes.common.util.bean.BeanUtils;
import com.hao.tnotes.common.util.common.Exceptions;
import com.hao.tnotes.common.util.common.TException;
import com.hao.tnotes.common.util.common.UserUtil;
import com.hao.tnotes.common.util.noteutil.NotesPermissionUtil;
import com.hao.tnotes.common.util.noteutil.PermissionValue;
import com.hao.tnotes.note.dao.*;
import com.hao.tnotes.note.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class NoteServiceImpl implements NoteService {

    @Autowired
    private NoteDao noteDao;
    @Autowired
    private NoteBookNoteDao noteBookNoteDao;
    @Autowired
    private NoteBookDao noteBookDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private NoteContentDao noteContentDao;
    @Autowired
    private NotesPermissionUtil notesPermissionUtil;
    @Autowired
    private NoteShareDao noteShareDao;
    @Autowired
    private NotePermissionDao notePermissionDao;
    @Autowired
    private NoteBookPermissionDao noteBookPermissionDao;

    @Override
    public List<NoteVo> list(Long id,Integer reqResource) {
        String userId = UserUtil.getUserId();
        //首先检查用户是否具有权限
        Notebook notebook = noteBookDao.selectById(id);
        if(notebook==null){
            throw new TException(Exceptions.NOTEBOOK_IS_NOT_FOUND);
        }
        //权限判定，此时查找的是笔记本中的笔记
        //此处判定思路，此时查找的笔记是由笔记本决定的，默认为笔记本公开后笔记本中的所有笔记都公开
        //所以此时判定使用笔记本的查看权限判定即可
        if(!notesPermissionUtil.checkNoteBookViewPermission(Long.valueOf(userId), notebook.getId(), reqResource)){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        //查找笔记本所有的笔记
        List<NotebookNote> notebookNotes = noteBookNoteDao.selectList(new LambdaQueryWrapper<NotebookNote>().eq(NotebookNote::getNotebookId, id));
        List<Long> list = notebookNotes.stream().map(notebookNote -> notebookNote.getNoteId()).toList();
        if(list.size()==0){
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Note> in = new LambdaQueryWrapper<Note>().in(list.size() != 0, Note::getId, list);
        List<Note> notes = noteDao.selectList(in);
        List<NoteVo> noteVos = BeanUtils.copyList(notes, NoteVo.class);
        List<NoteVo> list1 = noteVos.stream().map(noteVo -> {
            String name = userDao.selectById(noteVo.getUser()).getName();
            noteVo.setUser(name!=null?name:"未知用户");
            return noteVo;
        }).toList();
        return list1;
    }



    //专门用于内容的修改使用，不涉及其它的，可以给笔记或者随笔使用
    @Override
    public void addNoteContent(NoteContentDto noteContentDto) {
        String userId = UserUtil.getUserId();
        //首先查询该笔记或随笔是否存在
        Note note = noteDao.selectById(noteContentDto.getId());
        if(note==null||note.getType()==1){
            throw new TException(Exceptions.NOTE_IS_NOT_FOUND);
        }
        //查询是否具有相关权限,这里还需要进行修改，因为笔记和笔记本不同，
        if(!notesPermissionUtil.checkNoteEditPermission(Long.valueOf(userId), note.getId())){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        //随后进行笔记内容的修改即可
        Object o = noteContentDto.getContent().get("blocks");
        NoteContent noteContent = new NoteContent();
        noteContent.setContent(o);
        noteContent.setId(note.getContentId());
        noteContentDao.save(noteContent);
    }

    /**
     * 由于业务问题，获取笔记与获取随笔选择分开
     * @param id
     * @param reqReSource 1为新世界 2为笔记模块
     * @return
     */
    @Override
    public NoteVo getNote(Long id,Integer reqReSource) {
        //获取笔记
        String userId = UserUtil.getUserId();
        Note note = noteDao.selectById(id);
        //权限检查
        if(note==null||note.getType()==1){
            throw new TException(Exceptions.NOTE_IS_NOT_FOUND);
        }
        //此时需要判定是新世界还是笔记模块
        if(!notesPermissionUtil.checkNoteViewPermission(Long.valueOf(userId), note.getId(), reqReSource)){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        //内容转换
        NoteVo noteVo = BeanUtils.copyBean(note, NoteVo.class);
        noteVo.setUser(userDao.selectById(note.getUser()).getName());
        Optional<NoteContent> byId = noteContentDao.findById(note.getContentId());
        NoteContent noteContent = byId.get();
        noteVo.setContent(noteContent.getContent());
        noteVo.setPermission(notesPermissionUtil.getNotePermission(Long.valueOf(userId), note.getId()));
        return noteVo;
    }

    /**
     * 将获取笔记协作者的业务分离出来
     * @param id
     * @return
     */
    public List<CoopUserVo> getNoteCoopUser(Long id) {
        List<CoopUserVo> list = new ArrayList<>();
        String userId = UserUtil.getUserId();
        Note note = noteDao.selectById(id);
        if(note==null||note.getType()==1){
            throw new TException(Exceptions.NOTE_IS_NOT_FOUND);
        }
        //随后获取,这里还有权限冗余的问题，比如在表中可能同时存在不同的权限，此时只需要找出最高权限即可
        List<NotePermission> notePermissions = notePermissionDao.selectList(new LambdaQueryWrapper<NotePermission>().eq(NotePermission::getNoteId, id));
        if(notePermissions.size()!=0){
            List<CoopUserVo> list1 = notePermissions.stream().map(
                    notePermission -> {
                        CoopUserVo coopUserVo = new CoopUserVo();
                        coopUserVo.setUserId(notePermission.getUserId());
                        coopUserVo.setPermission(notePermission.getPermission());
                        coopUserVo.setResource(2);
                        User user = userDao.selectById(notePermission.getUserId());
                        coopUserVo.setUserName(user.getName());
                        coopUserVo.setAvatar(user.getAvatar());
                        return coopUserVo;
                    }
            ).toList();
            list.addAll(list1);
        }

        //获取笔记本的
        List<NotebookNote> notebookNotes = noteBookNoteDao.selectList(new LambdaQueryWrapper<NotebookNote>().eq(NotebookNote::getNoteId, id));
        List<NoteBookPermission> noteBookPermissions = noteBookPermissionDao.selectList(new LambdaQueryWrapper<NoteBookPermission>().eq(NoteBookPermission::getNotebookId, notebookNotes.get(0).getNotebookId()));
        if(noteBookPermissions.size()!=0){
            List<CoopUserVo> list2 = noteBookPermissions.stream().map(
                    noteBookPermission -> {
                        CoopUserVo coopUserVo = new CoopUserVo();
                        coopUserVo.setUserId(noteBookPermission.getUserId());
                        coopUserVo.setPermission(noteBookPermission.getPermission());
                        coopUserVo.setResource(1);
                        User user = userDao.selectById(noteBookPermission.getUserId());
                        coopUserVo.setUserName(user.getName());
                        coopUserVo.setAvatar(user.getAvatar());
                        return coopUserVo;
                    }
            ).toList();
            list.addAll(list2);
        }
        return list;
    }

    /**
     * 进行笔记的权限修改
     * @param permissionDto
     */
    @Override
    public void changePermission(PermissionDto permissionDto) {
        String userId = UserUtil.getUserId();
        Note note = noteDao.selectById(permissionDto.getId());
        if(note==null||note.getType()==1){
            throw new TException(Exceptions.NOTE_IS_NOT_FOUND);
        }
        //权限检查
        if(!note.getUser().equals(Long.valueOf(userId))){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        //权限修改
        //先检查权限是否正确
        if(!(permissionDto.getPermission()==1||permissionDto.getPermission()==2)){
            throw new TException(Exceptions.PARAM_ERROR);
        }
        List<NotePermission> notePermissions = notePermissionDao.selectList(new LambdaQueryWrapper<NotePermission>().eq(NotePermission::getNoteId, permissionDto.getId()).eq(NotePermission::getUserId, permissionDto.getUserId()));
        //删除权限
        if(notePermissions.size()!=0){
            notePermissionDao.delete(new LambdaQueryWrapper<NotePermission>().eq(NotePermission::getNoteId, permissionDto.getId()).eq(NotePermission::getUserId, permissionDto.getUserId()));
        }
            //增加权限
            NotePermission notePermission = new NotePermission();
            notePermission.setNoteId(permissionDto.getId());
            notePermission.setUserId(permissionDto.getUserId());
            notePermission.setPermission(permissionDto.getPermission());

            notePermissionDao.insert(notePermission);

    }

    @Override
    public void deletePermission(Long userId, Long noteId) {
        String loginId = UserUtil.getUserId();
        Note note = noteDao.selectById(noteId);
        if(note==null||note.getType()==1){
            throw new TException(Exceptions.NOTE_IS_NOT_FOUND);
        }
        if(!note.getUser().equals(Long.valueOf(loginId))){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        notePermissionDao.delete(new LambdaQueryWrapper<NotePermission>().eq(NotePermission::getNoteId, noteId).eq(NotePermission::getUserId, userId));
    }

    @Override
    public List<NoteVo> getCoopNote() {
        String loginId = UserUtil.getUserId();
        List<NotePermission> notePermissions = notePermissionDao.selectList(new LambdaQueryWrapper<NotePermission>().eq(NotePermission::getUserId, loginId));
        if(notePermissions.size()!=0){
            List<NoteVo> list = notePermissions.stream().map(
                    notePermission -> {
                        Note note = noteDao.selectById(notePermission.getNoteId());
                        NoteVo noteVo = BeanUtils.copyBean(note, NoteVo.class);
                        noteVo.setUser(userDao.selectById(note.getUser()).getName());
                        return noteVo;
                    }
            ).toList();
            return list;
        }else{
            return Collections.emptyList();
        }
    }

    @Override
    public void updateNote(NoteDto noteDto) {
        //首选查询改笔记是否存在，还要查询是否具有编辑权限
        String userId = UserUtil.getUserId();
        Note note = noteDao.selectById(noteDto.getId());
        if(note==null||note.getType()==1){
            throw new TException(Exceptions.NOTE_IS_NOT_FOUND);
        }
        //具有编辑权限的有笔记的编辑者和笔记本的编辑者
        if(!notesPermissionUtil.checkNoteEditPermission(Long.valueOf(userId), note.getId())){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        UpdateWrapper<Note> set = new UpdateWrapper<Note>().set(StringUtils.hasText(noteDto.getTitle()), "title", noteDto.getTitle()).set(StringUtils.hasText(noteDto.getDescription()), "description", noteDto.getDescription()).eq("id", noteDto.getId());
        noteDao.update(null,set);
    }

    @Override
    public void deleteJoinNote(Long id) {
        String userId = UserUtil.getUserId();
        Note note = noteDao.selectById(id);
        if(note==null||note.getType()==1){
            throw new TException(Exceptions.NOTE_IS_NOT_FOUND);
        }
        List<NotePermission> notePermissions = notePermissionDao.selectList(new LambdaQueryWrapper<NotePermission>().eq(NotePermission::getNoteId, id).eq(NotePermission::getUserId, userId));
        if(notePermissions.size()!=0){
            //直接删除即可
            notePermissionDao.delete(new LambdaQueryWrapper<NotePermission>().eq(NotePermission::getNoteId, id).eq(NotePermission::getUserId, userId));
        }else{
            throw new TException(Exceptions.NOTE_IS_NOT_FOUND);
        }
    }


    @Override
    public Long addNote(NoteDto noteDto) {
        String userId = UserUtil.getUserId();
        //增加笔记
        //首先查询指定笔记本是否存在
        Notebook notebook = noteBookDao.selectById(noteDto.getNotebookId());
        if(notebook==null){
            throw new TException(Exceptions.NOTEBOOK_IS_NOT_FOUND);
        }
        //随后查看是否具有权限新增笔记
        if(!notesPermissionUtil.checkNoteBookEditPermission(Long.valueOf(userId), notebook.getId())){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        //最后再进行类型转换
        Note note = BeanUtil.copyProperties(noteDto, Note.class);
        note.setUser(Long.valueOf(userId));
        note.setType(0);
        //笔记刚开始建立时默认设置为私有，不公开的形式
        note.setStatus(0);
        note.setVisible(0);
        //笔记描述，暂时为空
        if(!StringUtils.hasText(note.getDescription())){
            note.setDescription("用户暂时没有留下描述");
        }
        //查看笔记的标题是否存在
        if(!StringUtils.hasText(note.getTitle())){
            note.setTitle("无标题笔记");
        }
        NoteContent noteContent = new NoteContent();
        //为新增加的笔记或随笔添加空内容
        noteContentDao.save(noteContent);
        note.setContentId(noteContent.getId());
        noteDao.insert(note);
        //插入笔记本笔记对应表
        NotebookNote notebookNote = new NotebookNote();
        notebookNote.setNoteId(note.getId());
        notebookNote.setNotebookId(noteDto.getNotebookId());
        noteBookNoteDao.insert(notebookNote);
        return note.getId();
    }

    /**
     * 获取分享码，能够实现分享码的生成的只有，1.可编辑 2.本人 3.笔记本的编辑者或管理者
     * @param notesShareDto
     * @return
     */
    @Override
    public String getShareCode(NotesShareDto notesShareDto) {
        //分享链接
        //首先获取用户的id
        String loginId = UserUtil.getUserId();
        //还需要查询笔记是否存在
        Note note = noteDao.selectById(notesShareDto.getId());
        if(note==null||note.getType()==1){
            throw new TException(Exceptions.NOTE_IS_NOT_FOUND);
        }
        //查看用户分享的是否是笔记只有笔记才可以进行分享
        if(!note.getType().equals(0)){
            throw new TException(Exceptions.NOTE_IS_NOT_FOUND);
        }
        //查看用户是否具有相关的权限
        if(!notesPermissionUtil.checkNoteSharePermission(Long.valueOf(loginId),notesShareDto.getId())){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        //具有权限后可进行链接分享
        NoteShare noteShare = new NoteShare();
        noteShare.setUserId(Long.valueOf(loginId));
        noteShare.setNoteId(notesShareDto.getId());
        if(notesShareDto.getType().equals(1)){
            noteShare.setType(PermissionValue.ONLY_READ);
        }else if(notesShareDto.getType().equals(2)){
            noteShare.setType(PermissionValue.READ_WRITE);
        }else{
            throw new TException(Exceptions.PARAM_ERROR);
        }
        //生成分享码
        String code = RandomUtil.randomString(8);
        noteShare.setCode(code);
        //如果有密码就设置密码
        if(StringUtils.hasText(notesShareDto.getPassword())){
            noteShare.setPassword(notesShareDto.getPassword());
        }
        noteShareDao.insert(noteShare);
        return code;
    }

    @Override
    public void authCode(NotesCodeAuthDto code) {
        String loginId = UserUtil.getUserId();
        //首先根据分享码去查询分享链接中查询
        LambdaQueryWrapper<NoteShare> eq = new LambdaQueryWrapper<NoteShare>().eq(NoteShare::getCode, code.getCode());
        NoteShare noteShare = noteShareDao.selectOne(eq);
        if(noteShare==null){
            //如果不存在，就是分享码错误
            throw new TException(Exceptions.SHARE_CODE_IS_NOT_FOUND);
        }
        //如果用户已经具有了相关权限就不需要再次插入直接返回即可,防止重复添加
        LambdaQueryWrapper<NotePermission> eq1 = new LambdaQueryWrapper<NotePermission>().eq(NotePermission::getUserId, Long.valueOf(loginId)).eq(NotePermission::getNoteId, noteShare.getNoteId());
        List<NotePermission> notePermissions = notePermissionDao.selectList(eq1);
        for(NotePermission notePermission : notePermissions){
            if(notePermission.getPermission().equals(noteShare.getType()))
                return;
        }
        //首先查看是否设置密码，后期考虑加密
        if(StringUtils.hasText(noteShare.getPassword())){
            if(!noteShare.getPassword().equals(code.getPassword())){
                throw new TException(Exceptions.PASSWORD_ERROR);
            }
        }
        //验证通过后就对该用户进行添加

        NotePermission notePermission = new NotePermission();
        //设置该用户的权限
        notePermission.setPermission(noteShare.getType());
        notePermission.setUserId(Long.valueOf(loginId));
        notePermission.setNoteId(noteShare.getNoteId());
        //插入
        notePermissionDao.insert(notePermission);
    }

    @Override
    public void deleteNote(Long id) {
        String loginId = UserUtil.getUserId();
        Note note = noteDao.selectById(id);
        if(note == null){
            throw new TException(Exceptions.NOTE_IS_NOT_FOUND);
        }
        //查看类型是笔记还是随笔
        if(note.getType().equals(0)){
            //检查是否具有权限，只有笔记本的编辑者及以上才可以对笔记进行删除
            NotebookNote notebookNote = noteBookNoteDao.selectOne(new LambdaQueryWrapper<NotebookNote>().eq(NotebookNote::getNoteId, id));
            if(notebookNote==null){
                throw new TException(Exceptions.NOTE_IS_NOT_FOUND);
            }
            if(!notesPermissionUtil.checkNoteBookEditPermission(Long.valueOf(loginId), notebookNote.getNotebookId())){
                throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
            }
        }else{
            //作为随笔，只有自己可以删除
            if(!note.getUser().equals(Long.valueOf(loginId))){
                throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
            }
        }
        //最后可以进行删除，但是删除时不应该只删除这些，还需要删除笔记自己携带的一些信息
        if(note.getType().equals(0)){
            //首先删除笔记和笔记本的对应关系
            noteBookNoteDao.delete(new LambdaQueryWrapper<NotebookNote>().eq(NotebookNote::getNoteId, id));
            //删除笔记的内容
            noteContentDao.deleteById(note.getContentId());
            //删除笔记的相关权限
            notePermissionDao.delete(new LambdaQueryWrapper<NotePermission>().eq(NotePermission::getNoteId, id));
            //删除笔记的分享连接
            noteShareDao.delete(new LambdaQueryWrapper<NoteShare>().eq(NoteShare::getNoteId, id));
        }
        noteDao.deleteById(id);
    }
}
