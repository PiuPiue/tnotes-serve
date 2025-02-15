package com.hao.tnotes.cloud.service;

import com.hao.tnotes.common.bean.dto.CloudFileDto;
import com.hao.tnotes.common.bean.vo.CheckFileVo;
import com.hao.tnotes.common.bean.vo.CloudSuccess;
import com.hao.tnotes.common.util.common.AjaxResult;

public interface CloudFileService {

    CheckFileVo checkFile(Integer chunkNumber,
                          Long chunkSize,
                          Long currentChunkSize,
                          Integer totalSize,
                          String identifier,
                          String fileName,
                          String relativePath,
                          Integer totalChunks);
    CloudSuccess mergeFile(CloudFileDto cloudFileDto);

    CloudSuccess uploadFile(CloudFileDto cloudFileDto);

    AjaxResult preview(Long id);
}
