package com.hutu;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hutu.**.mapper")
@EnableMethodCache(basePackages = "com.hutu")
public class Application {

    public static void main(String[] args){
        SpringApplication.run(Application.class);
    }

}