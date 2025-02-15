package com.hao.tnotes.note.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hao.tnotes.common.bean.domain.NoteShare;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteShareDao extends BaseMapper<NoteShare> {
}
