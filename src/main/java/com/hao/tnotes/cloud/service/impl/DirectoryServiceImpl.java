package com.hao.tnotes.cloud.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hao.tnotes.cloud.dao.CloudFileDao;
import com.hao.tnotes.cloud.dao.DirectoryDao;
import com.hao.tnotes.cloud.dao.UserDirectoryDao;
import com.hao.tnotes.cloud.service.DirectoryService;
import com.hao.tnotes.common.bean.domain.Directory;
import com.hao.tnotes.common.bean.domain.UserDirectory;
import com.hao.tnotes.common.bean.vo.CloudInfoVo;
import com.hao.tnotes.common.bean.vo.DictVo;
import com.hao.tnotes.common.util.cloudutil.CloudPermissionUtil;
import com.hao.tnotes.common.util.common.AjaxResult;
import com.hao.tnotes.common.util.common.Exceptions;
import com.hao.tnotes.common.util.common.TException;
import com.hao.tnotes.common.util.common.UserUtil;
import com.hao.tnotes.common.util.bean.BeanUtils;
import com.hao.tnotes.common.util.redis.RedisCache;
import com.hao.tnotes.common.util.redis.RedisKeys;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DirectoryServiceImpl implements DirectoryService {

    @Autowired
    private DirectoryDao directoryDao;
    @Autowired
    private CloudFileDao cloudFileDao;
    @Autowired
    private UserDirectoryDao userDirectoryDao;
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private CloudPermissionUtil cloudPermissionUtil;
    @Autowired
    private RedisCache redisCache;

    /**
     * 获取根目录下的目录
     * @return
     */
    @Override
    public AjaxResult list() {
        //首选获取当前登录人
        String loginId = UserUtil.getUserId();
        //查询出登录人的根目录id
        UserDirectory userDirectory = userDirectoryDao.selectOne(new LambdaQueryWrapper<UserDirectory>().eq(UserDirectory::getUserId, loginId));
        List<DictVo> dictVoList = this.getDictVoList(userDirectory.getDirectoryId());
        return AjaxResult.success(dictVoList);
    }

    /**
     * 根据id获取子目录
     * @param id
     * @return
     */
    @Override
    public AjaxResult getChildDict(Long id) {
        List<DictVo> dictVoList = this.getDictVoList(id);
        return AjaxResult.success(dictVoList);
    }

    @Override
    public void createDict(Long parentId, String name) {
        String loginId = UserUtil.getUserId();
        //首先查看此文件夹是否属于用户
        Directory directory = directoryDao.selectOne(new LambdaQueryWrapper<Directory>().eq(Directory::getId, parentId));
        if (directory == null){
            throw new TException(Exceptions.DIRECTORY_IS_NOT_FOUND);
        }
        if (!cloudPermissionUtil.checkCreateDirectoryPermission(parentId)){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        //随后进行创建
        Directory directory1 = new Directory();
        directory1.setName(name);
        directory1.setUserId(Long.valueOf(loginId));
        directory1.setParentId(parentId);
        directory1.setSize(0L);
        directory1.setType(1);
        //设置父级的路径
        directory1.setPath(directory.getPath()!=null?directory.getPath():""+"/"+directory.getId());
        directoryDao.insert(directory1);
    }

    @Override
    public void deleteDict(Long id) {
        String loginId = UserUtil.getUserId();
        //首先查看此文件夹是否属于用户
        Directory directory = directoryDao.selectOne(new LambdaQueryWrapper<Directory>().eq(Directory::getId, id));
        if (directory == null){
            throw new TException(Exceptions.DIRECTORY_IS_NOT_FOUND);
        }
        //用户尝试删除根目录
        if(directory.getParentId()==null){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        if (!cloudPermissionUtil.checkDeleteDirectoryPermission(id)){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        //删除前先放入redis
        RedisKeys redisKeys = RedisKeys.generateCloudRecycleRecord(loginId,String.valueOf(id));
        redisCache.setCacheObject(redisKeys.CLOUD_RECYCLE_RECORD,directory,30, TimeUnit.DAYS);
        //随后如果是文件就可以直接删除
        if(directory.getType()==0){
            directoryDao.deleteById(id);
        }else{
            //如果是目录,就还需要查询出所有的子层,并记录到redis中
            //1.首先查询出直接子目录
            List<Directory> childDirectory = directoryDao.selectList(new LambdaQueryWrapper<Directory>().eq(Directory::getParentId, id));
            //2.查询出孙子目录
            List<Directory> directories = directoryDao.selectList(new LambdaQueryWrapper<Directory>().like(Directory::getPath, "/" + id + "/"));
            List<Long> ids = null;
            if(directories.size()!=0){
                childDirectory.addAll(directories);
            }
            if(childDirectory.size()!=0){
                //只需要记录所有id即可
                ids = childDirectory.stream().map(Directory::getId).collect(Collectors.toList());
                String directoryChildrenRecord = RedisKeys.generateDirectoryChildrenRecord(String.valueOf(id)).DIRECTORY_CHILDREN_RECORD;
                redisCache.setCacheObject(directoryChildrenRecord,ids,30,TimeUnit.DAYS);
            }
            //记录下来之后开始删除
            directoryDao.deleteById(id);
            if(ids!=null){
                directoryDao.delete(new LambdaQueryWrapper<Directory>().in(ids!=null,Directory::getId,ids));
            }

        }
        String path = directory.getPath();
        String[] split = path.split("/");
        //进行更新操作
        for(int i=0;i<split.length;i++){
            if(split[i]!="")
                this.updateUserDirectorySize(Long.valueOf(split[i]),directory.getSize(),0);
        }
        directoryDao.deleteById(id);
    }
    private void updateUserDirectorySize(Long directoryId,Long size,Integer pattern){
        Directory directory = directoryDao.selectById(directoryId);
        if(pattern==0){
            directory.setSize(directory.getSize()-size);
        }else{
            directory.setSize(directory.getSize()+size);
        }

        //需要进行更新操作的都是目录而不是文件
        if(directory.getType()==1){
            directoryDao.updateById(directory);
        }

    }

    @Override
    public AjaxResult getCloudInfo() {
        String userId = UserUtil.getUserId();
        CloudInfoVo cloudInfoVo = new CloudInfoVo();
        UserDirectory userDirectory = userDirectoryDao.selectOne(new LambdaQueryWrapper<UserDirectory>().eq(UserDirectory::getUserId, userId));
        cloudInfoVo.setRootId(userDirectory.getDirectoryId());
        cloudInfoVo.setAllSize(userDirectory.getAllSize());
        Long size = directoryDao.selectById(userDirectory.getDirectoryId()).getSize();
        cloudInfoVo.setUsedSize(size);
        return AjaxResult.success(cloudInfoVo);
    }

    @Override
    public void updateDict(Long id, String name) {
        String loginId = UserUtil.getUserId();
        //修改目录时先检查目录是否存在，再进行权限的检查
        Directory directory = directoryDao.selectById(id);
        if (directory == null){
            throw new TException(Exceptions.DIRECTORY_IS_NOT_FOUND);
        }
        if (!cloudPermissionUtil.checkUpdateDirectoryPermission(id)){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        //进行修改
        directory.setName(name);
        directoryDao.updateById(directory);
    }

    @Override
    public AjaxResult getDeletedDict() {
        String userId = UserUtil.getUserId();
        String key = RedisKeys.generateCloudRecycleRecord(userId,"*").CLOUD_RECYCLE_RECORD;
        Collection<String> keys = redisCache.keys(key);

        if(keys.size()!=0){
            List<Directory> list = keys.stream().map(key1 -> {
                Directory directory = redisCache.getCacheObject(key1);
                return directory;
            }).toList();
            return AjaxResult.success(list);
        }
        return AjaxResult.success();
    }

    @Override
    public void recycleDict(Long id) {
        String userId = UserUtil.getUserId();
        //首先在回收站中寻找是否存在
        RedisKeys redisKeys = RedisKeys.generateCloudRecycleRecord(userId,String.valueOf(id));
        if(redisCache.hasKey(redisKeys.CLOUD_RECYCLE_RECORD)){
            //如果存在的情况下就查找一下
            Directory directory = redisCache.getCacheObject(redisKeys.CLOUD_RECYCLE_RECORD);
            if(!String.valueOf(directory.getUserId()).equals(userId)){
                //检查权限
                throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
            }
            //恢复,恢复时还需要注意的就是还要恢复原来的size
            String path = directory.getPath();
            String[] split = path.split("/");
            for(int i=0;i<split.length;i++){
                if(split[i]!="")
                    this.updateUserDirectorySize(Long.valueOf(split[i]),directory.getSize(),1);
            }
            directoryDao.recycleById(List.of(directory.getId()));
            //随后删除key
            redisCache.deleteObject(redisKeys.CLOUD_RECYCLE_RECORD);
            //同时如果还存在子目录的情况下也一并恢复了
            String directoryChildrenRecord = RedisKeys.generateDirectoryChildrenRecord(String.valueOf(directory.getId())).DIRECTORY_CHILDREN_RECORD;
            if(redisCache.hasKey(directoryChildrenRecord)){
                List<Long> ids = redisCache.getCacheObject(directoryChildrenRecord);
                directoryDao.recycleById(ids);
                //删除key
                redisCache.deleteObject(directoryChildrenRecord);
            }
        }
    }

    @Override
    public void completeDelete(Long id) {
        String userId = UserUtil.getUserId();
        //首先在回收站中寻找是否存在
        RedisKeys redisKeys = RedisKeys.generateCloudRecycleRecord(userId,String.valueOf(id));
        if(redisCache.hasKey(redisKeys.CLOUD_RECYCLE_RECORD)){
            redisCache.deleteObject(redisKeys.CLOUD_RECYCLE_RECORD);
        }
    }

    @Override
    public void clearRecycle() {
        String userId = UserUtil.getUserId();
        String key = RedisKeys.generateCloudRecycleRecord(userId,"*").CLOUD_RECYCLE_RECORD;
        Collection<String> keys = redisCache.keys(key);
        for (String key1 : keys){
            redisCache.deleteObject(key1);
        }

    }


    private List<DictVo> getDictVoList(Long directoryId){
        String userId = UserUtil.getUserId();
        //对于用户的展示界面来说，只查询一级目录
        List<Directory> directories = directoryDao.selectList(new LambdaQueryWrapper<Directory>().eq(Directory::getParentId, directoryId));
        //目录不存在抛出异常
        if (directoryDao.selectById(directoryId)==null){
            throw new TException(Exceptions.DIRECTORY_IS_NOT_FOUND);
        }
        //查看该目录是否是当前登录人的目录
        if(!cloudPermissionUtil.checkGetDirectoryPermission(directoryId)){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        List<DictVo> list = BeanUtils.copyList(directories, DictVo.class);
        return list;
    }



}
