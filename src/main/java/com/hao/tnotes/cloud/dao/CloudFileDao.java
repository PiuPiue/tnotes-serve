package com.hao.tnotes.cloud.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hao.tnotes.common.bean.domain.CloudFile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CloudFileDao extends BaseMapper<CloudFile> {
}
