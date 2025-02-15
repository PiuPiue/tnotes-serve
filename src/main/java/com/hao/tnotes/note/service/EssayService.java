package com.hao.tnotes.note.service;

import com.hao.tnotes.common.bean.dto.EssayDto;
import com.hao.tnotes.common.bean.vo.EssayVo;

import java.util.List;

public interface EssayService {
    List<EssayVo> list();

    void add(EssayDto noteDto);

    void delete(Long id);

    EssayVo get(Long id);
}
