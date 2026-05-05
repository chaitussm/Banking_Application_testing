package com.novabank.tests;

import com.novabank.base.BaseTest;
import com.novabank.pages.DashboardPage;
import com.novabank.pages.LoginPage;
import com.novabank.utils.TestData;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for the Dashboard page (/dashboard).
 * Verifies stats cards, navigation, and role-based UI visibility.
 */
public class DashboardTest extends BaseTest {

    private void loginAs(String email, String password) {
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigate();
        loginPage.login(email, password);
        page.waitForURL("**/dashboard");
    }

    @Test(description = "Dashboard displays 4 stat cards after login")
    public void testDashboardShowsStatCards() {
        loginAs(TestData.CUSTOMER1_EMAIL, TestData.CUSTOMER1_PASSWORD);

        DashboardPage dashboardPage = new DashboardPage(page);
        Assert.assertTrue(dashboardPage.isDashboardDisplayed(), "Dashboard should be displayed");
        Assert.assertEquals(dashboardPage.getStatCardCount(), 4,
            "Dashboard should show 4 stat cards: Users, Accounts, Total Balance, Transactions");
    }

    @Test(description = "Dashboard shows correct heading")
    public void testDashboardHeading() {
        loginAs(TestData.CUSTOMER1_EMAIL, TestData.CUSTOMER1_PASSWORD);

        DashboardPage dashboardPage = new DashboardPage(page);
        Assert.assertEquals(dashboardPage.getHeading(), "Dashboard", "Dashboard heading should be 'Dashboard'");
    }

    @Test(description = "Dashboard displays logged-in customer's name and role")
    public void testDashboardShowsCustomerName() {
        loginAs(TestData.CUSTOMER1_EMAIL, TestData.CUSTOMER1_PASSWORD);

        DashboardPage dashboardPage = new DashboardPage(page);
        String subtitle = dashboardPage.getLoggedInUserText();
        Assert.assertTrue(subtitle.contains(TestData.CUSTOMER1_FULL_NAME),
            "Dashboard should show logged-in user's name");
        Assert.assertTrue(subtitle.contains(TestData.CUSTOMER1_ROLE),
            "Dashboard should show user's role");
    }

    @Test(description = "Dashboard displays manager's name and role")
    public void testDashboardShowsManagerName() {
        loginAs(TestData.MANAGER_EMAIL, TestData.MANAGER_PASSWORD);

        DashboardPage dashboardPage = new DashboardPage(page);
        String subtitle = dashboardPage.getLoggedInUserText();
        Assert.assertTrue(subtitle.contains(TestData.MANAGER_FULL_NAME),
            "Dashboard should show manager's full name");
        Assert.assertTrue(subtitle.contains(TestData.MANAGER_ROLE),
            "Dashboard should show manager role");
    }

    @Test(description = "Manager sees Users nav link; customer does not")
    public void testManagerSeesUsersNavLink() {
        loginAs(TestData.MANAGER_EMAIL, TestData.MANAGER_PASSWORD);
        Assert.assertTrue(page.isVisible("a[href='/users']"), "Manager should see Users nav link");
    }

    @Test(description = "Customer does not see Users nav link")
    public void testCustomerDoesNotSeeUsersNavLink() {
        loginAs(TestData.CUSTOMER1_EMAIL, TestData.CUSTOMER1_PASSWORD);
        Assert.assertFalse(page.isVisible("a[href='/users']"), "Customer should not see Users nav link");
    }

    @Test(description = "Logout button logs user out and redirects to login")
    public void testLogoutRedirectsToLogin() {
        loginAs(TestData.CUSTOMER1_EMAIL, TestData.CUSTOMER1_PASSWORD);

        DashboardPage dashboardPage = new DashboardPage(page);
        dashboardPage.clickLogout();

        page.waitForURL("**/login");
        Assert.assertTrue(page.url().contains("/login"), "After logout user should be redirected to login");
    }
}
