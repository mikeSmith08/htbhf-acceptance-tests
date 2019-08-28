package uk.gov.dhsc.htbhf.page.component;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/**
 * Contains functions for all components on a page.
 */
public abstract class BaseComponent {

    private final WebDriver webDriver;
    private final WebDriverWait webDriverWait;

    public BaseComponent(WebDriver webDriver, WebDriverWait webDriverWait) {
        this.webDriver = webDriver;
        this.webDriverWait = webDriverWait;
    }

    public WebElement findByClassName(String className) {
        return webDriver.findElement(By.className(className));
    }

    public WebElement findByCss(String cssSelector) {
        return webDriver.findElement(By.cssSelector(cssSelector));
    }

    public WebElement findById(String id) {
        return webDriver.findElement(By.id(id));
    }

    public List<WebElement> findAllByCss(String cssSelector) {
        return webDriver.findElements(By.cssSelector(cssSelector));
    }

    public void click(By by) {
        webDriverWait.until(ExpectedConditions.elementToBeClickable(by));
        WebElement element = webDriver.findElement(by);
        element.click();
    }
}
