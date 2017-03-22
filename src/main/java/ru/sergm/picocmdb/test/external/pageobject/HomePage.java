package ru.sergm.picocmdb.test.external.pageobject;

import org.openqa.selenium.By;
import com.codeborne.selenide.SelenideElement;
import static com.codeborne.selenide.Selenide.*;


public class HomePage {

	public SelenideElement getTitle() {
		return $(By.xpath("/html/head/title"));
	}

}
