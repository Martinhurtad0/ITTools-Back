package com.example.ITTools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@SpringBootApplication
@EnableScheduling
public class ItToolsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItToolsApplication.class, args);


	}



}
