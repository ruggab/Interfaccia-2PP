package it.duepassicalzature.interfaccia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class InterfacciaApplication  {

	public static void main(String[] args) {
		SpringApplication.run(InterfacciaApplication.class, args);

	}

}
