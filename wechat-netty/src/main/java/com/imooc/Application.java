package com.imooc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication
//@SpringBootApplication(scanBasePackages = {"com.imooc.service"})
// 扫描mybatis mapper包路径
@MapperScan(basePackages="com.imooc.mapper")
// 扫描 所有需要的包, 包含一些自用的工具类包 所在的路径
//@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@ComponentScan(basePackages= {"com.imooc","org.n3r.idworker"})
public class Application {
	
	@Bean
	public SpringUtil getSpringUtil() {
		return new SpringUtil();
	}
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
