package com.xuelian.career;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI智能求职辅导平台启动类
 * 扫描 com.xuelian.career.mapper 包下的所有 Mapper 接口
 */
@SpringBootApplication
@MapperScan("com.xuelian.career.mapper")
public class CareerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareerApplication.class, args);
    }
}
