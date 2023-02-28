package com.stock.mx2;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan(basePackages = "com.stock.mx2.dao")
@EnableScheduling
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class Mx2Application {

    public static void main(String[] args) {
        SpringApplication.run(Mx2Application.class, args);
    }

}
