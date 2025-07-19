package com.module.carpool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CarpoolApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarpoolApplication.class, args);
		System.out.println("H2 database console is running on http://localhost:8080/h2-console");
		System.out.println("server post - 9909");
		System.out.println("Carpool Application started");
	}

}
