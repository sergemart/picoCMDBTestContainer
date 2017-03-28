package ru.sergm.picocmdb.test.external.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.Properties;


@Configuration
@PropertySource("classpath:testing.properties")
public class TestingToolsConfig {

	private static final Logger LOG = LoggerFactory.getLogger(TestingToolsConfig.class);

	@Autowired
	private Environment env;


	@PostConstruct
	private void initSystemProperties() {
		System.setProperty("browser", env.getProperty("testing.browser")); // for Selenide
	}

}
