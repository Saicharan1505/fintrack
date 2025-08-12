package com.fintrack;

import org.springframework.boot.SpringApplication;

public class TestFintrackApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(FintrackApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
