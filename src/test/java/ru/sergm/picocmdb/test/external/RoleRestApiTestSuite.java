package ru.sergm.picocmdb.test.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;


@RunWith(SpringRunner.class)
@SpringBootTest
public class RoleRestApiTestSuite {

	@Autowired
	private Environment env;

	private String baseUrl;


	@Before
	public void setUp() {
		this.baseUrl = env.getProperty("testing.sut.rest.url");
	}


	@Test
	public void test_Suite_Data_Initialized() {
		assertNotNull(this.baseUrl);
	}


	@Test
	public void service_Returns_Role_List() {
		when().
				get(baseUrl + "/roles").
		then().
				assertThat().statusCode(200).
				and().
				assertThat().body( "get(0).id", equalTo("ADMINISTRATOR") ).
				assertThat().body( "get(0).system", equalTo(true) );
	}


	@Test
	public void service_Returns_Role() {
		when().
				get(baseUrl + "/roles/administrator").
		then().
				assertThat().statusCode(200).
				and().
				assertThat().body( "id", equalTo("ADMINISTRATOR") ).
				assertThat().body( "system", equalTo(true) );
	}


	@Test
	public void service_Returns_Error_When_No_Role_Found() {
		when().
				get(baseUrl + "/roles/administrator22").
		then().
				assertThat().statusCode(400).
				and().
				assertThat().body( "exceptionName", equalTo("ru.sergm.picocmdb.exception.NoSuchObjectException") ).
				assertThat().body( "errorCode", equalTo("1000404") );
	}

}
