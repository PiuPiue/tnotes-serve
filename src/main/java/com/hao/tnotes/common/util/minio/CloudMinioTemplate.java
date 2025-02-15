package com.hao.tnotes.common.util.minio;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hao.tnotes.cloud.dao.CloudFileDao;
import com.hao.tnotes.cloud.dao.DirectoryDao;
import com.hao.tnotes.common.bean.domain.CloudFile;
import com.hao.tnotes.common.bean.dto.CloudFileDto;
import com.hao.tnotes.common.bean.vo.CheckFileVo;
import com.hao.tnotes.common.bean.vo.CloudSuccess;
import com.hao.tnotes.common.util.common.Exceptions;
import com.hao.tnotes.common.util.common.TException;
import com.hao.tnotes.common.util.common.UserUtil;
import com.hao.tnotes.common.util.redis.RedisCache;
import com.hao.tnotes.common.util.redis.RedisKeys;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CloudMinioTemplate {

    @Autowired
    private MinioClient minioClient;

    private static final String SLASH = "/";

    @Value("${minio.defaultBucketName}")
    private String defaultBucketName;

    private static final String sliceBucketName = "slice";

    @Value("${minio.endpoint}")
    private String endpoint;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private CloudFileDao cloudFileDao;

    @Autowired
    private DirectoryDao directoryDao;

    /**
     * 用于检查整个文件和分片文件是否存在，实现秒传和分片上传
     * @param chunkNumber 第几个分片
     * @param chunkSize 分块大小
     * @param currentChunkSize 当前分片大小
     * @param identifier 唯一标识
     * @param fileName 文件名
     * @param relativePath 文件路径
     * @param totalChunks 总分片数
     * @return
     */
    public CheckFileVo checkFile(Integer chunkNumber,
                                 Long chunkSize,
                                 Long currentChunkSize,
                                 String identifier,
                                 String fileName,
                                 String relativePath,
                                 Integer totalChunks){
        //首先查询整个文件是否存在
        CloudFile cloudFile = cloudFileDao.selectOne(new LambdaQueryWrapper<CloudFile>().eq(CloudFile::getMd5, identifier));
        if(cloudFile != null){
            //如果存在就直接返回
            return new CheckFileVo(false,null, true,null);
        }
        //直接redis查询即可
        String key = RedisKeys.generateChunkFileUploadRecord(identifier).CHUNK_FILE_UPLOAD_RECORD;
        if(redisCache.hasKey(key)){
            //如果存在key就进行查询
            List<Integer> uploaded = redisCache.getCacheObject(key);
            if(uploaded.contains(chunkNumber)){
                //分片已上传
                return new CheckFileVo(false, null,false,uploaded.toArray(new Integer[0]));
            }else{
                //分片未上传
                return new CheckFileVo(false, null,false,uploaded.toArray(new Integer[0]));
            }
        }
        //此时不存在key,就证明还没有存储任何文件分片
        return new CheckFileVo(false,null, false,new Integer[0]);
    }

    /**
     * 直接进行文件上传
     * @param cloudFileDto
     * @return
     */
    public CloudSuccess uploadAllFile(CloudFileDto cloudFileDto){
        String userId = UserUtil.getUserId();
        try {
            InputStream inputStream = cloudFileDto.getFile().getInputStream();
            minioClient.putObject(PutObjectArgs.builder().bucket(defaultBucketName).object(userId+cloudFileDto.getFile().getOriginalFilename()).stream(inputStream, cloudFileDto.getFile().getSize(), -1).build());
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new TException(Exceptions.FILE_UPLOAD_ERROR);
        }
        return new CloudSuccess(true, "文件上传成功", endpoint+"/"+defaultBucketName+"/"+cloudFileDto.getFile().getOriginalFilename());

    }

    /**
     * 分片文件的上传
     * @param cloudFileDto
     * @return
     */
    public CloudSuccess uploadFile(CloudFileDto cloudFileDto) {
        String key = RedisKeys.generateChunkFileUploadRecord(cloudFileDto.getIdentifier()).CHUNK_FILE_UPLOAD_RECORD;
        if(!redisCache.hasKey(key)){
            //如果是第一次上传就需要设置一个key
            redisCache.setCacheObject(key, new Integer[0]);
        }
        //此时这个分片是没有被上传的，所以可以进行上传,上传之前还需要把名字给改一下
        this.createSliceBucket();
        //新名字为 chunkNumber+fileName
        String name = SLASH +cloudFileDto.getIdentifier()+SLASH+ cloudFileDto.getChunkNumber()+ cloudFileDto.getFile().getOriginalFilename();
        try {
            InputStream inputStream = cloudFileDto.getFile().getInputStream();
            minioClient.putObject(PutObjectArgs.builder().bucket(sliceBucketName).object(name).stream(inputStream, cloudFileDto.getFile().getSize(), -1).build());
            inputStream.close();
        }catch (Exception e){
            throw new TException(Exceptions.FILE_UPLOAD_ERROR);
        }
        //此时上传完成，可以把上传记录存到redis中
        List<Integer> uploaded = redisCache.getCacheObject(key);
        uploaded.add(cloudFileDto.getChunkNumber());
        redisCache.setCacheObject(key, uploaded.toArray(new Integer[0]));
        return new CloudSuccess(true, "分片上传成功",null);
    }

    public CloudSuccess mergeFile(CloudFileDto cloudFileDto) {
        String userId = UserUtil.getUserId();
        //先查看数据库中是否已经存在了
        CloudFile cloudFile1 = cloudFileDao.selectOne(new LambdaQueryWrapper<CloudFile>().eq(CloudFile::getMd5, cloudFileDto.getIdentifier()));
        if(cloudFile1 != null){
            //如果存在就直接返回
            return new CloudSuccess(true, "文件已存在", cloudFile1.getUrl());
        }
        //还需要检查的是所有分片是否真的已经上传
        String key = RedisKeys.generateChunkFileUploadRecord(cloudFileDto.getIdentifier()).CHUNK_FILE_UPLOAD_RECORD;
        if(!redisCache.hasKey(key)){
            //此时说明分片上传失败了
            throw new TException(Exceptions.FILE_UPLOAD_ERROR);
        }
        List<Integer> uploaded = redisCache.getCacheObject(key);
        if(uploaded.size() != cloudFileDto.getTotalChunks()){
            //此时说明分片上传失败了
            throw new TException(Exceptions.FILE_UPLOAD_ERROR);
        }
        //此时就可以进行合并处理了
        List<ComposeSource> list = new ArrayList<>();
        for(int i = 1; i <= cloudFileDto.getTotalChunks(); i++){
            list.add(ComposeSource.builder().bucket(sliceBucketName).object(SLASH+cloudFileDto.getIdentifier()+SLASH+i+cloudFileDto.getFileName()).build());
        }
        try {
            minioClient.composeObject(ComposeObjectArgs.builder().bucket(defaultBucketName).object(userId+cloudFileDto.getFileName()).sources(list).build());
        } catch (Exception e) {
            throw new TException(Exceptions.FILE_MERGE_ERROR);
        }
        //合并完成之后就可以删除分片文件了
        try {
            //先删除里面的文件
            for(int i = 1; i <= cloudFileDto.getTotalChunks(); i++){
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(sliceBucketName).object(SLASH+cloudFileDto.getIdentifier()+SLASH+i+cloudFileDto.getFileName()).build());
            }
            //再删除文件夹
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(sliceBucketName).object(SLASH+cloudFileDto.getIdentifier()).build());
        } catch (Exception e) {
            throw new TException(Exceptions.FILE_MERGE_ERROR);
        }
        String path = endpoint+"/"+defaultBucketName+"/"+cloudFileDto.getFileName();
        //此时可以清除redis中的缓存了
        redisCache.deleteObject(key);
        return new CloudSuccess(true, "合并成功", path);
    }



    /**
     * 创建分片上传的bucket
     */
    private void createSliceBucket(){
        BucketExistsArgs build = BucketExistsArgs.builder().bucket(sliceBucketName).build();
        try {
            if (!minioClient.bucketExists(build)) {
                minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket(sliceBucketName).build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String preview(String name) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(defaultBucketName).object(name).method(Method.GET).build());
        String replace = url.replace("http://103.218.240.252:9000", "https://minio.t-music.cn");
        return replace;
    }

    public StatObjectResponse getFileInfo(String name){
        try {
            return minioClient.statObject(StatObjectArgs.builder().bucket(defaultBucketName).object(name).build());
        } catch (Exception e) {
            e.printStackTrace();
            throw new TException(Exceptions.FILE_PREVIEW_ERROR);
        }
    }

}
