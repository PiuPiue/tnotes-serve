package com.hao.tnotes.note.controller;



import com.hao.tnotes.common.util.minio.MinioReturn;
import com.hao.tnotes.common.util.minio.MinioTemplate;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/minio")
@AllArgsConstructor
public class MinioController {


    private final MinioTemplate minioTemplate;

    @PostMapping("/upload")
    public MinioReturn upload(@RequestBody @RequestParam("file") MultipartFile file){
        MinioReturn minioReturn = null;
        try {
            minioReturn = minioTemplate.putFile(file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return minioReturn;
    }

    @PostMapping("/remove")
    public String removeFile(@RequestBody Map map){
        try {
            minioTemplate.removeFile(map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "success";
    }

    @PostMapping("/uploadMultiple")
    public List<MinioReturn> uploadMultipart(@RequestParam("files") MultipartFile[] files){
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<CompletableFuture<MinioReturn>> collect = List.of(files).stream().map(file -> CompletableFuture.supplyAsync(() -> {
            try {
                return minioTemplate.putFile(file);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executorService)).collect(Collectors.toList());

        List<MinioReturn> collect1 = collect.stream().map(CompletableFuture::join).collect(Collectors.toList());
        executorService.shutdown();
        return collect1;
    }



}
