package com.hao.tnotes.note.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hao.tnotes.common.bean.domain.NoteTag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteTagDao extends BaseMapper<NoteTag> {
}
