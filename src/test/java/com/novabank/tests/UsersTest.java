package com.novabank.tests;

import com.novabank.base.BaseTest;
import com.novabank.pages.LoginPage;
import com.novabank.pages.UsersPage;
import com.novabank.utils.TestData;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for the Users page (/users).
 * This page is restricted to manager role only.
 * Customers are redirected to /unauthorized.
 */
public class UsersTest extends BaseTest {

    private void loginAs(String email, String password) {
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigate();
        loginPage.login(email, password);
        page.waitForURL("**/dashboard");
    }

    @Test(description = "Manager can access the Users page")
    public void testManagerCanAccessUsersPage() {
        loginAs(TestData.MANAGER_EMAIL, TestData.MANAGER_PASSWORD);

        page.navigate(TestData.USERS_URL);
        page.waitForSelector(".list-grid");

        UsersPage usersPage = new UsersPage(page);
        Assert.assertTrue(usersPage.isUsersPageDisplayed(), "Manager should be able to view the Users page");
    }

    @Test(description = "Users page shows 3 seeded users for manager")
    public void testUsersPageShowsSeededUsers() {
        loginAs(TestData.MANAGER_EMAIL, TestData.MANAGER_PASSWORD);

        page.navigate(TestData.USERS_URL);
        page.waitForSelector(".list-grid");

        UsersPage usersPage = new UsersPage(page);
        Assert.assertTrue(usersPage.getUserCardCount() >= 3, "Should display at least 3 seeded users");
    }

    @Test(description = "Users page displays correct seeded user names")
    public void testUsersPageDisplaysSeededUserNames() {
        loginAs(TestData.MANAGER_EMAIL, TestData.MANAGER_PASSWORD);

        page.navigate(TestData.USERS_URL);
        page.waitForSelector(".list-grid");

        UsersPage usersPage = new UsersPage(page);

        boolean foundAva = false, foundNoah = false, foundMia = false;
        int count = usersPage.getUserCardCount();
        for (int i = 0; i < count; i++) {
            String name = usersPage.getUserName(i);
            if (name.equals(TestData.CUSTOMER1_FULL_NAME)) foundAva = true;
            if (name.equals(TestData.CUSTOMER2_FULL_NAME)) foundNoah = true;
            if (name.equals(TestData.MANAGER_FULL_NAME)) foundMia = true;
        }

        Assert.assertTrue(foundAva, "Ava Smith should be listed");
        Assert.assertTrue(foundNoah, "Noah Patel should be listed");
        Assert.assertTrue(foundMia, "Mia Johnson should be listed");
    }

    @Test(description = "Customer is redirected to /unauthorized when accessing Users page")
    public void testCustomerIsRedirectedFromUsersPage() {
        loginAs(TestData.CUSTOMER1_EMAIL, TestData.CUSTOMER1_PASSWORD);

        page.navigate(TestData.USERS_URL);
        page.waitForURL("**/unauthorized");

        Assert.assertTrue(page.url().contains("/unauthorized"),
            "Customer should be redirected to /unauthorized when accessing Users page");
    }

    @Test(description = "Unauthorized page shows proper message for customer")
    public void testUnauthorizedPageContent() {
        loginAs(TestData.CUSTOMER2_EMAIL, TestData.CUSTOMER2_PASSWORD);

        page.navigate(TestData.USERS_URL);
        page.waitForURL("**/unauthorized");

        Assert.assertTrue(page.isVisible("h2:has-text('Unauthorized')"), "Unauthorized page should have a heading");
        Assert.assertEquals(page.textContent("h2:has-text('Unauthorized')").trim(), "Unauthorized", "Heading should be 'Unauthorized'");
    }

    @Test(description = "Users page subtitle mentions seeded users")
    public void testUsersPageSubtitle() {
        loginAs(TestData.MANAGER_EMAIL, TestData.MANAGER_PASSWORD);

        page.navigate(TestData.USERS_URL);
        page.waitForSelector(".list-grid");

        UsersPage usersPage = new UsersPage(page);
        Assert.assertTrue(usersPage.getSubtitle().contains("seeded"),
            "Users page subtitle should mention seeded users");
    }
}
