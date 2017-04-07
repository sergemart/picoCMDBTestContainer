package com.github.sergemart.picocmdb.test.external.restapi.atomic;

import org.junit.Test;

import io.restassured.http.ContentType;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import com.github.sergemart.picocmdb.test.external.AbstractTests;


public class RoleRestApiTests extends AbstractTests {

	@Test
	public void test_Suite_Prerequisites_Initialized() {
		assertThat(super.baseRestUrl, not(isEmptyOrNullString()));
		assertThat(super.jdbcTemplate, not(is(nullValue())));
		assertThat(super.jdbcCleaner, not(is(nullValue())));
	}


	@Test
	public void read_Op_Reads_Entity_List() {
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
	public void read_Op_Reads_Entity() {
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
				body( "id", is(entityId1) ).								// check if service returns just created entity
				body( "description", is("тестовое описание") ).		// check if UTF-8 chain is not broken
				body( "system", is(true) );
	}


	@Test
	public void read_Op_Reports_When_No_Entity_Found() {
		given().
				//log().all().
				header("Accept-Language", "ru-RU").		// switch language; expected message should be in Russian
		when().
				get(super.baseRestUrl + "/roles/nosuchid").	// no such entity
		then().
				//log().all().
				assertThat().statusCode(400).					// check envelope
				contentType(ContentType.JSON).
				and().
				body( "exceptionName", is("com.github.sergemart.picocmdb.exception.NoSuchObjectException") ).
				body( "errorName", is("ROLENOTFOUND") ).
				body( "localizedMessage", is("Роль не найдена.") ). // check if the language is switched
				body( "errorCode", is("1000404") );
	}

}
