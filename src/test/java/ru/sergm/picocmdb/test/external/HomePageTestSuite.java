package ru.sergm.picocmdb.test.external;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import com.codeborne.selenide.SelenideElement;
import org.junit.Test;
import org.junit.runner.RunWith;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;

import ru.sergm.picocmdb.test.external.pageobject.*;


@RunWith(SpringRunner.class)
@SpringBootTest
public class HomePageTestSuite {

	@Test
	public void home_Page_Opens() {
		HomePage homePage = open("http://tomcat.igelkott:8080/picocmdb/", HomePage.class);
		//homePage.getTitle().shouldHave(text("picoCMDB"));
		assertEquals("picoCMDB", homePage.getTitle().innerText());
	}

}
