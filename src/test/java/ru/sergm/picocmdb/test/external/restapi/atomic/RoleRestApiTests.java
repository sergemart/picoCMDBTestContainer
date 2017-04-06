package ru.sergm.picocmdb.test.external.restapi.atomic;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.Before;
import ru.sergm.picocmdb.test.external.AbstractTests;


public class RoleRestApiTests extends AbstractTests {

	private String baseUrl;


	@Before
	public void setUp() {
		this.baseUrl = super.env.getProperty("testing.sut.rest.url");
	}


	@Test
	public void test_Suite_Data_Initialized() {
		assertNotNull(this.baseUrl);
	}


	@Test
	public void service_Returns_Role_List() {
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
