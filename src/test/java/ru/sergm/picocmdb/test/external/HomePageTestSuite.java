package ru.sergm.picocmdb.test.external;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;



@RunWith(SpringRunner.class)
@SpringBootTest
public class HomePageTestSuite {

	@Test
	public void home_Page_Opens() {
		open("http://tomcat.igelkott:8080/picocmdb/");
		//assertEquals("", "");
	}

}
