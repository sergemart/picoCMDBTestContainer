package ru.sergm.picocmdb.test.external;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@SpringBootTest
public class RoleRestApiTestSuite {

	@Autowired
	//private TestRestTemplate restTemplate;


	@Test
	public void service_Returns_Role_List() {
	}

}
