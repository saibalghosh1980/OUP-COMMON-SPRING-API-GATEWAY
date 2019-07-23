package com.oup.apiproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("com.oup")
public class OupSpringApiGatewayApplication {

	public static void main(String[] args) {
		
		SpringApplication.run(OupSpringApiGatewayApplication.class, args);
	}
}
