package com.hao.tnotes.note.dao;

import com.hao.tnotes.common.bean.domain.NoteContent;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.net.ContentHandler;

@Mapper
public interface NoteContentDao extends MongoRepository<NoteContent,String> {
}
