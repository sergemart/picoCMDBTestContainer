package com.github.sergemart.picocmdb.test.external;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SpringBootApplication
public class TestContainerApplication {

	private static final Logger LOG = LoggerFactory.getLogger(TestContainerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(TestContainerApplication.class, args);
		LOG.info("Invoked.");
	}
}
