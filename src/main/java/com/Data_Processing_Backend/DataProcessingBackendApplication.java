package com.Data_Processing_Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.Data_Processing_Backend.config.ImportProperties;

@SpringBootApplication
@EnableConfigurationProperties(ImportProperties.class)
public class DataProcessingBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataProcessingBackendApplication.class, args);
	}

}
