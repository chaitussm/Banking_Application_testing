package com.novabank.base;

import com.microsoft.playwright.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

/**
 * Base test class that manages Playwright lifecycle.
 * Each test method gets a fresh browser context and page.
 */
public class BaseTest {

    protected static Playwright playwright;
    protected static Browser browser;

    protected BrowserContext context;
    protected Page page;

    @BeforeSuite
    public void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions()
                .setHeadless(true)
        );
    }

    @AfterSuite
    public void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeMethod
    public void createContextAndPage() {
        context = browser.newContext(
            new Browser.NewContextOptions()
                .setViewportSize(1280, 800)
        );
        page = context.newPage();
    }

    @AfterMethod
    public void closeContextAndPage() {
        if (page != null) {
            page.close();
        }
        if (context != null) {
            context.close();
        }
    }
}
