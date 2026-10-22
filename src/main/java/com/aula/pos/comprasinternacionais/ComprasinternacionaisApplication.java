package com.aula.pos.comprasinternacionais;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class ComprasinternacionaisApplication {

	public static void main(String[] args) {
		SpringApplication.run(ComprasinternacionaisApplication.class, args);
	}

}
