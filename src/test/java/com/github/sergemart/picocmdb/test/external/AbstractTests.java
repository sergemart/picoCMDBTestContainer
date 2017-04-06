package com.github.sergemart.picocmdb.test.external;

import com.github.sergemart.picocmdb.test.external.junitrule.JdbcCleaner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.atomic.AtomicInteger;


@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class AbstractTests {

	private static AtomicInteger counter = new AtomicInteger(0);
	protected String baseUiUrl;
	protected String baseRestUrl;
	@Autowired
	private Environment env;
	@Autowired
	protected JdbcTemplate jdbcTemplate;
	@Rule
	public final JdbcCleaner jdbcCleaner = new JdbcCleaner();


	// to make unique entities' IDs to prevent constraint violation when test executed in parallel
	protected String getSalt() {
		return String.valueOf(counter.incrementAndGet());
	}


	@Before
	public void basicSetUp() { // attn: do not override
		this.baseUiUrl = this.env.getProperty("testing.sut.ui.url");
		this.baseRestUrl = this.env.getProperty("testing.sut.rest.url");

		// to inject beans to rules (rules are instantiated before Spring context loads)
		jdbcCleaner.setJdbcTemplate(jdbcTemplate);
	}

}
