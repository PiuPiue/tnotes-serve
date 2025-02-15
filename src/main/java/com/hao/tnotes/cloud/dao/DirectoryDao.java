package com.hao.tnotes.cloud.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hao.tnotes.common.bean.domain.Directory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;


@Mapper
public interface DirectoryDao extends BaseMapper<Directory> {

    @Update("<script>" +
            "UPDATE tnotes.directory SET is_delete = 0 WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    public void recycleById(List<Long> ids);

}
