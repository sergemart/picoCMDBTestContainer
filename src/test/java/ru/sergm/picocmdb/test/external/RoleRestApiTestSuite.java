package ru.sergm.picocmdb.test.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.*;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;


@RunWith(SpringRunner.class)
@SpringBootTest
public class RoleRestApiTestSuite {

	@Test
	public void service_Returns_Role_List() {
		when().
				get("http://tomcat.igelkott:8080/picocmdb/rest/roles").
		then().
				assertThat().statusCode(200).
				and().
				assertThat().body( "get(0).id", equalTo("ADMINISTRATOR") ).
				assertThat().body( "get(0).system", equalTo(true) );
	}


	@Test
	public void service_Returns_Role() {
		when().
				get("http://tomcat.igelkott:8080/picocmdb/rest/roles/administrator").
		then().
				assertThat().statusCode(200).
				and().
				assertThat().body( "id", equalTo("ADMINISTRATOR") ).
				assertThat().body( "system", equalTo(true) );
	}

}
