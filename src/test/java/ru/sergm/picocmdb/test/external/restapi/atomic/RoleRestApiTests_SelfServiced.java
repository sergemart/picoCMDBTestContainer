package ru.sergm.picocmdb.test.external.restapi.atomic;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sergm.picocmdb.test.external.AbstractTests;
import ru.sergm.picocmdb.test.external.junitrule.JdbcCleaner;

import static org.junit.Assert.assertNotNull;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;


public class RoleRestApiTests_SelfServiced extends AbstractTests {

	private static final Logger LOG = LoggerFactory.getLogger(RoleRestApiTests_SelfServiced.class);
	private String baseUrl;


	@Before
	public void setUp() {
		this.baseUrl = super.env.getProperty("testing.sut.rest.url");
	}


	@Test
	public void test_Suite_Prerequisites_Initialized() {
		assertNotNull(this.baseUrl);
		assertNotNull(super.jdbcTemplate);
	}


	@Test
	public void service_Returns_Role_List() {
		// GIVEN
		String entityId1 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO role(id, description, is_system) VALUES (?, 'dummy description', true)", (Object[]) new String[] {entityId1});
		super.jdbcCleaner.addTask("DELETE FROM role WHERE (id LIKE ?)", new String[] {entityId1});

		when().
				get(this.baseUrl + "/roles").
		then().
				assertThat().statusCode(200).
				and().
				assertThat().body( "get(0).id", equalTo("ADMINISTRATOR") ).
				assertThat().body( "get(0).system", equalTo(true) );
	}


	@Test
	public void service_Returns_Role() {
		when().
				get(this.baseUrl + "/roles/administrator").
		then().
				assertThat().statusCode(200).
				and().
				assertThat().body( "id", equalTo("ADMINISTRATOR") ).
				assertThat().body( "system", equalTo(true) );
	}


	@Test
	public void service_Returns_Error_When_No_Role_Found() {
		given().
				log().all().
				header("Accept-Language", "ru-RU").
		when().
				get(this.baseUrl + "/roles/administrator22").
		then().
				log().all().
				assertThat().statusCode(400).
				and().
				assertThat().body( "exceptionName", equalTo("ru.sergm.picocmdb.exception.NoSuchObjectException") ).
				assertThat().body( "errorName", equalTo("ROLENOTFOUND") ).
				assertThat().body( "errorCode", equalTo("1000404") );
	}

}
