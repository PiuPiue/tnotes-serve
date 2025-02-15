package com.hao.tnotes.note.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.hao.tnotes.common.bean.domain.User;
import com.hao.tnotes.common.bean.dto.LoginDto;
import com.hao.tnotes.common.bean.dto.UserDto;
import com.hao.tnotes.common.bean.mq.CodeMessage;
import com.hao.tnotes.common.bean.vo.UserVo;
import com.hao.tnotes.common.mq.MQRecord;
import com.hao.tnotes.common.util.bean.BeanUtils;
import com.hao.tnotes.common.util.common.EmailUtil;
import com.hao.tnotes.common.util.common.Exceptions;
import com.hao.tnotes.common.util.common.TException;
import com.hao.tnotes.common.util.common.UserUtil;
import com.hao.tnotes.common.util.redis.RedisCache;
import com.hao.tnotes.common.util.redis.RedisKeys;
import com.hao.tnotes.note.dao.UserDao;
import com.hao.tnotes.note.service.UserService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private EmailUtil emailUtil;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public UserVo login(LoginDto loginDto) {
        //首先查看登录类型
        User user = null;
        if(loginDto.getName() != null){
            //用户名密码登录
            if(StringUtils.isEmpty(loginDto.getName()) || StringUtils.isEmpty(loginDto.getPassword())){
                throw new TException(Exceptions.NAME_OR_PASSWORD_IS_EMPTY);
            }
            LambdaQueryWrapper<User> eq = new LambdaQueryWrapper<User>().eq(User::getName, loginDto.getName()).eq(User::getPassword, loginDto.getPassword());
            user = userDao.selectOne(eq);
        }else{
            //邮箱登录
            if(StringUtils.isEmpty(loginDto.getEmail()) || StringUtils.isEmpty(loginDto.getPassword())){
                throw new TException(Exceptions.USER_OR_EMAIL_IS_EMPTY);
            }
            LambdaQueryWrapper<User> eq = new LambdaQueryWrapper<User>().eq(User::getEmail, loginDto.getEmail()).eq(User::getPassword, loginDto.getPassword());
            user = userDao.selectOne(eq);
        }
        //密码错误
        if(user==null){
            throw new TException(Exceptions.USER_OR_PASSWORD_IS_ERROR);
        }
        //设置登录
        StpUtil.login(user.getId());

        //返回用户信息
        UserVo userVo = BeanUtils.copyBean(user, UserVo.class);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        userVo.setToken(tokenInfo.getTokenValue());
        return userVo;
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public void register(UserDto userDto) {
        //首选查看验证码是否正确
        String userVerifyCode = RedisKeys.generateUserVerifyCode(userDto.getEmail()).USER_VERIFY_CODE;
        if(!redisCache.hasKey(userVerifyCode)){
            throw new TException(Exceptions.AUTH_CODE_ERROR);
        }
        String code = redisCache.getCacheObject(userVerifyCode);
        if(!code.equals(userDto.getAuthCode())){
            throw new TException(Exceptions.AUTH_CODE_ERROR);
        }
        //随后校验用户名、邮箱是否重复
        if(userDao.selectOne(new LambdaQueryWrapper<User>().eq(User::getName, userDto.getName()))!=null){
            throw new TException(Exceptions.USER_NAME_HAS_EXIST);
        }
        if(userDao.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, userDto.getEmail()))!=null){
            throw new TException(Exceptions.USER_EMAIL_HAS_EXIST);
        }
        User user = BeanUtils.copyBean(userDto, User.class);
        user.setAvatar("https://haowallpaper.com/link/common/file/previewFileImg/15798746313888064");
        user.setDescription("用户暂无描述~~~");
        userDao.insert(user);
        //除此之外，还需进行一些初始化设置
        rabbitTemplate.convertAndSend(MQRecord.EXCHANGE_NAME, MQRecord.INIT_USER_NOTE_ROUTING_KEY,user.getId());
        rabbitTemplate.convertAndSend(MQRecord.EXCHANGE_NAME, MQRecord.INIT_USER_CLOUD_ROUTING_KEY,user.getId());
    }

    @Override
    public void sendAuthCode(String email) {
        //生成随机六位验证码
        String code = RandomUtil.randomNumbers(6);
        //首先存储进redis中
        String userVerifyCode = RedisKeys.generateUserVerifyCode(email).USER_VERIFY_CODE;
        //五分钟内有效
        redisCache.setCacheObject(userVerifyCode, code,5, TimeUnit.MINUTES);
        CodeMessage codeMessage = new CodeMessage(email,code);
        rabbitTemplate.convertAndSend(MQRecord.EXCHANGE_NAME, MQRecord.AUTH_CODE_ROUTING_KEY,codeMessage);
    }

    @Override
    public UserVo updateUser(UserDto userDto) {
        String userId = UserUtil.getUserId();
        //查看姓名是否相同
        User user1 = userDao.selectOne(new LambdaQueryWrapper<User>().eq(User::getName, userDto.getName()));
        if(user1!=null&&!user1.getId().equals(Long.valueOf(userId))){
            throw new TException(Exceptions.USER_NAME_HAS_EXIST);
        }
        User user = BeanUtils.copyBean(userDto, User.class);
        user.setId(Long.parseLong(userId));
        //防止用户恶意修改
        user.setIsVip(null);
        user.setPassword(null);
        user.setEmail(null);
        user.setCreateTime(null);
        user.setUpdateTime(null);
        user.setIsDelete(null);
        //进行修改
        userDao.updateById(user);
        return BeanUtils.copyBean(userDao.selectById(userId), UserVo.class);
    }

    @Override
    public void modifyPassword(UserDto userDto) {
        String userId = UserUtil.getUserId();
        String email = userDao.selectById(userId).getEmail();
        //首选查看验证码是否正确
        String userVerifyCode = RedisKeys.generateUserVerifyCode(email).USER_VERIFY_CODE;
        if(!redisCache.hasKey(userVerifyCode)){
            throw new TException(Exceptions.AUTH_CODE_ERROR);
        }
        String code = redisCache.getCacheObject(userVerifyCode);
        if(!code.equals(userDto.getAuthCode())){
            throw new TException(Exceptions.AUTH_CODE_ERROR);
        }
        //然后查询新密码格式是否为空
        if(StringUtils.isEmpty(userDto.getPassword())){
            throw new TException(Exceptions.PASSWORD_ERROR);
        }
        //然后进行修改
        userDao.update(null,new UpdateWrapper<User>().eq("id",userId).set("password",userDto.getPassword()));
    }

    @Override
    public void sendPwdCode() {
        String userId = UserUtil.getUserId();
        String email = userDao.selectById(userId).getEmail();
        //生成随机六位验证码
        String code = RandomUtil.randomNumbers(6);
        //首先存储进redis中
        String userVerifyCode = RedisKeys.generateUserVerifyCode(email).USER_VERIFY_CODE;
        //五分钟内有效
        redisCache.setCacheObject(userVerifyCode, code,5, TimeUnit.MINUTES);
        CodeMessage codeMessage = new CodeMessage(email,code);
        rabbitTemplate.convertAndSend(MQRecord.EXCHANGE_NAME, MQRecord.AUTH_CODE_ROUTING_KEY,codeMessage);
    }

    @Override
    public void sendEmailCode() {
        String userId = UserUtil.getUserId();
        String email = userDao.selectById(userId).getEmail();
        //生成随机六位验证码
        String code = RandomUtil.randomNumbers(6);
        //首先存储进redis中
        String unbindEmailVerifyCode = RedisKeys.generateUnbindEmailVerifyCode(email).UNBIND_EMAIL_VERIFY_CODE;
        //五分钟内有效
        redisCache.setCacheObject(unbindEmailVerifyCode, code,5, TimeUnit.MINUTES);
        CodeMessage codeMessage = new CodeMessage(email,code);
        rabbitTemplate.convertAndSend(MQRecord.EXCHANGE_NAME, MQRecord.AUTH_CODE_ROUTING_KEY,codeMessage);
    }

    @Override
    public void modifyEmail(UserDto userDto) {
        String userId = UserUtil.getUserId();
        String email = userDao.selectById(userId).getEmail();
        if (!redisCache.hasKey(RedisKeys.generateUnbindEmailFlag(email).UNBIND_EMAIL_FLAG)){
            throw new TException(Exceptions.HAS_NOT_UNBIND_EMAIL);
        }
        //然后验证
        if (!redisCache.hasKey(RedisKeys.generateNewEmailVerifyCode(userDto.getEmail()).NEW_EMAIL_VERIFY_CODE)){
            throw new TException(Exceptions.AUTH_CODE_ERROR);
        }
        if (!userDto.getAuthCode().equals(redisCache.getCacheObject(RedisKeys.generateNewEmailVerifyCode(userDto.getEmail()).NEW_EMAIL_VERIFY_CODE))){
            throw new TException(Exceptions.AUTH_CODE_ERROR);
        }
        //检查邮箱是否已经被绑定
        if(userDao.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, userDto.getEmail()))!=null){
            throw new TException(Exceptions.USER_EMAIL_HAS_EXIST);
        }
        //最后进行修改
        userDao.update(null,new UpdateWrapper<User>().eq("id",userId).set("email",userDto.getEmail()));
    }

    @Override
    public void unbindEmail(String code) {
        String userId = UserUtil.getUserId();
        String email = userDao.selectById(userId).getEmail();
        if(!redisCache.hasKey(RedisKeys.generateUnbindEmailVerifyCode(email).UNBIND_EMAIL_VERIFY_CODE)){
            throw new TException(Exceptions.AUTH_CODE_ERROR);
        }
        if(!code.equals(redisCache.getCacheObject(RedisKeys.generateUnbindEmailVerifyCode(email).UNBIND_EMAIL_VERIFY_CODE))){
            throw new TException(Exceptions.AUTH_CODE_ERROR);
        }
        String unbindEmailFlag = RedisKeys.generateUnbindEmailFlag(email).UNBIND_EMAIL_FLAG;
        redisCache.setCacheObject(unbindEmailFlag, "1", 5, TimeUnit.MINUTES);

    }

    @Override
    public void sendNewEmailCode(String email) {
        //生成随机六位验证码
        String code = RandomUtil.randomNumbers(6);
        //首先存储进redis中
        String userVerifyCode = RedisKeys.generateNewEmailVerifyCode(email).NEW_EMAIL_VERIFY_CODE;
        //五分钟内有效
        redisCache.setCacheObject(userVerifyCode, code,5, TimeUnit.MINUTES);
        CodeMessage codeMessage = new CodeMessage(email,code);
        rabbitTemplate.convertAndSend(MQRecord.EXCHANGE_NAME, MQRecord.AUTH_CODE_ROUTING_KEY,codeMessage);
    }
}
