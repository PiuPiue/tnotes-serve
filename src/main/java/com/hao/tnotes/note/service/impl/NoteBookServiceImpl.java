package com.hao.tnotes.note.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hao.tnotes.common.bean.domain.*;
import com.hao.tnotes.common.bean.dto.NoteBookDto;
import com.hao.tnotes.common.bean.dto.NotesCodeAuthDto;
import com.hao.tnotes.common.bean.dto.NotesShareDto;
import com.hao.tnotes.common.bean.dto.PermissionDto;
import com.hao.tnotes.common.bean.vo.NoteBookVo;
import com.hao.tnotes.common.bean.vo.UserVo;
import com.hao.tnotes.common.util.bean.BeanUtils;
import com.hao.tnotes.common.util.common.Exceptions;
import com.hao.tnotes.common.util.common.TException;
import com.hao.tnotes.common.util.common.UserUtil;
import com.hao.tnotes.common.util.noteutil.NotesPermissionUtil;
import com.hao.tnotes.common.util.noteutil.PermissionValue;
import com.hao.tnotes.note.dao.*;
import com.hao.tnotes.note.service.NoteBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class NoteBookServiceImpl implements NoteBookService {


    @Autowired
    private UserNoteBookDao userNoteBookDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private NoteBookDao noteBookDao;
    @Autowired
    private NoteBookNoteDao noteBookNoteDao;
    @Autowired
    private NotesPermissionUtil notesPermissionUtil;
    @Autowired
    private NoteBookShareDao noteBookShareDao;
    @Autowired
    private NoteBookPermissionDao noteBookPermissionDao;

    @Override
    public List<NoteBookVo> list() {
        //首先获取用户的id
        String loginId = UserUtil.getUserId();
        //定义一个返回集合
        List<NoteBookVo> list = new ArrayList<>();
        //查看用户自己的笔记本
        LambdaQueryWrapper<Notebook> eq = new LambdaQueryWrapper<Notebook>().eq(Notebook::getUser, loginId);
        List<Notebook> myNotebooks = noteBookDao.selectList(eq);
        if(myNotebooks.size()!=0){
            List<NoteBookVo> myNotebookVos = BeanUtils.copyList(myNotebooks, NoteBookVo.class);
            List<NoteBookVo> list1 = myNotebookVos.stream().map(notebookvo -> {
//                String name = userDao.selectById(notebookvo.getUser()).getName();
//                notebookvo.setUser(name != null ? name : "未知用户");
                notebookvo.setResType(1);
                notebookvo.setPermission(PermissionValue.MASTER_ADMIN);
                notebookvo.setNoteCount(noteBookNoteDao.selectCount(new LambdaQueryWrapper<NotebookNote>().eq(NotebookNote::getNotebookId, notebookvo.getId())));
                return notebookvo;
            }).toList();
            list.addAll(list1);
        }

        //查看用户收藏的笔记本
        LambdaQueryWrapper<UserNoteBook> eq1 = new LambdaQueryWrapper<UserNoteBook>().eq(UserNoteBook::getUserId, loginId);
        List<UserNoteBook> collectedNotebooks = userNoteBookDao.selectList(eq1);
        if(collectedNotebooks.size()!=0){LambdaQueryWrapper<Notebook> in1 = new LambdaQueryWrapper<Notebook>().in(Notebook::getId, collectedNotebooks.stream().map(UserNoteBook::getNotebookId).toList());
            List<Notebook> collectedNotebooks1 = noteBookDao.selectList(in1);
            List<NoteBookVo> collectedNotebookVos = BeanUtils.copyList(collectedNotebooks1, NoteBookVo.class);
            List<NoteBookVo> list2 = collectedNotebookVos.stream().map(notebookvo -> {
//                String name = userDao.selectById(notebookvo.getUser()).getName();
//                notebookvo.setUser(name != null ? name : "未知用户");
                //需要先去查看权限
                Integer permission = notesPermissionUtil.getNotebookPermission(Long.valueOf(loginId), notebookvo.getId());
                notebookvo.setPermission(permission);
                notebookvo.setResType(3);
                notebookvo.setNoteCount(noteBookNoteDao.selectCount(new LambdaQueryWrapper<NotebookNote>().eq(NotebookNote::getNotebookId, notebookvo.getId())));
                return notebookvo;
            }).toList();
            list.addAll(list2);
        }

        //查找用户协作的笔记本
        LambdaQueryWrapper<NoteBookPermission> eq2 = new LambdaQueryWrapper<NoteBookPermission>().eq(NoteBookPermission::getUserId, loginId);
        List<NoteBookPermission> noteBookPermissions = noteBookPermissionDao.selectList(eq2);
        //首先进行权限去重，里面可能存在一样的笔记本
        List<Long> list3 = noteBookPermissions.stream().map(noteBookPermission -> {
            return noteBookPermission.getNotebookId();
        }).distinct().toList();
        if(list3.size()!=0){
            LambdaQueryWrapper<Notebook> in = new LambdaQueryWrapper<Notebook>().in(list3.size()!=0,Notebook::getId, list3);
            List<Notebook> joinedNotebooks = noteBookDao.selectList(in);
            List<NoteBookVo> joinedNotebookVos = BeanUtils.copyList(joinedNotebooks, NoteBookVo.class);
            List<NoteBookVo> list4 = joinedNotebookVos.stream().map(notebookvo -> {
//                String name = userDao.selectById(notebookvo.getUser()).getName();
//                notebookvo.setUser(name != null ? name : "未知用户");
                Integer permission = notesPermissionUtil.getNotebookPermission(Long.valueOf(loginId), notebookvo.getId());
                notebookvo.setPermission(permission);
                notebookvo.setResType(2);
                notebookvo.setNoteCount(noteBookNoteDao.selectCount(new LambdaQueryWrapper<NotebookNote>().eq(NotebookNote::getNotebookId, notebookvo.getId())));
                return notebookvo;
            }).toList();
            list.addAll(list4);
        }

        if(list.size()==0){
            return Collections.emptyList();
        }
        return list;
    }

    @Override
    public void addOrUpdateNoteBook(NoteBookDto noteBookDto) {
        //获取用户id
        String loginId = UserUtil.getUserId();
        if(noteBookDto.getId() == null){
            //新增
            //先查看用户是否是vip
            Integer isVip = userDao.selectById(loginId).getIsVip();
            Integer integer = noteBookDao.selectCount(new LambdaQueryWrapper<Notebook>().eq(Notebook::getUser, loginId));
            if(isVip==0&&integer>=5){
                throw new TException(Exceptions.USER_IS_NOT_VIP);
            }
            Notebook notebook = BeanUtils.copyBean(noteBookDto, Notebook.class);
            notebook.setUser(Long.valueOf(loginId));
            if(!StringUtils.hasText(notebook.getDescription())){
                notebook.setDescription("这个人很懒，什么都没有留下~~~");
            }
            noteBookDao.insert(notebook);
        }else{
            //修改
            //首先需要查看此人是否具有相关权限
            Notebook notebook = noteBookDao.selectById(noteBookDto.getId());
            if(notebook==null){
                throw new TException(Exceptions.NOTEBOOK_IS_NOT_FOUND);
            }
            //权限判定
            boolean b = notesPermissionUtil.checkNoteBookEditPermission(Long.valueOf(loginId), noteBookDto.getId());
            if(!b){
                throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
            }
            Notebook notebook1 = BeanUtils.copyBean(noteBookDto, Notebook.class);
            noteBookDao.updateById(notebook1);
        }
    }

    @Override
    public void deleteNoteBook(Long id,Integer deleteType) {
        //根据不同情况进行调用即可
        if(deleteType == 1){
            //逻辑删除
            deleteNoteBook1(id);
        }else if(deleteType == 2){
            this.deleteJoinedNoteBook(id);
        }else if(deleteType == 3){
            this.deleteCollectedNoteBook(id);
        }else{
            throw new TException(Exceptions.PARAM_ERROR);
        }

    }

    /**
     * 删除笔记本，是真正的逻辑删除
     * @param id
     */
    private void deleteNoteBook1(Long id){
        String loginId = UserUtil.getUserId();
        Notebook notebook = noteBookDao.selectById(id);
        if(notebook==null){
            throw new TException(Exceptions.NOTEBOOK_IS_NOT_FOUND);
        }
        //检查用户是否具有权限,对于笔记本的删除
        if(!notebook.getUser().equals(Long.valueOf(loginId))){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        //检查笔记本中是否还具有笔记
        List<NotebookNote> notebookNotes = noteBookNoteDao.selectList(new LambdaQueryWrapper<NotebookNote>().eq(NotebookNote::getNotebookId, id));
        if(notebookNotes.size()!=0){
            throw new TException(Exceptions.NOTEBOOK_IS_NOT_EMPTY);
        }
        //删除笔记本的被收藏记录
        userNoteBookDao.delete(new LambdaQueryWrapper<UserNoteBook>().eq(UserNoteBook::getNotebookId, id));
        //删除笔记本
        noteBookDao.deleteById(id);
    }

    /**
     * 取消收藏的删除而不是真正删除
     * @param id
     */
    private void deleteCollectedNoteBook(Long id){
        String loginId = UserUtil.getUserId();
        Notebook notebook = noteBookDao.selectById(id);
        if(notebook==null){
            throw new TException(Exceptions.NOTEBOOK_IS_NOT_FOUND);
        }
        userNoteBookDao.delete(new LambdaQueryWrapper<UserNoteBook>().eq(UserNoteBook::getNotebookId, id).eq(UserNoteBook::getUserId, loginId));
    }

    /**
     * 取消协作当前的笔记本
     * @param id
     */
    private void deleteJoinedNoteBook(Long id){
        String loginId = UserUtil.getUserId();
        Notebook notebook = noteBookDao.selectById(id);
        if(notebook==null){
            throw new TException(Exceptions.NOTEBOOK_IS_NOT_FOUND);
        }
        noteBookPermissionDao.delete(new LambdaQueryWrapper<NoteBookPermission>().eq(NoteBookPermission::getNotebookId, id).eq(NoteBookPermission::getUserId, loginId));
    }

    @Override
    public NoteBookVo getNoteBook(Long id,Integer reqResource) {
        //首先获取用户的id
        String loginId = UserUtil.getUserId();
        //查看用户是否具有相关的权限
        Notebook notebook = noteBookDao.selectById(id);
        if(notebook==null){
            throw new TException(Exceptions.NOTEBOOK_IS_NOT_FOUND);
        }
        //权限判定
        boolean b = notesPermissionUtil.checkNoteBookViewPermission(Long.valueOf(loginId), id, reqResource);
        if(!b){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        //这里加一个拥有权限
        Integer permission = notesPermissionUtil.getNotebookPermission(Long.valueOf(loginId), id);

        //有权限之后在返回内容
        NoteBookVo noteBookVo = BeanUtils.copyBean(notebook, NoteBookVo.class);
        noteBookVo.setUser(userDao.selectById(notebook.getUser()).getName());
        noteBookVo.setPermission(permission);
        //获取笔记的协作者列表
        List<UserVo> coopUser = noteBookPermissionDao.selectList(new LambdaQueryWrapper<NoteBookPermission>().eq(NoteBookPermission::getNotebookId, id)).stream().map(noteBookPermission -> {
            User user = userDao.selectById(noteBookPermission.getUserId());
            UserVo userVo = BeanUtils.copyBean(user, UserVo.class);
            userVo.setCoopType(noteBookPermission.getPermission());
            return userVo;
        }).distinct().toList();
        noteBookVo.setCoopUser(coopUser);
        noteBookVo.setNoteCount( noteBookNoteDao.selectCount(new LambdaQueryWrapper<NotebookNote>().eq(NotebookNote::getNotebookId, id)));
        return noteBookVo;
    }

    /**
     * 能够生成分享链接的只有自己和笔记本的管理人员
     * @param notesShareDto
     * @return
     */
    @Override
    public String getShareCode(NotesShareDto notesShareDto) {
        //分享链接
        //首先获取用户的id
        String loginId = UserUtil.getUserId();
        //还需要查询笔记本是否存在
        Notebook notebook = noteBookDao.selectById(notesShareDto.getId());
        if(notebook==null){
            throw new TException(Exceptions.NOTEBOOK_IS_NOT_FOUND);
        }
        //查看用户是否具有相关的权限
        if(!notesPermissionUtil.checkNoteBookSharePermission(Long.valueOf(loginId),notesShareDto.getId())){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        //具有权限后可进行链接分享
        NoteBookShare noteBookShare = new NoteBookShare();
        noteBookShare.setUserId(Long.valueOf(loginId));
        noteBookShare.setNotebookId(notesShareDto.getId());
        if(notesShareDto.getType().equals(1)){
            noteBookShare.setType(PermissionValue.ONLY_READ);
        }else if(notesShareDto.getType().equals(2)){
            noteBookShare.setType(PermissionValue.READ_WRITE);
        }else if(notesShareDto.getType().equals(3)){
            noteBookShare.setType(PermissionValue.ADMIN);
        }else{
            throw new TException(Exceptions.PARAM_ERROR);
        }
        //生成分享码
        String code = RandomUtil.randomString(8);
        noteBookShare.setCode(code);
        //如果有密码就设置密码
        if(StringUtils.hasText(notesShareDto.getPassword())){
            noteBookShare.setPassword(notesShareDto.getPassword());
        }
        noteBookShareDao.insert(noteBookShare);
        return code;
    }


    @Override
    public void authCode(NotesCodeAuthDto code) {
        String loginId = UserUtil.getUserId();
        //首先根据分享码去查询分享链接中查询
        LambdaQueryWrapper<NoteBookShare> eq = new LambdaQueryWrapper<NoteBookShare>().eq(NoteBookShare::getCode, code.getCode());
        NoteBookShare noteBookShare = noteBookShareDao.selectOne(eq);
        if(noteBookShare==null){
            //如果不存在，就是分享码错误
            throw new TException(Exceptions.SHARE_CODE_IS_NOT_FOUND);
        }
        //首先查看是否设置密码，后期考虑加密
        if(StringUtils.hasText(noteBookShare.getPassword())){
            if(!noteBookShare.getPassword().equals(code.getPassword())){
                throw new TException(Exceptions.PASSWORD_ERROR);
            }
        }
        //验证通过后就对该用户进行添加
        //如果用户已经具有了相关权限就不需要再次插入直接返回即可,防止重复添加
        LambdaQueryWrapper<NoteBookPermission> eq1 = new LambdaQueryWrapper<NoteBookPermission>().eq(NoteBookPermission::getUserId, Long.valueOf(loginId)).eq(NoteBookPermission::getNotebookId, noteBookShare.getNotebookId());
        List<NoteBookPermission> noteBookPermissions = noteBookPermissionDao.selectList(eq1);
        for(NoteBookPermission noteBookPermission : noteBookPermissions){
            //此时进行判断，如果已经具有大于或者等于此权限时就不需要再次添加
            if(noteBookPermission.getPermission().equals(noteBookShare.getType()))
                return;
        }
        NoteBookPermission noteBookPermission = new NoteBookPermission();
        //设置该用户的权限
        noteBookPermission.setPermission(noteBookShare.getType());
        noteBookPermission.setUserId(Long.valueOf(loginId));
        noteBookPermission.setNotebookId(noteBookShare.getNotebookId());
        //插入
        noteBookPermissionDao.insert(noteBookPermission);
    }

    @Override
    public void changePermission(PermissionDto permissionDto) {
        String loginId = UserUtil.getUserId();
        //先查看该笔记本是否存在
        Notebook notebook = noteBookDao.selectById(permissionDto.getId());
        if(notebook==null){
            throw new TException(Exceptions.NOTEBOOK_IS_NOT_FOUND);
        }
        //查找关系
        LambdaQueryWrapper<NoteBookPermission> eq = new LambdaQueryWrapper<NoteBookPermission>().eq(NoteBookPermission::getUserId, permissionDto.getUserId()).eq(NoteBookPermission::getNotebookId, permissionDto.getId());
        List<NoteBookPermission> noteBookPermissions = noteBookPermissionDao.selectList(eq);
        if(noteBookPermissions.size()==0){
            throw new TException(Exceptions.PARAM_ERROR);
        }
        //查看当前用户是否具有权限,只有作者可以设置权限
        if(!notebook.getUser().equals(Long.valueOf(loginId))){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        //删除掉以前的权限
        noteBookPermissionDao.delete(eq);
        //新增权限
        //先检查权限值是否正确
        if(!notesPermissionUtil.isPermissionValue(permissionDto.getPermission())){
            throw new TException(Exceptions.PARAM_ERROR);
        }
        NoteBookPermission noteBookPermission = new NoteBookPermission();
        noteBookPermission.setUserId(permissionDto.getUserId());
        noteBookPermission.setNotebookId(permissionDto.getId());
        noteBookPermission.setPermission(permissionDto.getPermission());
        noteBookPermissionDao.insert(noteBookPermission);
    }

    @Override
    public void deleteCoopUser(Long userId, Long notebookId) {
        String loginId = UserUtil.getUserId();
        //先查看该笔记本是否存在
        Notebook notebook = noteBookDao.selectById(notebookId);
        if(notebook==null){
            throw new TException(Exceptions.NOTEBOOK_IS_NOT_FOUND);
        }
        //查找关系
        LambdaQueryWrapper<NoteBookPermission> eq = new LambdaQueryWrapper<NoteBookPermission>().eq(NoteBookPermission::getUserId, userId).eq(NoteBookPermission::getNotebookId, notebookId);
        List<NoteBookPermission> noteBookPermissions = noteBookPermissionDao.selectList(eq);
        if(noteBookPermissions.size()==0){
            throw new TException(Exceptions.PARAM_ERROR);
        }
        //查看当前用户是否具有权限,只有作者可以实现删除
        if(!notebook.getUser().equals(Long.valueOf(loginId))){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        //删除掉所有的权限
        noteBookPermissionDao.delete(eq);
    }


}
