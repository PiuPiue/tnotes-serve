package com.hao.tnotes.common.util.cloudutil;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hao.tnotes.cloud.dao.DirectoryDao;
import com.hao.tnotes.cloud.dao.UserDirectoryDao;
import com.hao.tnotes.common.bean.domain.Directory;
import com.hao.tnotes.common.bean.domain.UserDirectory;
import com.hao.tnotes.common.util.common.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 网盘文件权限检查类
 */
@Component
public class CloudPermissionUtil {

    @Autowired
    private DirectoryDao directoryDao;
    @Autowired
    private UserDirectoryDao userDirectoryDao;

    //获取目录
    public boolean checkGetDirectoryPermission(Long directoryId){
        Long userId = Long.valueOf(UserUtil.getUserId());
        //查看该目录是否是当前登录人的
        Directory directory = directoryDao.selectById(directoryId);
        if(userId.equals(directory.getUserId())){
            return true;
        }
        return false;
    }
    //新建目录
    public boolean checkCreateDirectoryPermission(Long parentId){
        Long userId = Long.valueOf(UserUtil.getUserId());
        //首先查看是否是当前登录人的
        return this.checkGetDirectoryPermission(parentId);
        //后续可以根据实际需求进行调整和扩展
    }

    //修改目录
    public boolean checkUpdateDirectoryPermission(Long directoryId){
        Long userId = Long.valueOf(UserUtil.getUserId());
        //首先查看是否是当前登录人的
        return this.checkGetDirectoryPermission(directoryId);
        //后续可以根据实际需求进行调整和扩展
    }

    //删除目录
    public boolean checkDeleteDirectoryPermission(Long directoryId){
        Long userId = Long.valueOf(UserUtil.getUserId());
        //首先查看是否是当前登录人的
        return this.checkGetDirectoryPermission(directoryId);
        //后续可以根据实际需求进行调整和扩展
    }

    //剩余空间判断
    public boolean checkSpacePermission(Integer size){
        //首先判断用户剩余空间是否足够
        String userId = UserUtil.getUserId();
        UserDirectory userDirectory = userDirectoryDao.selectOne(new LambdaQueryWrapper<UserDirectory>().eq(UserDirectory::getUserId, userId));
        Long allSize = userDirectory.getAllSize();
        Long usedSize = directoryDao.selectById(userDirectory.getDirectoryId()).getSize();
        if(allSize - usedSize >= size){
            return true;
        }
        return false;
    }





}
