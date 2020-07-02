package net.northking.atp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import feign.Request;
import feign.Retryer;

@SpringBootApplication
@EnableDiscoveryClient
@EnableEurekaClient
@EnableFeignClients
@EnableScheduling
@MapperScan({"net.northking.atp.db.mapper", "net.northking.atp.db.dao"})
public class CaseDesignApplication
{
  public static void main(String[] args)
  {
    SpringApplication.run(CaseDesignApplication.class, args);
  }
  
  
  @Bean
	Request.Options feignOptions() {

		return new Request.Options(/**connectTimeoutMillis**/30 * 1000, /** readTimeoutMillis **/30 * 1000);
	}

	@Bean
	Retryer feignRetryer() {
		return Retryer.NEVER_RETRY;
	}
}
