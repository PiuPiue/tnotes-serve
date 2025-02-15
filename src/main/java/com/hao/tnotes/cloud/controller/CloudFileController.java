package com.hao.tnotes.cloud.controller;

import com.hao.tnotes.cloud.service.CloudFileService;
import com.hao.tnotes.common.bean.dto.CloudFileDto;
import com.hao.tnotes.common.bean.vo.CheckFileVo;
import com.hao.tnotes.common.bean.vo.CloudSuccess;
import com.hao.tnotes.common.util.common.AjaxResult;
import com.hao.tnotes.common.util.minio.CloudMinioTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tcloud")
public class CloudFileController {

    @Autowired
    private CloudMinioTemplate CloudMinioTemplate;
    @Autowired
    private CloudFileService cloudFileService;

    @GetMapping("/upload")
    public CheckFileVo upload(
            Integer chunkNumber,
            Long chunkSize,
            Long currentChunkSize,
            Integer totalSize,
            String identifier,
            String fileName,
            String relativePath,
            Integer totalChunks)
    {
        return cloudFileService.checkFile(chunkNumber, chunkSize, currentChunkSize, totalSize, identifier, fileName, relativePath, totalChunks);
    }

    @PostMapping("/upload")
    public CloudSuccess uploadFile(CloudFileDto cloudFileDto) {
        return cloudFileService.uploadFile(cloudFileDto);

    }

    @PostMapping("/merge")
    public CloudSuccess mergeFile(@RequestBody CloudFileDto cloudFileDto) {
        return cloudFileService.mergeFile(cloudFileDto);
    }

    @GetMapping("/preview")
    public AjaxResult preview(Long id) {
        return cloudFileService.preview(id);
    }


}
