package com.github.sergemart.picocmdb.test.external.restapi.atomic;

import org.junit.Test;
import io.restassured.http.ContentType;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import com.github.sergemart.picocmdb.test.external.AbstractTests;


public class ConfigurationItemTypeRestApiTests extends AbstractTests {

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
			// create entities, just in case if the database is empty; add tasks to delete these entities after the test
		String entityId1 = "DUMMY" + super.getSalt();
		String entityId2 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO configuration_item_type(id, description) VALUES (?, 'dummy description')", (Object[]) new String[] {entityId1});
		super.jdbcTemplate.update("INSERT INTO configuration_item_type(id, description) VALUES (?, 'dummy description')", (Object[]) new String[] {entityId2});
		super.jdbcCleaner.addTask("DELETE FROM configuration_item_type WHERE (id = ?)", new String[] {entityId1});
		super.jdbcCleaner.addTask("DELETE FROM configuration_item_type WHERE (id = ?)", new String[] {entityId2});
		given().
				//log().all().
		when().
				get(super.baseRestUrl + "/configurationitemtypes").
		then().
				//log().all().
				statusCode(200).								// check envelope
				contentType(ContentType.JSON).
				and().
				body( "size()", greaterThan(1) ).		// check if body is a collection
				body( "get(1)", hasKey("id") ).				// check if 2-nd member has expected fields
				body( "get(1)", hasKey("description") );
	}


	@Test
	public void read_Op_Reads_Entity() {
		// GIVEN
			// create an entity; add task to delete this entity after the test
		String entityId1 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO configuration_item_type(id, description) VALUES (?, 'Тестовое описание.')", (Object[]) new String[] {entityId1});
		super.jdbcCleaner.addTask("DELETE FROM configuration_item_type WHERE (id = ?)", new String[] {entityId1});

		given().
				//log().all().
		when().
				get(super.baseRestUrl + "/configurationitemtypes/" + entityId1).
		then().
				//log().all().
				statusCode(200).											// check envelope
				contentType(ContentType.JSON).
				and().
				body( "id", is(entityId1) ).								// check if service returns just created entity
				body( "description", is("Тестовое описание.") );	// check if UTF-8 chain is not broken
	}


	@Test
	public void read_Op_Reports_When_No_Such_Entity() {
		given().
				//log().all().
				header("Accept-Language", "ru-RU").				// switch language; expected message should be in Russian
		when().
				get(super.baseRestUrl + "/configurationitemtypes/nosuchid").	// no such entity
		then().
				//log().all().
				assertThat().statusCode(400).					// check envelope
				contentType(ContentType.JSON).
				and().
				body( "exceptionName", is("com.github.sergemart.picocmdb.exception.NoSuchObjectException") ).
				body( "errorName", is("CONFIGURATIONITEMTYPENOTFOUND") ).
				body( "localizedMessage", is("Тип конфигурационной единицы не найден.") ). // check if the language is switched
				body( "errorCode", is("1000404") );
	}


	@Test
	public void read_Op_Reads_Dependent_Entity_List() {
		// GIVEN
			// create a parent entity
		String entityId1 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO configuration_item_type(id) VALUES (?)", (Object[]) new String[] {entityId1});
			// create child entities
		String childName1 = "DUMMY" + super.getSalt();
		String childName2 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO configuration_item(name, ci_type_id) VALUES (?, ?)", (Object[]) new String[] {childName1, entityId1});
		super.jdbcTemplate.update("INSERT INTO configuration_item(name, ci_type_id) VALUES (?, ?)", (Object[]) new String[] {childName2, entityId1});
			// add tasks (in right order) to delete test entities after the test
		super.jdbcCleaner.addTask("DELETE FROM configuration_item WHERE (name = ?)", new String[] {childName1});
		super.jdbcCleaner.addTask("DELETE FROM configuration_item WHERE (name = ?)", new String[] {childName2});
		super.jdbcCleaner.addTask("DELETE FROM configuration_item_type WHERE (id = ?)", new String[] {entityId1});
		given().
				//log().all().
		when().
				get(super.baseRestUrl + "/configurationitemtypes/" + entityId1 + "/configurationitems").
			then().
				//log().all().
				statusCode(200).								// check envelope
				contentType(ContentType.JSON).
				and().
				body( "size()", is(2) ).				// check if body is a collection of a right size
				body( "get(1)", hasKey("id") ).				// check if 2-nd member has expected fields
				body( "get(1)", hasKey("name") ).
				body( "get(1)", hasKey("description") ).
				body( "get(1).type.id", is(entityId1) );
	}

	// -------------- CREATE --------------

	@Test
	public void create_Op_Creates_Entity() {
		// GIVEN
			// add task to delete created entity after the test
		String entityId1 = "DUMMY" + super.getSalt();
		super.jdbcCleaner.addTask("DELETE FROM configuration_item_type WHERE (id = ?)", new String[] {entityId1});
			// construct JSON
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("id", entityId1);
		jsonMap.put("description", "Тестовое описание.");

		String receivedEntityDescription = // check it later directly via JDBC
		given().
				//log().all().
				body(jsonMap).
				contentType(ContentType.JSON).
		when().
				post(super.baseRestUrl + "/configurationitemtypes/").
		then().
				//log().all().
				statusCode(200).								// check envelope
				contentType(ContentType.JSON).
				and().
				body( "id", is(entityId1) ).
				body( "description", is("Тестовое описание.") ).	// check if UTF-8 chain is not broken
		extract().
				path("description");

		// extra check directly in database
		String entityDescription = super.jdbcTemplate.queryForObject("SELECT description FROM configuration_item_type WHERE (id = ?)", new String[] {entityId1}, String.class);
		assertThat(entityDescription, is(receivedEntityDescription));
	}


	@Test
	public void create_Op_Reports_When_Entity_With_Same_Id_Exists() {
		// GIVEN
			// create an entity; add task to delete this entity after the test
		String entityId1 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO configuration_item_type(id, description) VALUES (?, 'Тестовое описание.')", (Object[]) new String[] {entityId1});
		super.jdbcCleaner.addTask("DELETE FROM configuration_item_type WHERE (id = ?)", new String[] {entityId1});
		// construct JSON
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("id", entityId1);
		jsonMap.put("description", "Ещё одно описание.");

		given().
				//log().all().
				body(jsonMap).
				contentType(ContentType.JSON).
				header("Accept-Language", "ru-RU"). 			// switch language; expected message should be in Russian
		when().
				post(super.baseRestUrl + "/configurationitemtypes/").
		then().
				//log().all().
				statusCode(400).									// check envelope
				contentType(ContentType.JSON).
				and().
				body( "exceptionName", is("com.github.sergemart.picocmdb.exception.ObjectAlreadyExistsException") ).
				body( "errorName", is("CONFIGURATIONITEMTYPEEXISTS") ).
				body( "localizedMessage", is("Тип конфигурационной единицы уже существует.") ). // check if the language is switched
				body( "errorCode", is("1000405") );
	}


	@Test
	public void create_Op_Reports_When_JSON_Has_Wrong_Schema() {
			// construct JSON
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("badfield1", "badfieldvalue");
		jsonMap.put("badfield2", "Некое значение.");

		given().
				//log().all().
				body(jsonMap).
				contentType(ContentType.JSON).
				header("Accept-Language", "ru-RU").				// switch language; expected message should be in Russian
		when().
				post(super.baseRestUrl + "/configurationitemtypes/").
		then().
				//log().all().
				statusCode(400).								// check envelope
				contentType(ContentType.JSON).
				and().
				body( "exceptionName", is("com.github.sergemart.picocmdb.exception.WrongDataException") ).
				body( "errorName", is("CONFIGURATIONITEMTYPEBAD") ).
				body( "localizedMessage", is("Тип конфигурационной единицы содержит неверные данные.") ). // check if the language is switched
				body( "errorCode", is("1000500") );
	}

	// -------------- UPDATE --------------

	@Test
	public void update_Op_Updates_Entity() {
		// GIVEN
			// create an entity to be updated; add task to delete the entity after the test
		String entityId1 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO configuration_item_type(id, description) VALUES (?, 'Тестовое описание.')", (Object[]) new String[] {entityId1});
		super.jdbcCleaner.addTask("DELETE FROM configuration_item_type WHERE (id = ?)", new String[] {entityId1});
			// construct JSON
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("description", "Изменённое тестовое описание.");

		given().
				//log().all().
				body(jsonMap).
				contentType(ContentType.JSON).
		when().
				put(super.baseRestUrl + "/configurationitemtypes/" + entityId1).
		then().
				//log().all().
				statusCode(200).								// check envelope
				contentType(ContentType.JSON).
				and().
				body( "id", is(entityId1) ).
				body( "description", is("Изменённое тестовое описание.") );	// check if UTF-8 chain is not broken

		// extra check directly in database
		Map<String, Object> modifiedEntity = super.jdbcTemplate.queryForMap("SELECT id, description FROM configuration_item_type WHERE (id = ?)", (Object[])new String[] {entityId1});
		assertThat(modifiedEntity.get("description"), is("Изменённое тестовое описание."));
	}


	// -------------- DELETE --------------

	@Test
	public void delete_Op_Deletes_Entity() {
		// GIVEN
			// create an entity to be deleted; add task to delete this entity after the test, just in case if delete fails for any reason
		String entityId1 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO configuration_item_type(id, description) VALUES (?, 'Тестовое описание.')", (Object[]) new String[] {entityId1});
		super.jdbcCleaner.addTask("DELETE FROM configuration_item_type WHERE (id = ?)", new String[] {entityId1});

		given().
				//log().all().
		when().
				delete(super.baseRestUrl + "/configurationitemtypes/" + entityId1).
		then().
				//log().all().
				statusCode(200);									// check envelope

		// extra check directly in database that the entity is deleted
		Integer entityCount = super.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM configuration_item_type WHERE (id = ?)", new String[] {entityId1}, Integer.class);
		assertThat(entityCount, is(0));
	}


	@Test
	public void delete_Op_Reports_When_No_Such_Entity() {
		given().
				//log().all().
				header("Accept-Language", "ru-RU").                // switch language; expected message should be in Russian
		when().
				delete(super.baseRestUrl + "/configurationitemtypes/nosuchid").    // no such entity
		then().
				//log().all().
				assertThat().statusCode(400).                    // check envelope
				contentType(ContentType.JSON).
				and().
				body("exceptionName", is("com.github.sergemart.picocmdb.exception.NoSuchObjectException")).
				body("errorName", is("CONFIGURATIONITEMTYPENOTFOUND")).
				body("localizedMessage", is("Тип конфигурационной единицы не найден.")). // check if the language is switched
				body("errorCode", is("1000404"));
	}


	@Test
	public void delete_Op_Reports_When_Dependent_Entity_Exists() {
		// GIVEN
			// create a parent entity which would be deleted
		String entityId1 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO configuration_item_type(id) VALUES (?)", (Object[]) new String[] {entityId1});
			// create a child entity
		String childName1 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO configuration_item(name, ci_type_id) VALUES (?, ?)", (Object[]) new String[] {childName1, entityId1});
			// add tasks (in right order) to delete test entities after the test
		super.jdbcCleaner.addTask("DELETE FROM configuration_item WHERE (name = ?)", new String[] {childName1});
		super.jdbcCleaner.addTask("DELETE FROM configuration_item_type WHERE (id = ?)", new String[] {entityId1});

		given().
				//log().all().
				header("Accept-Language", "ru-RU").                // switch language; expected message should be in Russian
		when().
				delete(super.baseRestUrl + "/configurationitemtypes/" + entityId1).
		then().
				//log().all().
				assertThat().statusCode(400).                    // check envelope
				contentType(ContentType.JSON).
				and().
				body("exceptionName", is("com.github.sergemart.picocmdb.exception.DependencyExistsException")).
				body("errorName", is("CONFIGURATIONITEMEXISTS")).
				body("localizedMessage", is("Существуют зависимые конфигурационные единицы.")). // check if the language is switched
				body("errorCode", is("1000406"));
	}

}
