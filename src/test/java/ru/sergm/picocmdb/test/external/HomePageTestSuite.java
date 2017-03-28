package ru.sergm.picocmdb.test.external;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import static com.codeborne.selenide.Selenide.*;

import ru.sergm.picocmdb.test.external.pageobject.*;


@RunWith(SpringRunner.class)
@SpringBootTest
public class HomePageTestSuite {

	@Autowired
	private Environment env;

	private String baseUrl;


	@Before
	public void setUp() {
		this.baseUrl = env.getProperty("testing.sut.ui.url");
	}


	@Test
	public void test_Suite_Data_Initialized() {
		assertNotNull(this.baseUrl);
	}


	@Test
	public void home_Page_Opens() {
		HomePage homePage = open(baseUrl, HomePage.class);
		assertEquals("picoCMDB", homePage.getTitle().innerText());
	}

}
