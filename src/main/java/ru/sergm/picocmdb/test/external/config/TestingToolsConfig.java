package ru.sergm.picocmdb.test.external.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


@Configuration
public class TestingToolsConfig {

	private static final Logger LOG = LoggerFactory.getLogger(TestingToolsConfig.class);


	@Bean
	public Properties testingProperties() {
		Properties properties = new Properties();
		//System.setProperty("browser", "");
		properties.setProperty("sut.app.url", "");

		LOG.info("Invoked.");
		return properties;
	}

}
