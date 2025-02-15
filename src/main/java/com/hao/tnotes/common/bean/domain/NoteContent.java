package com.hao.tnotes.common.bean.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;



@Document(collection = "note_content")
@Data
public class NoteContent {

    @Id
    private String id;
    private Object content;

}
