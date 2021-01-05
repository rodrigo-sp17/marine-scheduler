package com.github.rodrigo_sp17.mscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class MarinerSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarinerSchedulerApplication.class, args);
	}

}
