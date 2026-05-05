package com.novabank.tests;

import com.novabank.base.BaseTest;
import com.novabank.pages.AccountsPage;
import com.novabank.pages.LoginPage;
import com.novabank.utils.TestData;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for the Accounts page (/accounts).
 * Verifies that seeded accounts are displayed correctly.
 */
public class AccountsTest extends BaseTest {

    private void loginAs(String email, String password) {
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigate();
        loginPage.login(email, password);
        page.waitForURL("**/dashboard");
    }

    @Test(description = "Accounts page is accessible after login")
    public void testAccountsPageIsAccessible() {
        loginAs(TestData.CUSTOMER1_EMAIL, TestData.CUSTOMER1_PASSWORD);

        page.navigate(TestData.ACCOUNTS_URL);
        AccountsPage accountsPage = new AccountsPage(page);

        Assert.assertTrue(accountsPage.isAccountsPageDisplayed(), "Accounts page should be displayed");
    }

    @Test(description = "Accounts page displays the seeded accounts")
    public void testAccountsPageShowsSeededAccounts() {
        loginAs(TestData.MANAGER_EMAIL, TestData.MANAGER_PASSWORD);

        page.navigate(TestData.ACCOUNTS_URL);
        page.waitForSelector(".list-grid");

        AccountsPage accountsPage = new AccountsPage(page);
        int count = accountsPage.getAccountCardCount();

        Assert.assertTrue(count >= 3, "At least 3 seeded accounts should be displayed");
    }

    @Test(description = "Accounts page heading is correct")
    public void testAccountsPageHeading() {
        loginAs(TestData.CUSTOMER1_EMAIL, TestData.CUSTOMER1_PASSWORD);

        page.navigate(TestData.ACCOUNTS_URL);
        AccountsPage accountsPage = new AccountsPage(page);

        Assert.assertEquals(accountsPage.getHeading(), "Accounts", "Accounts heading should be 'Accounts'");
    }

    @Test(description = "Unauthenticated access to accounts page redirects to login")
    public void testUnauthenticatedAccountsAccessRedirects() {
        page.navigate(TestData.ACCOUNTS_URL);
        page.waitForURL("**/login");
        Assert.assertTrue(page.url().contains("/login"), "Should redirect unauthenticated user to login");
    }
}
