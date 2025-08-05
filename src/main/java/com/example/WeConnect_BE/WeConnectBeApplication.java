package com.example.WeConnect_BE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(proxyBeanMethods = false)
public class WeConnectBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeConnectBeApplication.class, args);
	}

}
