package com.novabank.tests;

import com.novabank.base.BaseTest;
import com.novabank.pages.LoginPage;
import com.novabank.pages.TransactionsPage;
import com.novabank.utils.TestData;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for the Transactions page (/transactions).
 * Covers: page accessibility, heading, create transaction form.
 */
public class TransactionsTest extends BaseTest {

    private void loginAs(String email, String password) {
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigate();
        loginPage.login(email, password);
        page.waitForURL("**/dashboard");
    }

    @Test(description = "Transactions page is accessible after login")
    public void testTransactionsPageIsAccessible() {
        loginAs(TestData.CUSTOMER1_EMAIL, TestData.CUSTOMER1_PASSWORD);

        page.navigate(TestData.TRANSACTIONS_URL);
        page.waitForSelector("h2");

        TransactionsPage transactionsPage = new TransactionsPage(page);
        Assert.assertTrue(transactionsPage.isTransactionsPageDisplayed(), "Transactions page should be displayed");
    }

    @Test(description = "Transactions page shows correct heading")
    public void testTransactionsPageHeading() {
        loginAs(TestData.CUSTOMER1_EMAIL, TestData.CUSTOMER1_PASSWORD);

        page.navigate(TestData.TRANSACTIONS_URL);
        page.waitForSelector("h2");

        TransactionsPage transactionsPage = new TransactionsPage(page);
        Assert.assertEquals(transactionsPage.getHeading(), "Transactions", "Heading should be 'Transactions'");
    }

    @Test(description = "Transactions page has account select, kind select, and submit button")
    public void testTransactionsFormElementsPresent() {
        loginAs(TestData.CUSTOMER1_EMAIL, TestData.CUSTOMER1_PASSWORD);

        page.navigate(TestData.TRANSACTIONS_URL);
        page.waitForSelector(".simple-form");

        Assert.assertTrue(page.isVisible("select"), "Account select should be present");
        Assert.assertTrue(page.isVisible("button[type='submit']"), "Submit button should be present");
    }

    @Test(description = "Unauthenticated access to transactions redirects to login")
    public void testUnauthenticatedTransactionsAccessRedirects() {
        page.navigate(TestData.TRANSACTIONS_URL);
        page.waitForURL("**/login");
        Assert.assertTrue(page.url().contains("/login"), "Should redirect unauthenticated user to login");
    }

    @Test(description = "Manager can access transactions page")
    public void testManagerCanAccessTransactions() {
        loginAs(TestData.MANAGER_EMAIL, TestData.MANAGER_PASSWORD);

        page.navigate(TestData.TRANSACTIONS_URL);
        page.waitForSelector("h2");

        TransactionsPage transactionsPage = new TransactionsPage(page);
        Assert.assertTrue(transactionsPage.isTransactionsPageDisplayed(), "Manager should see transactions page");
    }
}
