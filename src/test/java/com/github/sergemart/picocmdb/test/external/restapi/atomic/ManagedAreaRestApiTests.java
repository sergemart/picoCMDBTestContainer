package com.github.sergemart.picocmdb.test.external.restapi.atomic;

import org.junit.Test;
import io.restassured.http.ContentType;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.List;
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
			// create entities, just in case if the database is empty; add tasks to delete these entities after the test
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
			// create an entity; add task to delete this entity after the test
		String entityName1 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO managed_area(name, description) VALUES (?, 'Тестовое описание.')", (Object[]) new String[] {entityName1});
		super.jdbcCleaner.addTask("DELETE FROM managed_area WHERE (name = ?)", new String[] {entityName1});
			// get auto-generated entity ID
		Integer entityId1 = super.jdbcTemplate.queryForObject("SELECT id FROM managed_area WHERE (name = ?)", new String[] {entityName1}, Integer.class);

		given().
				//log().all().
		when().
				get(super.baseRestUrl + "/managedareas/" + entityId1).
		then().
				//log().all().
				statusCode(200).											// check envelope
				contentType(ContentType.JSON).
				and().
				body( "id", is(entityId1) ).								// check if service returns just created entity
				body( "name", is(entityName1) ).
				body( "description", is("Тестовое описание.") );	// check if UTF-8 chain is not broken
	}


	@Test
	public void read_Op_Reads_Linked_Entity_List() {
		// GIVEN
			// create a tested entity; this entity will be deleted on rollback after the test
		String entityName1 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO managed_area(name) VALUES (?)", (Object[]) new String[] {entityName1});
			// get auto-generated ID of the created tested entity
		Long entityId1 = super.jdbcTemplate.queryForObject("SELECT id FROM managed_area WHERE (name = ?)", new String[] {entityName1}, Long.class);
			// create parent (classifier) entity for linked entities; the entity will be deleted on rollback after the test
		String parentId1 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO configuration_item_type(id) VALUES (?)", (Object[]) new String[] {parentId1});
			// create will-be-linked entities; the entities will be deleted on rollback after the test
		String linkedName1 = "DUMMY" + super.getSalt();
		String linkedName2 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO configuration_item(name, ci_type_id) VALUES (?, ?)", (Object[]) new String[]{linkedName1, parentId1});
		super.jdbcTemplate.update("INSERT INTO configuration_item(name, ci_type_id) VALUES (?, ?)", (Object[]) new String[]{linkedName2, parentId1});
			// get auto-generated IDs of created will-be-linked entities
		Long linkedId1 = super.jdbcTemplate.queryForObject("SELECT id FROM configuration_item WHERE (name = ?)", new String[] {linkedName1}, Long.class);
		Long linkedId2 = super.jdbcTemplate.queryForObject("SELECT id FROM configuration_item WHERE (name = ?)", new String[] {linkedName2}, Long.class);
			// create links between the tested entity and will-be-linked entities; these links will be deleted on rollback after the test
		super.jdbcTemplate.update("INSERT INTO ci_marea_link(ci_id, marea_id) VALUES (?, ?)", (Object[]) new Long[] {linkedId1, entityId1});
		super.jdbcTemplate.update("INSERT INTO ci_marea_link(ci_id, marea_id) VALUES (?, ?)", (Object[]) new Long[] {linkedId2, entityId1});
			// add tasks (in right order) to delete test entities after the test
		super.jdbcCleaner.addTask("DELETE FROM ci_marea_link WHERE (marea_id = ?)", new Long[] {entityId1});
		super.jdbcCleaner.addTask("DELETE FROM configuration_item WHERE (ci_type_id = ?)", new String[] {parentId1});
		super.jdbcCleaner.addTask("DELETE FROM configuration_item_type WHERE (id = ?)", new String[] {parentId1});
		super.jdbcCleaner.addTask("DELETE FROM managed_area WHERE (name = ?)", new String[] {entityName1});
		given().
				log().all().
		when().
				get(super.baseRestUrl + "/managedareas/" + entityId1 + "/configurationitems").
		then().
				log().all().
				statusCode(200).						// check envelope
				contentType(ContentType.JSON).
				and().
				body( "size()", is(2) ).				// check if body is a collection of a right size
				body( "get(1)", hasKey("id") ).				// check if 2-nd member has expected fields
				body( "get(1)", hasKey("name") ).
				body( "get(1)", hasKey("description") ).
				body( "get(1)", hasKey("type") ).
				body( "get(1).type", hasKey("id") ).			// check if the nested object has expected fields
				body( "get(1).type", hasKey("description") );
	}


	@Test
	public void read_Op_Reports_When_No_Such_Entity() {
		given().
				//log().all().
				header("Accept-Language", "ru-RU").				// switch language; expected message should be in Russian
		when().
				get(super.baseRestUrl + "/managedareas/nosuchid").	// no such entity
		then().
				//log().all().
				assertThat().statusCode(400).					// check envelope
				contentType(ContentType.JSON).
				and().
				body( "exceptionName", is("com.github.sergemart.picocmdb.exception.NoSuchObjectException") ).
				body( "errorName", is("MANAGEDAREANOTFOUND") ).
				body( "localizedMessage", is("Область управления не найдена.") ). // check if the language is switched
				body( "errorCode", is("1000404") );
	}

	// -------------- CREATE --------------

	@Test
	public void create_Op_Creates_Entity() {
		// GIVEN
			// add task to delete created entity after the test
		String entityName1 = "DUMMY" + super.getSalt();
		super.jdbcCleaner.addTask("DELETE FROM managed_area WHERE (name = ?)", new String[] {entityName1});
			// construct JSON
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("name", entityName1);
		jsonMap.put("description", "Тестовое описание.");

		Integer receivedEntityId1 = // check it later directly via JDBC
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
				body( "name", is(entityName1) ).
				body( "description", is("Тестовое описание.") ).	// check if UTF-8 chain is not broken
		 extract().
				path("id");

		// extra check directly in database
		Integer entityId1 = super.jdbcTemplate.queryForObject("SELECT id FROM managed_area WHERE (name = ?)", new String[] {entityName1}, Integer.class);
		assertThat(entityId1, is(receivedEntityId1));
	}


	@Test
	public void create_Op_Reports_When_Entity_With_Same_Name_Exists() {
		// GIVEN
			// create an entity; add task to delete this entity after the test
		String entityName1 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO managed_area(name, description) VALUES (?, 'Тестовое описание.')", (Object[]) new String[] {entityName1});
		super.jdbcCleaner.addTask("DELETE FROM managed_area WHERE (name = ?)", new String[] {entityName1});
		// construct JSON
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("name", entityName1);
		jsonMap.put("description", "Ещё одно описание.");

		given().
				//log().all().
				body(jsonMap).
				contentType(ContentType.JSON).
				header("Accept-Language", "ru-RU"). 			// switch language; expected message should be in Russian
		when().
				post(super.baseRestUrl + "/managedareas/").
		then().
				//log().all().
				statusCode(400).									// check envelope
				contentType(ContentType.JSON).
				and().
				body( "exceptionName", is("com.github.sergemart.picocmdb.exception.ObjectAlreadyExistsException") ).
				body( "errorName", is("MANAGEDAREAEXISTS") ).
				body( "localizedMessage", is("Область управления уже существует.") ). // check if the language is switched
				body( "errorCode", is("1000405") );
	}


	@Test
	public void create_Op_Reports_When_No_Required_Data() {
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
				post(super.baseRestUrl + "/managedareas/").
		then().
				//log().all().
				statusCode(400).								// check envelope
				contentType(ContentType.JSON).
				and().
				body( "exceptionName", is("com.github.sergemart.picocmdb.exception.WrongDataException") ).
				body( "errorName", is("MANAGEDAREABAD") ).
				body( "localizedMessage", is("Область управления содержит неверные данные.") ). // check if the language is switched
				body( "errorCode", is("1000500") );
	}

	// -------------- UPDATE --------------

	@Test
	public void update_Op_Updates_Entity() {
		// GIVEN
			// create an entity to be updated
		String entityName1 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO managed_area(name, description) VALUES (?, 'Тестовое описание.')", (Object[]) new String[] {entityName1});
			// get auto-generated entity ID
		Integer entityId1 = super.jdbcTemplate.queryForObject("SELECT id FROM managed_area WHERE (name = ?)", new String[] {entityName1}, Integer.class);
			// add task to delete the entity by ID (name will be changed) after the test
		super.jdbcCleaner.addTask("DELETE FROM managed_area WHERE (id = ?)", new Integer[] {entityId1});
			// construct JSON
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("name", entityName1 + "_modified");
		jsonMap.put("description", "Изменённое тестовое описание.");

		given().
				//log().all().
				body(jsonMap).
				contentType(ContentType.JSON).
		when().
				put(super.baseRestUrl + "/managedareas/" + entityId1).
		then().
				//log().all().
				statusCode(200).								// check envelope
				contentType(ContentType.JSON).
				and().
				body( "id", is(entityId1) ).
				body( "name", is(entityName1 + "_modified") ).
				body( "description", is("Изменённое тестовое описание.") );	// check if UTF-8 chain is not broken

		// extra check directly in database
		Map<String, Object> modifiedEntity = super.jdbcTemplate.queryForMap("SELECT name, description FROM managed_area WHERE (id = ?)", (Object[])new Integer[] {entityId1});
		assertThat(modifiedEntity.get("name"), is(entityName1 + "_modified"));
		assertThat(modifiedEntity.get("description"), is("Изменённое тестовое описание."));
	}


	@Test
	public void update_Op_Reports_When_Entity_With_Same_Name_Exists() {
		// GIVEN
			// create an entity to be updated; add task to delete this entity after the test
		String entityName1 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO managed_area(name, description) VALUES (?, 'Тестовое описание.')", (Object[]) new String[] {entityName1});
		super.jdbcCleaner.addTask("DELETE FROM managed_area WHERE (name = ?)", new String[] {entityName1});
			// get auto-generated entity ID
		Integer entityId1 = super.jdbcTemplate.queryForObject("SELECT id FROM managed_area WHERE (name = ?)", new String[] {entityName1}, Integer.class);
			// create an entity what will be a conflicting existing entity; add task to delete this entity after the test
		String entityName2 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO managed_area(name, description) VALUES (?, 'Тестовое описание.')", (Object[]) new String[] {entityName2});
		super.jdbcCleaner.addTask("DELETE FROM managed_area WHERE (name = ?)", new String[] {entityName2});
			// construct JSON
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("name", entityName2); // try to rename the entity to the existing name
		jsonMap.put("description", "Изменённое тестовое описание.");

		given().
				//log().all().
				body(jsonMap).
				contentType(ContentType.JSON).
				header("Accept-Language", "ru-RU"). 			// switch language; expected message should be in Russian
		when().
				put(super.baseRestUrl + "/managedareas/" + entityId1).
		then().
				//log().all().
				statusCode(400).								// check envelope
				contentType(ContentType.JSON).
				and().
				body( "exceptionName", is("com.github.sergemart.picocmdb.exception.ObjectAlreadyExistsException") ).
				body( "errorName", is("MANAGEDAREAEXISTS") ).
				body( "localizedMessage", is("Область управления уже существует.") ). // check if the language is switched
				body( "errorCode", is("1000405") );

		// extra check directly in database that the entity that would be modified remains unmodified
		Map<String, Object> unmodifiedEntity = super.jdbcTemplate.queryForMap("SELECT name, description FROM managed_area WHERE (id = ?)", (Object[])new Integer[] {entityId1});
		assertThat(unmodifiedEntity.get("name"), is(entityName1));
		assertThat(unmodifiedEntity.get("description"), is("Тестовое описание."));
	}


	@Test
	public void update_Op_Reports_When_No_Required_Data() {
		// GIVEN
			// create an entity to be updated; add task to delete this entity after the test
		String entityName1 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO managed_area(name, description) VALUES (?, 'Тестовое описание.')", (Object[]) new String[] {entityName1});
		super.jdbcCleaner.addTask("DELETE FROM managed_area WHERE (name = ?)", new String[] {entityName1});
			// get auto-generated entity ID
		Integer entityId1 = super.jdbcTemplate.queryForObject("SELECT id FROM managed_area WHERE (name = ?)", new String[] {entityName1}, Integer.class);
			// construct JSON
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("badfield1", "badfieldvalue");
		jsonMap.put("badfield2", "Некое значение.");

		given().
				//log().all().
				body(jsonMap).
				contentType(ContentType.JSON).
				header("Accept-Language", "ru-RU"). 			// switch language; expected message should be in Russian
		when().
				put(super.baseRestUrl + "/managedareas/" + entityId1).
		then().
				//log().all().
				statusCode(400).								// check envelope
				contentType(ContentType.JSON).
				and().
				body( "exceptionName", is("com.github.sergemart.picocmdb.exception.WrongDataException") ).
				body( "errorName", is("MANAGEDAREABAD") ).
				body( "localizedMessage", is("Область управления содержит неверные данные.") ). // check if the language is switched
				body( "errorCode", is("1000500") );

		// extra check directly in database that the entity that would be modified remains unmodified
		Map<String, Object> unmodifiedEntity = super.jdbcTemplate.queryForMap("SELECT name, description FROM managed_area WHERE (id = ?)", (Object[])new Integer[] {entityId1});
		assertThat(unmodifiedEntity.get("name"), is(entityName1));
		assertThat(unmodifiedEntity.get("description"), is("Тестовое описание."));
	}

	// -------------- DELETE --------------

	@Test
	public void delete_Op_Deletes_Entity() {
		// GIVEN
			// create an entity to be deleted; add task to delete this entity after the test, just in case if delete fails for any reason
		String entityName1 = "DUMMY" + super.getSalt();
		super.jdbcTemplate.update("INSERT INTO managed_area(name, description) VALUES (?, 'Тестовое описание.')", (Object[]) new String[] {entityName1});
		super.jdbcCleaner.addTask("DELETE FROM managed_area WHERE (name = ?)", new String[] {entityName1});
			// get auto-generated entity ID
		Integer entityId1 = super.jdbcTemplate.queryForObject("SELECT id FROM managed_area WHERE (name = ?)", new String[] {entityName1}, Integer.class);

		given().
				//log().all().
		when().
				delete(super.baseRestUrl + "/managedareas/" + entityId1).
		then().
				//log().all().
				statusCode(200);									// check envelope

		// extra check directly in database that the entity is deleted
		Integer entityCount = super.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM managed_area WHERE (name = ?)", new String[] {entityName1}, Integer.class);
		assertThat(entityCount, is(0));
	}


	@Test
	public void delete_Op_Reports_When_No_Such_Entity() {
		given().
				//log().all().
				header("Accept-Language", "ru-RU").                // switch language; expected message should be in Russian
		when().
				delete(super.baseRestUrl + "/managedareas/nosuchid").    // no such entity
		then().
				//log().all().
				assertThat().statusCode(400).                    // check envelope
				contentType(ContentType.JSON).
				and().
				body("exceptionName", is("com.github.sergemart.picocmdb.exception.NoSuchObjectException")).
				body("errorName", is("MANAGEDAREANOTFOUND")).
				body("localizedMessage", is("Область управления не найдена.")). // check if the language is switched
				body("errorCode", is("1000404"));
	}


}
