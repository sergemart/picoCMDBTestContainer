package com.github.sergemart.picocmdb.test.external.restapi.atomic;

import org.junit.Test;
import io.restassured.http.ContentType;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import com.github.sergemart.picocmdb.test.external.AbstractTests;


public class ManagedAreaRestApiTests extends AbstractTests {

	@Test
	public void test_Suite_Prerequisites_Initialized() {
		assertThat(super.baseRestUrl, not(isEmptyOrNullString()));
		assertThat(super.jdbcTemplate, not(is(nullValue())));
		assertThat(super.jdbcCleaner, not(is(nullValue())));
	}

	// -------------- READ --------------

	@Test
	public void read_Op_Reads_Entity_List() {
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
	public void read_Op_Reads_Entity() {
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
				body( "id", equalTo(Integer.parseInt(entityId1)) ).				// check if service returns just created entity
				body( "name", equalTo(entityName1) ).
				body( "description", equalTo("Тестовое описание.") );		// check if UTF-8 chain is not broken
	}


	@Test
	public void read_Op_Reports_When_No_Entity_Found() {
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

	// -------------- CREATE --------------

	@Test
	public void create_Op_Creates_Entity() {
		// GIVEN
			// prepare ID and add task to delete created entity
		String entityName1 = "DUMMY" + super.getSalt();
		super.jdbcCleaner.addTask("DELETE FROM managed_area WHERE (name = ?)", new String[] {entityName1});
			// construct JSON
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("name", entityName1);
		jsonMap.put("description", "Тестовое описание.");

		Integer receivedEntityId1 = // to check it later directly via JDBC
		given().
				//log().all().
				body(jsonMap).
				contentType(ContentType.JSON).
		when().
				post(super.baseRestUrl + "/managedareas/").
		then().
				//log().all().
				statusCode(200).								// check envelope
				contentType(ContentType.JSON).
				and().
				body( "", hasKey("id") ).   									// check if service returns just created entity
				body( "name", equalTo(entityName1) ).
				body( "description", equalTo("Тестовое описание.") ).	// check if UTF-8 chain is not broken
		 extract().
				path("id");

		// to extra check directly in database
		Integer entityId1 = super.jdbcTemplate.queryForObject("SELECT id FROM managed_area WHERE (name = ?)", new String[] {entityName1}, Integer.class);
		assertThat(entityId1, is(receivedEntityId1));
	}


	@Test
	public void create_Op_Reports_When_Entity_With_Same_Name_Exists() {
		// GIVEN
			// prepare ID, create entity w/ the same name and add task to delete this entity
		String entityName1 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO managed_area(name, description) VALUES (?, 'Тестовое описание.')", (Object[]) new String[] {entityName1});
		super.jdbcCleaner.addTask("DELETE FROM managed_area WHERE (name = ?)", new String[] {entityName1});
		// construct JSON
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("name", entityName1);
		jsonMap.put("description", "Ещё одно описание.");

		given().
				log().all().
				body(jsonMap).
				contentType(ContentType.JSON).
				header("Accept-Language", "ru-RU"). 			// to switch language; expected message should be in Russian
		when().
				post(super.baseRestUrl + "/managedareas/").
		then().
				log().all().
				statusCode(400).									// check envelope
				contentType(ContentType.JSON).
				and().
				body( "exceptionName", equalTo("com.github.sergemart.picocmdb.exception.ObjectAlreadyExistsException") ).
				body( "errorName", equalTo("MANAGEDAREAEXISTS") ).
				body( "localizedMessage", equalTo("Область управления уже существует.") ). // check if the language is switched
				body( "errorCode", equalTo("1000405") );
	}


	@Test
	public void create_Op_Reports_When_JSON_Has_Wrong_Schema() {
			// construct JSON
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("badfield1", "badfieldvalue");
		jsonMap.put("badfield2", "Некое значение.");

		given().
				log().all().
				body(jsonMap).
				contentType(ContentType.JSON).
				header("Accept-Language", "ru-RU").				// to switch language; expected message should be in Russian
		when().
				post(super.baseRestUrl + "/managedareas/").
		then().
				log().all().
				statusCode(400).								// check envelope
				contentType(ContentType.JSON).
				and().
				body( "exceptionName", equalTo("com.github.sergemart.picocmdb.exception.WrongDataException") ).
				body( "errorName", equalTo("MANAGEDAREABAD") ).
				body( "localizedMessage", equalTo("Область управления содержит неверные данные.") ). // check if the language is switched
				body( "errorCode", equalTo("1000500") );
	}


}
