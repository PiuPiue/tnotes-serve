package com.hao.tnotes;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hao.tnotes.*.dao")
public class TNotesApplication {

    public static void main(String[] args) {
        SpringApplication.run(TNotesApplication.class, args);
    }

}
