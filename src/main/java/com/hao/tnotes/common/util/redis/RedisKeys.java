package com.hao.tnotes.common.util.redis;

/**
 * RedisKey常类
 */
public class RedisKeys {

    // 分片文件上传记录缓存
    public String CHUNK_FILE_UPLOAD_RECORD;
    // 云盘回收站记录
    public String CLOUD_RECYCLE_RECORD;
    // 文件夹对应子目录
    public String DIRECTORY_CHILDREN_RECORD;
    // 用户验证码存储
    public String USER_VERIFY_CODE;
    // 解绑邮箱验证码
    public String UNBIND_EMAIL_VERIFY_CODE;
    // 解绑邮箱标记
    public String UNBIND_EMAIL_FLAG;
    // 新邮箱验证码
    public String NEW_EMAIL_VERIFY_CODE;



    public static RedisKeys generateChunkFileUploadRecord(String fileMD5) {
        RedisKeys redisKeys = new RedisKeys();
        // 生成策略：前缀+文件MD5-
        redisKeys.CHUNK_FILE_UPLOAD_RECORD = "CHUNK_FILE_UPLOAD_RECORD:" + fileMD5;
        return redisKeys;
    }

    public static RedisKeys generateCloudRecycleRecord(String userId,String id) {
        RedisKeys redisKeys = new RedisKeys();
        redisKeys.CLOUD_RECYCLE_RECORD = "CLOUD_RECYCLE_RECORD:" + userId + ":" + id;
        return redisKeys;
    }

    public static RedisKeys generateDirectoryChildrenRecord(String directoryId) {
        RedisKeys redisKeys = new RedisKeys();
        redisKeys.DIRECTORY_CHILDREN_RECORD = "DIRECTORY_CHILDREN_RECORD:" + directoryId;
        return redisKeys;
    }

    public static RedisKeys generateUserVerifyCode(String email) {
        RedisKeys redisKeys = new RedisKeys();
        redisKeys.USER_VERIFY_CODE = "USER_VERIFY_CODE:" + email;
        return redisKeys;
    }

    public static RedisKeys generateUnbindEmailVerifyCode(String email) {
        RedisKeys redisKeys = new RedisKeys();
        redisKeys.UNBIND_EMAIL_VERIFY_CODE = "UNBIND_EMAIL_VERIFY_CODE:" + email;
        return redisKeys;
    }

    public static RedisKeys generateUnbindEmailFlag(String email) {
        RedisKeys redisKeys = new RedisKeys();
        redisKeys.UNBIND_EMAIL_FLAG = "UNBIND_EMAIL_FLAG:" + email;
        return redisKeys;
    }

    public static RedisKeys generateNewEmailVerifyCode(String email) {
        RedisKeys redisKeys = new RedisKeys();
        redisKeys.NEW_EMAIL_VERIFY_CODE = "NEW_EMAIL_VERIFY_CODE:" + email;
        return redisKeys;
    }


}
