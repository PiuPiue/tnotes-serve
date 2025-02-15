package com.hao.tnotes.common.util.noteutil;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hao.tnotes.common.bean.domain.*;
import com.hao.tnotes.note.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 笔记模块权限判定的一个工具类,
 * 根据不同的id等来判断是否具有相关权限
 * 具体需要权限判定的有：
 * 1.笔记本：查看 收藏 修改 删除给笔记本增加笔记
 * 2.笔记：查看 收藏 修改 删除
 */
@Component
public class NotesPermissionUtil {

    @Autowired
    private NoteBookDao noteBookDao;
    @Autowired
    private NoteDao noteDao;
    @Autowired
    private NoteBookPermissionDao noteBookPermissionDao;
    @Autowired
    private NotePermissionDao notePermissionDao;
    @Autowired
    private NoteBookNoteDao noteBookNoteDao;

    /**
     * 判断用户对笔记本的查看权限
     * 也就是说不管是那种方式访问这个文档，最后都需要调用查看文档的这个方法，方法是通用的
     * 但是具有不同的情况，这两种情况的前端界面是不同的
     * 不过是由前端界面来进行控制，当新世界没有开时，用户通过新世界查看，能不能查看，肯定是不能，所以还需要改造后端
     * 1.新世界查看时
     * 2.通过链接查看时
     * 刚开始时，用户点击生成链接，可以由前生成端链接，然后存储进后端，后端存储完成
     * 还有就是链接的验证，设计思路，当前端生成分享链接时，这个链接指向的是一个链接界面，需不需要密码，所以这个界面是一定需要的
     * 这个界面会自动像后端发送请求，请求的内容就是一个token，这个token记录在数据库中，验证这个token，然后根据这个token、密码来
     * 将该用户存储权限
     * 当二次进入时，还需不需要这个链接或者后端直接返回一个可以进入，或者是重定向就可以，需不需要判断或者说是
     * 随后后端进行一个类似于判断的东西或者还是什么
     * @param userId 要查看的用户id
     * @param notebookId 笔记本id
     * @param reqResource 请求资源类型，1表示新世界查看，2表示通过链接查看
     * @return
     */
    public boolean checkNoteBookViewPermission(Long userId,Long notebookId,Integer reqResource){
        //首先根据笔记本的id找出笔记本
        Notebook notebook = noteBookDao.selectById(notebookId);
        //请求为新世界
        if(reqResource==1){
            if(notebook.getVisible().equals(PermissionValue.VISIBLE))
                return true;
            else
                return false;
        }
        //请求为协作
        //如果是自己就直接查看
        if(notebook.getUser().equals(userId)){
            return true;
        }
        //如果是他人，需要查看此人有没有权限，需要得出一个最高权限作为返回的目标
        //此时需要根据用户id,笔记本id进行查找，查找出相关权限
        List<NoteBookPermission> noteBookPermissions = noteBookPermissionDao.selectList(new LambdaQueryWrapper<NoteBookPermission>().eq(NoteBookPermission::getUserId, userId).eq(NoteBookPermission::getNotebookId, notebookId));
        //如果没有查找到任何权限
        if(noteBookPermissions.size()==0){
            return false;
        }
        //因为最低权限就是只读，所以一定存在
        return true;
    }

    /**
     * 查看用户是否具有笔记本的编辑权限：包括1.查看和编辑笔记本以及其中的所有文章
     * @param userId
     * @param notebookId
     * @return
     */
    public boolean checkNoteBookEditPermission(Long userId,Long notebookId){
        //首先查找出此笔记本
        Notebook notebook = noteBookDao.selectById(notebookId);
        //此人是本人，具有最大权限
        if(notebook.getUser().equals(userId)){
            return true;
        }
        List<NoteBookPermission> noteBookPermissions = noteBookPermissionDao.selectList(new LambdaQueryWrapper<NoteBookPermission>().eq(NoteBookPermission::getUserId, userId).eq(NoteBookPermission::getNotebookId, notebookId));
        for (NoteBookPermission noteBookPermission : noteBookPermissions){
            if(noteBookPermission.getPermission()>=PermissionValue.READ_WRITE){
                return true;
            }
        }
        return false;
    }

    /**
     * 查看用户是否具有笔记本的管理权限
     * @param userId
     * @param notebookId
     * @return
     */
    public boolean checkNoteBookAdminPermission(Long userId,Long notebookId){
        //首先查找出此笔记本
        Notebook notebook = noteBookDao.selectById(notebookId);
        //此人是本人，具有最大权限
        if(notebook.getUser().equals(userId)){
            return true;
        }
        List<NoteBookPermission> noteBookPermissions = noteBookPermissionDao.selectList(new LambdaQueryWrapper<NoteBookPermission>().eq(NoteBookPermission::getUserId, userId).eq(NoteBookPermission::getNotebookId, notebookId));
        for (NoteBookPermission noteBookPermission : noteBookPermissions){
            if(noteBookPermission.getPermission()>=PermissionValue.ADMIN){
                return true;
            }
        }
        return false;
    }

    public Integer getNotebookPermission(Long userId,Long notebookId){
        //首先查找出此笔记本
        Notebook notebook = noteBookDao.selectById(notebookId);
        //此人是本人，具有最大权限
        if(notebook.getUser().equals(userId)){
            return PermissionValue.MASTER_ADMIN;
        }
        //需要在这里找出最大权限
        Integer permission = PermissionValue.ONLY_READ;
        List<NoteBookPermission> noteBookPermissions = noteBookPermissionDao.selectList(new LambdaQueryWrapper<NoteBookPermission>().eq(NoteBookPermission::getUserId, userId).eq(NoteBookPermission::getNotebookId, notebookId));
        for(NoteBookPermission noteBookPermission : noteBookPermissions){
            permission = Math.max(permission,noteBookPermission.getPermission());
        }
        return permission;
    }

    /**
     * 检查笔记阅读权限,此时有一个问题，涉及到是从专栏中查找还是从笔记模块查找的问题
     * 现在默认情况，用户公开笔记本后，所有的笔记都是可以被查看的，所以按照现在的思路来看
     * @param userId
     * @param noteId
     * @param reqResource
     * @return
     */
    public boolean checkNoteViewPermission(Long userId,Long noteId,Integer reqResource){
        //首先根据笔记本的id找出笔记本
        Note note = noteDao.selectById(noteId);
        //此时为互联网公开的状态，互联网公开时1.自己本身公开2.笔记本公开
        if(reqResource==1){
            if(note.getVisible().equals(PermissionValue.VISIBLE))
                return true;
            //此时要查看该笔记的笔记本
            LambdaQueryWrapper<NotebookNote> eq = new LambdaQueryWrapper<NotebookNote>().eq(NotebookNote::getNoteId, noteId);
            NotebookNote notebookNote = noteBookNoteDao.selectOne(eq);
            //调用本地方法
            return this.checkNoteBookViewPermission(userId,notebookNote.getNotebookId(),1);
        }
        //如果是自己就直接查看
        if(note.getUser().equals(userId)){
            return true;
        }
        //如果是他人，需要查看此人有没有权限，需要得出一个最高权限作为返回的目标
        List<NotePermission> notePermissions = notePermissionDao.selectList(new LambdaQueryWrapper<NotePermission>().eq(NotePermission::getUserId, userId).eq(NotePermission::getNoteId, noteId));
        for(NotePermission notePermission : notePermissions){
            if(notePermission.getPermission()>=PermissionValue.ONLY_READ){
                return true;
            }
        }
        //如果还是没有，查看该笔记的
        LambdaQueryWrapper<NotebookNote> eq = new LambdaQueryWrapper<NotebookNote>().eq(NotebookNote::getNoteId, noteId);
        NotebookNote notebookNote = noteBookNoteDao.selectOne(eq);
        return this.checkNoteBookViewPermission(userId,notebookNote.getNotebookId(),2);
    }

    /**
     * 查看笔记编辑权限，主要有笔记本身元数据的编辑和笔记中内容的编辑
     * @param userId
     * @param noteId
     * @return
     */
    public boolean checkNoteEditPermission(Long userId,Long noteId){
        //首先根据笔记本的id找出笔记本
        Note note = noteDao.selectById(noteId);
        //如果本人就是作者，具有最高权限
        if(note.getUser().equals(userId)){
            return true;
        }
        //查看该笔记的权限
        List<NotePermission> notePermissions = notePermissionDao.selectList(new LambdaQueryWrapper<NotePermission>().eq(NotePermission::getUserId, userId).eq(NotePermission::getNoteId, noteId));
        for(NotePermission notePermission : notePermissions){
            if(notePermission.getPermission()>=PermissionValue.READ_WRITE){
                return true;
            }
        }
        //查看笔记本的权限
        LambdaQueryWrapper<NotebookNote> eq = new LambdaQueryWrapper<NotebookNote>().eq(NotebookNote::getNoteId, noteId);
        NotebookNote notebookNote = noteBookNoteDao.selectOne(eq);
        return this.checkNoteBookEditPermission(userId,notebookNote.getNotebookId());
    }

    public boolean checkNoteBookSharePermission(Long userId,Long notebookId){
        //首先根据笔记本的id找出笔记本
        Notebook notebook = noteBookDao.selectById(notebookId);
        if(notebook.getUser().equals(userId))
            return true;
        //然后查看是否为笔记本的管理人员
        return this.checkNoteBookAdminPermission(userId, notebookId);
    }

    public boolean checkNoteSharePermission(Long userId,Long noteId){
        //查找出笔记
        Note note = noteDao.selectById(noteId);
        //本人具有最高权限
        if(note.getUser().equals(userId)){
            return true;
        }
        //如果不是本人，只有笔记本的编辑以上权限可以实现生成
        NotebookNote notebookNote = noteBookNoteDao.selectOne(new LambdaQueryWrapper<NotebookNote>().eq(NotebookNote::getNoteId, noteId));
        return checkNoteBookEditPermission(userId, notebookNote.getNotebookId());
    }

    public boolean isPermissionValue(Integer permission) {
        if(permission>=1&&permission<=3)
            return true;
        return false;
    }

    public Integer getNotePermission(Long userId,Long noteId){
        Note note = noteDao.selectById(noteId);
        if(note.getUser().equals(userId)){
            return PermissionValue.READ_WRITE;
        }
        List<NotePermission> notePermissions = notePermissionDao.selectList(new LambdaQueryWrapper<NotePermission>().eq(NotePermission::getUserId, userId).eq(NotePermission::getNoteId, noteId));

        //这里有不同的情况还有一种就是没有权限，但是没有权限时不会到这一层后续待修改
        for(NotePermission notePermission : notePermissions){
            if(notePermission.getPermission()>=PermissionValue.READ_WRITE){
                return notePermission.getPermission();
            }
        }
        NotebookNote notebookNote = noteBookNoteDao.selectOne(new LambdaQueryWrapper<NotebookNote>().eq(NotebookNote::getNoteId, noteId));
        if(this.checkNoteBookEditPermission(userId, notebookNote.getNotebookId())){
            return PermissionValue.READ_WRITE;
        }
        return PermissionValue.ONLY_READ;
    }
}
