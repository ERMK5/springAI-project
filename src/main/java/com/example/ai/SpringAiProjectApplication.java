package com.example.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.example.ai.mapper")
@SpringBootApplication
public class SpringAiProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiProjectApplication.class, args);
    }

}
