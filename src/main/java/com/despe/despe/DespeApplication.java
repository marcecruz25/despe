package com.despe.despe;

import com.despe.despe.service.DespegarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class DespeApplication implements CommandLineRunner {

	@Autowired
	private DespegarService despegarService;

	public static void main(String[] args) {
		SpringApplication.run(DespeApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
		despegarService.startApp();
	}
}
