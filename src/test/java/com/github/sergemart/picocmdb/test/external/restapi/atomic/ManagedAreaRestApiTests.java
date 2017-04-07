package com.github.sergemart.picocmdb.test.external.restapi.atomic;

import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.Matchers.*;
import io.restassured.http.ContentType;
import static io.restassured.RestAssured.given;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

import com.github.sergemart.picocmdb.test.external.AbstractTests;


public class ManagedAreaRestApiTests extends AbstractTests {

	private static final Logger LOG = LoggerFactory.getLogger(ManagedAreaRestApiTests.class);

	@Test
	public void test_Suite_Prerequisites_Initialized() {
		assertNotNull(super.baseRestUrl);
		assertNotNull(super.jdbcTemplate);
		assertNotNull(super.jdbcCleaner);
	}


	@Test
	public void service_Returns_ManagedArea_List() {
		// GIVEN
			// create entities, just in case if the database is empty
		String entityName1 = "DUMMY" + super.getSalt();
		String entityName2 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO managed_area(name, description) VALUES (?, 'dummy description')", (Object[]) new String[] {entityName1});
		super.jdbcTemplate.update("INSERT INTO managed_area(name, description) VALUES (?, 'dummy description')", (Object[]) new String[] {entityName2});
		super.jdbcCleaner.addTask("DELETE FROM managed_area WHERE (name = ?)", new String[] {entityName1});
		super.jdbcCleaner.addTask("DELETE FROM managed_area WHERE (name = ?)", new String[] {entityName2});
		given().
				//log().all().
		when().
				get(super.baseRestUrl + "/managedareas").
		then().
				//log().all().
				statusCode(200).								// check envelope
				contentType(ContentType.JSON).
				and().
				body( "size()", greaterThan(1) ).		// check if body is a collection
				body( "get(1)", hasKey("id") ).				// check if 2-nd member has expected fields
				body( "get(1)", hasKey("name") ).
				body( "get(1)", hasKey("description") );
	}


	@Test
	public void service_Returns_ManagedArea() {
		// GIVEN
			// create entities, just in case if the database is empty
		String entityName1 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO managed_area(name, description) VALUES (?, 'Тестовое описание.')", (Object[]) new String[] {entityName1});
			// get auto-generated entity ID
		String entityId1 = super.jdbcTemplate.queryForObject("SELECT id FROM managed_area WHERE (name = ?)", new String[] {entityName1}, String.class);
		super.jdbcCleaner.addTask("DELETE FROM managed_area WHERE (name = ?)", new String[] {entityName1});

		given().
				//log().all().
		when().
				get(super.baseRestUrl + "/managedareas/" + entityId1).
		then().
				//log().all().
				statusCode(200).								// check envelope
				contentType(ContentType.JSON).
				and().
				body( "id", equalTo(Integer.parseInt(entityId1)) ).				// check if API returns just created entity
				body( "name", equalTo(entityName1) ).
				body( "description", equalTo("Тестовое описание.") );		// check if UTF-8 chain is not broken
	}


	@Test
	public void service_Returns_Error_When_No_ManagedArea_Found() {
		given().
				log().all().
				header("Accept-Language", "ru-RU").				// to switch language; expected message should be in Russian
		when().
				get(super.baseRestUrl + "/managedareas/nosuchid").	// no such entity
		then().
				log().all().
				assertThat().statusCode(400).					// check envelope
				contentType(ContentType.JSON).
				and().
				body( "exceptionName", equalTo("com.github.sergemart.picocmdb.exception.NoSuchObjectException") ).
				body( "errorName", equalTo("MANAGEDAREANOTFOUND") ).
				body( "localizedMessage", equalTo("Область управления не найдена.") ). // check if the language is switched
				body( "errorCode", equalTo("1000404") );
	}


	@Test
	public void service_Creates_ManagedArea() {
		// GIVEN
			// prepare ID and add task to delete created entity
		String entityName1 = "DUMMY" + super.getSalt();
		super.jdbcCleaner.addTask("DELETE FROM managed_area WHERE (name = ?)", new String[] {entityName1});
			// construct JSON
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("name", entityName1);
		jsonMap.put("description", "Тестовое описание.");

		Integer receivedEntityId1 =
		given().
				log().all().
				body(jsonMap).
				contentType(ContentType.JSON).
		when().
				post(super.baseRestUrl + "/managedareas/").
		then().
				log().all().
				statusCode(200).								// check envelope
				contentType(ContentType.JSON).
				and().
				body( "", hasKey("id") ).   									// check if API returns just created entity
				body( "name", equalTo(entityName1) ).
				body( "description", equalTo("Тестовое описание.") ).	// check if UTF-8 chain is not broken
		 extract().
				path("id");

		// to check directly in database
		Integer entityId1 = super.jdbcTemplate.queryForObject("SELECT id FROM managed_area WHERE (name = ?)", new String[] {entityName1}, Integer.class);
		assertEquals(entityId1, receivedEntityId1);
	}


}
