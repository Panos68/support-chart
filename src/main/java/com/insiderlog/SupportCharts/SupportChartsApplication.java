package com.insiderlog.SupportCharts;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableEncryptableProperties
public class SupportChartsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SupportChartsApplication.class, args);
	}

}
