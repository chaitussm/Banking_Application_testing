package com.novabank.tests;

import com.novabank.base.BaseTest;
import com.novabank.pages.LoginPage;
import com.novabank.pages.TransfersPage;
import com.novabank.utils.TestData;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for the Transfers page (/transfers).
 * Covers: page accessibility, heading, form elements.
 */
public class TransfersTest extends BaseTest {

    private void loginAs(String email, String password) {
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigate();
        loginPage.login(email, password);
        page.waitForURL("**/dashboard");
    }

    @Test(description = "Transfers page is accessible after login")
    public void testTransfersPageIsAccessible() {
        loginAs(TestData.MANAGER_EMAIL, TestData.MANAGER_PASSWORD);

        page.navigate(TestData.TRANSFERS_URL);
        page.waitForSelector("h2");

        TransfersPage transfersPage = new TransfersPage(page);
        Assert.assertTrue(transfersPage.isTransfersPageDisplayed(), "Transfers page should be displayed");
    }

    @Test(description = "Transfers page shows correct heading")
    public void testTransfersPageHeading() {
        loginAs(TestData.CUSTOMER1_EMAIL, TestData.CUSTOMER1_PASSWORD);

        page.navigate(TestData.TRANSFERS_URL);
        page.waitForSelector("h2");

        TransfersPage transfersPage = new TransfersPage(page);
        Assert.assertEquals(transfersPage.getHeading(), "Transfers", "Heading should be 'Transfers'");
    }

    @Test(description = "Transfers form has from/to account selects and submit button")
    public void testTransfersFormElementsPresent() {
        loginAs(TestData.MANAGER_EMAIL, TestData.MANAGER_PASSWORD);

        page.navigate(TestData.TRANSFERS_URL);
        page.waitForSelector(".simple-form");

        Assert.assertTrue(page.isVisible("select"), "Account select dropdowns should be present");
        Assert.assertTrue(page.isVisible("button[type='submit']"), "Submit button should be present");
    }

    @Test(description = "Unauthenticated access to transfers redirects to login")
    public void testUnauthenticatedTransfersAccessRedirects() {
        page.navigate(TestData.TRANSFERS_URL);
        page.waitForURL("**/login");
        Assert.assertTrue(page.url().contains("/login"), "Should redirect unauthenticated user to login");
    }
}
