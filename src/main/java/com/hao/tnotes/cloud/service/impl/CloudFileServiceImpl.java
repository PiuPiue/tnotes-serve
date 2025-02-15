package com.hao.tnotes.cloud.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hao.tnotes.cloud.dao.CloudFileDao;
import com.hao.tnotes.cloud.dao.DirectoryDao;
import com.hao.tnotes.cloud.dao.UserDirectoryDao;
import com.hao.tnotes.cloud.service.CloudFileService;
import com.hao.tnotes.common.bean.domain.CloudFile;
import com.hao.tnotes.common.bean.domain.Directory;
import com.hao.tnotes.common.bean.domain.UserDirectory;
import com.hao.tnotes.common.bean.dto.CloudFileDto;
import com.hao.tnotes.common.bean.vo.CheckFileVo;
import com.hao.tnotes.common.bean.vo.CloudSuccess;
import com.hao.tnotes.common.util.cloudutil.CloudPermissionUtil;
import com.hao.tnotes.common.util.common.AjaxResult;
import com.hao.tnotes.common.util.common.Exceptions;
import com.hao.tnotes.common.util.common.TException;
import com.hao.tnotes.common.util.common.UserUtil;
import com.hao.tnotes.common.util.minio.CloudMinioTemplate;
import io.minio.MinioClient;
import io.minio.StatObjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CloudFileServiceImpl implements CloudFileService {

    @Autowired
    private CloudMinioTemplate cloudMinioTemplate;
    @Autowired
    private DirectoryDao directoryDao;
    @Autowired
    private CloudFileDao cloudFileDao;
    @Autowired
    private CloudPermissionUtil cloudPermissionUtil;
    @Autowired
    private UserDirectoryDao userDirectoryDao;

    @Override
    public CheckFileVo checkFile(Integer chunkNumber, Long chunkSize, Long currentChunkSize, Integer totalSize,String identifier, String fileName, String relativePath, Integer totalChunks) {
        //首先判断用户剩余空间是否足够
        if(!cloudPermissionUtil.checkSpacePermission(totalSize)){
            return new CheckFileVo(true,"用户剩余空间不足",false,null);
        }
        //然后再进行判断
        return cloudMinioTemplate.checkFile(chunkNumber,chunkSize,currentChunkSize,identifier,fileName,relativePath,totalChunks);
    }

    @Override
    public CloudSuccess mergeFile(CloudFileDto cloudFileDto) {
        //合并操作，需要判断用户是否具有相关权限，实际上就是创建文件的权限
        String userId = UserUtil.getUserId();
        Directory directory1 = directoryDao.selectById(cloudFileDto.getParentId());
        if(directory1==null){
            throw new TException(Exceptions.DIRECTORY_IS_NOT_FOUND);
        }
        if(!cloudPermissionUtil.checkCreateDirectoryPermission(cloudFileDto.getParentId())){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }

        CloudSuccess cloudSuccess = cloudMinioTemplate.mergeFile(cloudFileDto);
        //此时还有数据库插入操作，
        if(cloudSuccess.getSuccess()){
            //还需要获取的是文件的大小
            if(!cloudSuccess.getMessage().equals("文件已存在")){
                CloudFile cloudFile = new CloudFile();
                cloudFile.setName(userId+cloudFileDto.getFileName());
                cloudFile.setMd5(cloudFileDto.getIdentifier());
                StatObjectResponse fileInfo = cloudMinioTemplate.getFileInfo(userId + cloudFileDto.getFileName());
                cloudFile.setSize(fileInfo.size());
                cloudFile.setUrl(cloudSuccess.getUrl());
                cloudFileDao.insert(cloudFile);
            }
            //随后插入目录表
            CloudFile cloudFile = cloudFileDao.selectOne(new LambdaQueryWrapper<CloudFile>().eq(CloudFile::getMd5, cloudFileDto.getIdentifier()));
            Directory directory = new Directory();
            directory.setName(cloudFileDto.getFileName());
            directory.setSize(cloudFile.getSize());
            directory.setFileId(cloudFile.getId());
            directory.setType(0);
            directory.setUserId(Long.valueOf(UserUtil.getUserId()));
            directory.setParentId(cloudFileDto.getParentId());
            String path =  directory1.getPath()!=null?directory1.getPath()+"/"+directory1.getId():""+"/"+directory1.getId();
            directory.setPath(path);
            directoryDao.insert(directory);
            //还需要更新用户目录的文件大小
            String[] split = path.split("/");
            //进行更新操作
            for(int i=0;i<split.length;i++){
                if(split[i]!="")
                    this.updateUserDirectorySize(Long.valueOf(split[i]),cloudFile.getSize());
            }
        }
        return cloudSuccess;
    }

    private void updateUserDirectorySize(Long directoryId,Long size){
        Directory directory = directoryDao.selectById(directoryId);
        directory.setSize(directory.getSize()+size);
        //需要进行更新操作的都是目录而不是文件
        if(directory.getType()==1){
            directoryDao.updateById(directory);
        }

    }

    @Override
    public CloudSuccess uploadFile(CloudFileDto cloudFileDto) {
        //判断用户剩余空间是否足够
        boolean b = cloudPermissionUtil.checkSpacePermission(Integer.valueOf(cloudFileDto.getTotalSize()));
        if(!b){
            throw new TException(Exceptions.USER_HAS_NO_SPACE);
        }
        String userId = UserUtil.getUserId();
        if(cloudFileDto.getTotalChunks()==1&&cloudFileDto.getChunkSize()<=6*1024*1024){
            //查看当前是进行分片上传还是直接上传
            //然后查看该文件是否已经存在
            CloudFile cloudFile1 = cloudFileDao.selectOne(new LambdaQueryWrapper<CloudFile>().eq(CloudFile::getMd5, cloudFileDto.getIdentifier()));
            if(cloudFile1!=null){
                //此时说明该文件已经存在
                return new CloudSuccess(true,"文件已存在",null);
            }
            CloudSuccess cloudSuccess = cloudMinioTemplate.uploadAllFile(cloudFileDto);
            if(cloudSuccess.getSuccess()){
                //此时就可以进行数据库插入操作了
                //首先插入文件表
                CloudFile cloudFile = new CloudFile();
                cloudFile.setName(userId+cloudFileDto.getFile().getOriginalFilename());
                cloudFile.setMd5(cloudFileDto.getIdentifier());
                cloudFile.setSize(Long.valueOf(cloudFileDto.getTotalSize()));
                cloudFile.setUrl(cloudSuccess.getUrl());
                cloudFileDao.insert(cloudFile);
            }
            return cloudSuccess;
        }else{
            return cloudMinioTemplate.uploadFile(cloudFileDto);
        }

    }

    @Override
    public AjaxResult preview(Long id) {
        //修改用户的一系列存储的大小
        String userId = UserUtil.getUserId();
        //首先查询该文件是否存在以及权限判定
        Directory directory = directoryDao.selectById(id);
        if(directory==null){
            throw new TException(Exceptions.FILE_IS_NOT_FOUND);
        }
        if(!userId.equals(String.valueOf(directory.getUserId()))){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        String name = cloudFileDao.selectById(directory.getFileId()).getName();
        String url = null;
       try {
           url = cloudMinioTemplate.preview(name);
       }catch (Exception e){
           throw new TException(Exceptions.FILE_PREVIEW_ERROR);
       }
        return AjaxResult.success(url);
    }
}
