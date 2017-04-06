package ru.sergm.picocmdb.test.external;

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import ru.sergm.picocmdb.test.external.junitrule.JdbcCleaner;
import ru.sergm.picocmdb.test.external.restapi.atomic.RoleRestApiTests_SelfServiced;

import java.util.concurrent.atomic.AtomicInteger;


@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class AbstractTests {

	private static AtomicInteger counter = new AtomicInteger(0);
	@Autowired
	protected Environment env;
	@Autowired
	protected JdbcTemplate jdbcTemplate;
	@Rule
	public final JdbcCleaner jdbcCleaner = new JdbcCleaner();


	// to make unique entities' IDs to prevent constraint violation when test executed in parallel
	protected String getSalt() {
		return String.valueOf(counter.incrementAndGet());
	}


	// to inject beans to rules (rules are instantiated before Spring context loads)
	@Before
	public void setUpRules() {
		jdbcCleaner.setJdbcTemplate(jdbcTemplate);
	}

}
