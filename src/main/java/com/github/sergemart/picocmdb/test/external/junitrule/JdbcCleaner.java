package com.github.sergemart.picocmdb.test.external.junitrule;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;


/**
 * Provides an option to manage (mainly delete) database entities after a test completion.
 * The test should invoke JdbcCleaner.addTask() to register JDBC query to be executed after the test.
 */
public class JdbcCleaner implements TestRule {

	private static final Logger LOG = LoggerFactory.getLogger(JdbcCleaner.class);
	private JdbcTemplate jdbcTemplate;
	// database queries (mainly 'DELETE's) to be executed after a test
	private final List<JdbcTask> jdbcTaskList = new ArrayList<>();


	// separate setter required to init JdbcCleaner after Spring context loads (JUnit instantiates @Rule before Spring context loads)
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	// register a new task; should be called in a test
	public void addTask (String sqlQuery, Object[] sqlQueryParameters) {
		this.jdbcTaskList.add(new JdbcTask(sqlQuery, sqlQueryParameters));
	}


	@Override
	public Statement apply(final Statement base, final Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				try {
					base.evaluate();
				} finally {
					if (jdbcTemplate == null) LOG.error("JdbcTemplate is not set.");
					// iterate over execute parameters have been set in a test
					for (JdbcTask jdbcTask : jdbcTaskList) {
						jdbcTemplate.update(jdbcTask.getSqlQuery(), jdbcTask.getSqlQueryParameters());
						LOG.info("Executed SQL: {}", jdbcTask.toString());
					}
				}
			}

		};
	}

}
