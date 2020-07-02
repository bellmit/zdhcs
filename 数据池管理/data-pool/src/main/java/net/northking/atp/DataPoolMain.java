package net.northking.atp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableEurekaClient
@ServletComponentScan
@EnableTransactionManagement
@MapperScan({"net.northking.atp.db.mapper", "net.northking.atp.db.dao"})
public class DataPoolMain {


    public static void main(String[] args)
    {
        SpringApplication.run(DataPoolMain.class, args);

    }
}
