package com.github.sergemart.picocmdb.test.external.web;

import com.github.sergemart.picocmdb.test.external.pageobject.HomePage;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import static com.codeborne.selenide.Selenide.*;

import com.github.sergemart.picocmdb.test.external.AbstractTests;
import com.github.sergemart.picocmdb.test.external.pageobject.*;


public class HomePageTests extends AbstractTests {


	private String baseUrl;


	@Before
	public void setUp() {
		this.baseUrl = super.env.getProperty("testing.sut.ui.url");
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
