package com.module.carpool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CarpoolApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarpoolApplication.class, args);
		System.out.println("Carpool Application started");
	}

}
