package net.northking.cloudtest;


import feign.Request;
import feign.Retryer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@MapperScan("net.northking.cloudtest.dao")
@SpringBootApplication
@EnableEurekaClient
@EnableTransactionManagement
@EnableFeignClients
public class CloudtestProjectApplication  {

	public static void main(String[] args) {
		SpringApplication.run(CloudtestProjectApplication.class, args);
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
