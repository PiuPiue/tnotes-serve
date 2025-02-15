package com.hao.tnotes.note.controller;

import com.hao.tnotes.common.bean.dto.MessageDto;
import com.hao.tnotes.common.util.common.AjaxResult;
import com.hao.tnotes.note.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/summary")
    public AjaxResult summary(@RequestBody MessageDto messageDto) {
        return aiService.summary(messageDto.getMessage());
    }

}
