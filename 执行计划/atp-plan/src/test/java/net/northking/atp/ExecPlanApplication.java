package net.northking.atp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 执行测试计划单元测试应用启动类
 */
@SpringBootApplication
@EnableEurekaClient
@MapperScan({"net.northking.atp.db.mapper", "net.northking.atp.db.dao"})
@EnableScheduling
@EnableFeignClients
public class ExecPlanApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExecPlanApplication.class, args);
    }
}
