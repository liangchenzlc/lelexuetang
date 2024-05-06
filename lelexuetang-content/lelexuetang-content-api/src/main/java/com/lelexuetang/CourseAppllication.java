package com.lelexuetang;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableSwagger2Doc
@SpringBootApplication
@MapperScan("com.lelexuetang.content.mapper")
public class CourseAppllication {
    public static void main(String[] args) {
        SpringApplication.run(CourseAppllication.class,args);
    }
}
