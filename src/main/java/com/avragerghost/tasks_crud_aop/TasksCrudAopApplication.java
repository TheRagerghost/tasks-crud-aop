package com.avragerghost.tasks_crud_aop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class TasksCrudAopApplication {

	public static void main(String[] args) {
		SpringApplication.run(TasksCrudAopApplication.class, args);
	}

}
