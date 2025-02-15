package com.hao.tnotes.note.controller;

import com.hao.tnotes.common.bean.dto.EssayDto;
import com.hao.tnotes.common.util.common.AjaxResult;
import com.hao.tnotes.note.service.EssayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/essay")
public class EssayController {

    @Autowired
    private EssayService essayService;

    @GetMapping ("/list")
    public AjaxResult list() {
       return AjaxResult.success(essayService.list());
    }

    @PostMapping("/add")
    public AjaxResult add(@RequestBody EssayDto essayDto) {
        essayService.add(essayDto);
        return AjaxResult.success();
    }

    @GetMapping("/delete")
    public AjaxResult delete(Long id) {
        essayService.delete(id);
        return AjaxResult.success();
    }

    @GetMapping("/get")
    public AjaxResult get(Long id) {
        return AjaxResult.success(essayService.get(id));
    }

}
