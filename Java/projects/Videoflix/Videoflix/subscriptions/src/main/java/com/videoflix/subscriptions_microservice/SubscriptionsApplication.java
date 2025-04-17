package com.videoflix.subscriptions_microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SubscriptionsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubscriptionsApplication.class, args);
	}

}
