package net.northking.atp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
@MapperScan({"net.northking.atp.db.mapper", "net.northking.atp.db.dao"})
@ServletComponentScan
public class ConfigApplication
 extends SpringBootServletInitializer
{
  public static void main(String[] args)
  {
    SpringApplication.run(ConfigApplication.class, args);
  }
  
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder)
  {
    return builder.web(true).sources(new Class[] { ConfigApplication.class });
  }
}
