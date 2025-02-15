package com.hao.tnotes.note.service.impl;

import com.hao.tnotes.common.util.common.AjaxResult;
import com.hao.tnotes.note.service.AIService;
import org.springframework.ai.openai.client.OpenAiClient;
import org.springframework.ai.prompt.Prompt;
import org.springframework.ai.prompt.messages.SystemMessage;
import org.springframework.ai.prompt.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ai.prompt.messages.Message;

import java.util.List;

@Service
public class AIServiceImpl implements AIService {

   @Autowired
   private OpenAiClient openAiClient;

   private final Message systemMessage = new SystemMessage("你是一个笔记总结方面的专家，你主要就是负责对接收到的笔记、文章进行一个摘要生成,总结的字数根据笔记长度决定，同时生成的回复作为一段文字即可，不需要使用md格式");

    @Override
    public AjaxResult summary(String content) {
        String message1 = "帮我实现以下文章的总结："+content;
        Message message = new UserMessage(message1);
        List<Message> res = List.of(systemMessage, message);
        String text = openAiClient.generate(new Prompt(res)).getGeneration().getText();
        if (text != null) {
            return AjaxResult.success(text);
        }
        return null;
    }
}
