package ru.sergm.picocmdb.test.external;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@SpringBootTest
public class HomePageTestSuite {

	@Test
	public void home_Page_Opens() {
		assertEquals("", "");
	}

}
