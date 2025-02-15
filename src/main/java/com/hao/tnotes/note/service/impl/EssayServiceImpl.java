package com.hao.tnotes.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hao.tnotes.common.bean.domain.Essay;
import com.hao.tnotes.common.bean.dto.EssayDto;
import com.hao.tnotes.common.bean.vo.EssayVo;
import com.hao.tnotes.common.util.bean.BeanUtils;
import com.hao.tnotes.common.util.bean.CollUtils;
import com.hao.tnotes.common.util.common.Exceptions;
import com.hao.tnotes.common.util.common.TException;
import com.hao.tnotes.common.util.common.UserUtil;
import com.hao.tnotes.common.util.noteutil.PermissionValue;
import com.hao.tnotes.note.dao.EssayDao;
import com.hao.tnotes.note.dao.UserDao;
import com.hao.tnotes.note.service.EssayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EssayServiceImpl implements EssayService {

    @Autowired
    private EssayDao essayDao;
    @Autowired
    private UserDao userDao;



    @Override
    public List<EssayVo> list() {
        String userId = UserUtil.getUserId();
        String name = userDao.selectById(userId).getName();
        List<Essay> essays = essayDao.selectList(new LambdaQueryWrapper<Essay>().eq(Essay::getUserId, userId));
        if(essays.size()!=0){
            List<EssayVo> essayVos = BeanUtils.copyList(essays, EssayVo.class);
            List<EssayVo> list = essayVos.stream().map(
                    essayVo -> {
                        essayVo.setUser(name);
                        return essayVo;
                    }
            ).toList();
            return list;
        }
        return CollUtils.emptyList();
    }

    @Override
    public void add(EssayDto essayDto) {
        String userId = UserUtil.getUserId();
        if(essayDto.getId()!=null){
            //首先检查是否存在和是否是作者本人
            Essay essay = essayDao.selectById(essayDto.getId());
            if(essay==null){
                throw new TException(Exceptions.NOTE_IS_NOT_FOUND);
            }
            if(!userId.equals(String.valueOf(essay.getUserId()))){
                throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
            }
            essay = BeanUtils.copyBean(essayDto, Essay.class);
            essayDao.updateById(essay);
        }else{
            //新增或删除,需不需要作为另外一个存在，如果不需要的话业务会变得非常简化了
            Essay essay  = BeanUtils.copyBean(essayDto, Essay.class);
            essay.setVisible(PermissionValue.NO_VISIBLE);
            essay.setUserId(Long.valueOf(userId));
            essayDao.insert(essay);
        }
    }

    @Override
    public void delete(Long id) {
        String userId = UserUtil.getUserId();
        Essay essay = essayDao.selectById(id);
        if(essay==null){
            throw new TException(Exceptions.NOTE_IS_NOT_FOUND);
        }
        if(!essay.getUserId().equals(Long.valueOf(userId))){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        essayDao.deleteById(id);
    }

    @Override
    public EssayVo get(Long id) {
        String userId = UserUtil.getUserId();
        Essay essay = essayDao.selectById(id);
        if(essay==null){
            throw new TException(Exceptions.NOTE_IS_NOT_FOUND);
        }
        if(!essay.getUserId().equals(Long.valueOf(userId))){
            throw new TException(Exceptions.USER_HAS_NO_PERMISSION);
        }
        EssayVo essayVo = BeanUtils.copyBean(essay, EssayVo.class);

        String name = userDao.selectById(userId).getName();
        essayVo.setUser(name);
        return essayVo;
    }
}
