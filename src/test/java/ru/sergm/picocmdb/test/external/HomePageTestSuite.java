package ru.sergm.picocmdb.test.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import com.codeborne.selenide.SelenideElement;
import org.junit.Test;
import org.junit.runner.RunWith;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;

import ru.sergm.picocmdb.test.external.pageobject.*;

import java.util.Properties;


@RunWith(SpringRunner.class)
@SpringBootTest
public class HomePageTestSuite {

	private String sutAppUrl = System.getProperty("sut.app.url");

	@Autowired
	private Properties testingProperties;


	@Test
	public void home_Page_Opens() {
		HomePage homePage = open(sutAppUrl, HomePage.class);
		//homePage.getTitle().shouldHave(text("picoCMDB"));
		assertEquals("picoCMDB", homePage.getTitle().innerText());
	}

}
