package com.github.sergemart.picocmdb.test.external.restapi.atomic;

import io.restassured.http.ContentType;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static io.restassured.RestAssured.given;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sergemart.picocmdb.test.external.AbstractTests;


public class RoleRestApiTests extends AbstractTests {

	private static final Logger LOG = LoggerFactory.getLogger(RoleRestApiTests.class);


	@Test
	public void test_Suite_Prerequisites_Initialized() {
		assertNotNull(super.baseRestUrl);
		assertNotNull(super.jdbcTemplate);
		assertNotNull(super.jdbcCleaner);
	}


	@Test
	public void service_Returns_Role_List() {
		// GIVEN
			// create entities, just in case if the database is empty
		String entityId1 = "DUMMY" + super.getSalt();
		String entityId2 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO role(id, description, is_system) VALUES (?, 'dummy description', true)", (Object[]) new String[] {entityId1});
		super.jdbcTemplate.update("INSERT INTO role(id, description, is_system) VALUES (?, 'dummy description', true)", (Object[]) new String[] {entityId2});
		super.jdbcCleaner.addTask("DELETE FROM role WHERE (id = ?)", new String[] {entityId1});
		super.jdbcCleaner.addTask("DELETE FROM role WHERE (id = ?)", new String[] {entityId2});
		given().
				//log().all().
		when().
				get(super.baseRestUrl + "/roles").
		then().
				//log().all().
				statusCode(200).								// check envelope
				contentType(ContentType.JSON).
				and().
				body( "size()", greaterThan(1) ).		// check if body is a collection
				body( "get(1)", hasKey("id") ).				// check if 2-nd member has expected fields
				body( "get(1)", hasKey("description") ).
				body( "get(1)", hasKey("system") );
	}


	@Test
	public void service_Returns_Role() {
		// GIVEN
			// create entities, just in case if the database is empty
		String entityId1 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO role(id, description, is_system) VALUES (?, 'тестовое описание', true)", (Object[]) new String[] {entityId1});
		super.jdbcCleaner.addTask("DELETE FROM role WHERE (id = ?)", new String[] {entityId1});

		given().
				//log().all().
		when().
				get(super.baseRestUrl + "/roles/" + entityId1).
		then().
				//log().all().
				statusCode(200).								// check envelope
				contentType(ContentType.JSON).
				and().
				body( "id", equalTo(entityId1) ).								// check if API returns just created entity
				body( "description", equalTo("тестовое описание") ).		// check if UTF-8 chain is not broken
				body( "system", equalTo(true) );
	}


	@Test
	public void service_Returns_Error_When_No_Role_Found() {
		given().
				//log().all().
				header("Accept-Language", "ru-RU").		// to switch language; expected message should be in Russian
		when().
				get(super.baseRestUrl + "/roles/nosuchid").	// no such entity
		then().
				//log().all().
				assertThat().statusCode(400).					// check envelope
				contentType(ContentType.JSON).
				and().
				body( "exceptionName", equalTo("com.github.sergemart.picocmdb.exception.NoSuchObjectException") ).
				body( "errorName", equalTo("ROLENOTFOUND") ).
				body( "localizedMessage", equalTo("Роль не найдена.") ). // check if the language is switched
				body( "errorCode", equalTo("1000404") );
	}

}
