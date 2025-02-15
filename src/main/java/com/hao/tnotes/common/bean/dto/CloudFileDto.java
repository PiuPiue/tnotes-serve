package com.hao.tnotes.common.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CloudFileDto {

    private Integer chunkNumber;
    private Long chunkSize;
    private Long currentChunkSize;
    private Integer totalSize;
    private String identifier;
    private String fileName;
    private String relativePath;
    private Integer totalChunks;
    private MultipartFile file;
    private Long parentId;

}
