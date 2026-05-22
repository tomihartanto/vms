package com.asset.VMS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class VmsApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(VmsApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(VmsApplication.class);
	}

	@Configuration
	@EnableScheduling
	@ConditionalOnProperty(prefix = "spring.task.scheduling", name = "enabled", havingValue = "true")
	static class SchedulingConfig {
	}
}
