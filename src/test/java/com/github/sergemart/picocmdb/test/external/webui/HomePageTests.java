package com.github.sergemart.picocmdb.test.external.webui;

import com.github.sergemart.picocmdb.test.external.restapi.atomic.RoleRestApiTests;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static com.codeborne.selenide.Selenide.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sergemart.picocmdb.test.external.AbstractTests;
import com.github.sergemart.picocmdb.test.external.pageobject.HomePage;


public class HomePageTests extends AbstractTests {

	private static final Logger LOG = LoggerFactory.getLogger(HomePageTests.class);


	@Test
	public void test_Suite_Prerequisites_Initialized() {
		assertNotNull(super.baseUiUrl);
	}


	@Test
	public void home_Page_Opens() {
		HomePage homePage = open(super.baseUiUrl, HomePage.class);
		assertEquals("picoCMDB", homePage.getTitle().innerText());
	}

}
