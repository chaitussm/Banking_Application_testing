package com.novabank.tests;

import com.novabank.base.BaseTest;
import com.novabank.pages.DashboardPage;
import com.novabank.pages.LoginPage;
import com.novabank.utils.TestData;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for Login page functionality.
 * Covers: valid login for all 3 seeded users, invalid credentials, UI elements.
 */
public class LoginTest extends BaseTest {

    @Test(description = "Customer Ava Smith can log in successfully")
    public void testCustomer1LoginSuccess() {
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigate();

        Assert.assertTrue(loginPage.isLoginPageDisplayed(), "Login page should be displayed");

        loginPage.login(TestData.CUSTOMER1_EMAIL, TestData.CUSTOMER1_PASSWORD);

        page.waitForURL("**/dashboard");
        DashboardPage dashboardPage = new DashboardPage(page);
        Assert.assertTrue(dashboardPage.isDashboardDisplayed(), "Dashboard should be displayed after login");
        Assert.assertTrue(
            dashboardPage.getLoggedInUserText().contains(TestData.CUSTOMER1_FULL_NAME),
            "Logged in user name should be visible"
        );
    }

    @Test(description = "Customer Noah Patel can log in successfully")
    public void testCustomer2LoginSuccess() {
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigate();

        loginPage.login(TestData.CUSTOMER2_EMAIL, TestData.CUSTOMER2_PASSWORD);

        page.waitForURL("**/dashboard");

        DashboardPage dashboardPage = new DashboardPage(page);
        Assert.assertTrue(dashboardPage.isDashboardDisplayed(), "Dashboard should be displayed after login");
        Assert.assertTrue(
            dashboardPage.getLoggedInUserText().contains(TestData.CUSTOMER2_FULL_NAME),
            "Logged in user name should be visible"
        );
    }

    @Test(description = "Manager Mia Johnson can log in successfully")
    public void testManagerLoginSuccess() {
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigate();

        loginPage.login(TestData.MANAGER_EMAIL, TestData.MANAGER_PASSWORD);

        page.waitForURL("**/dashboard");

        DashboardPage dashboardPage = new DashboardPage(page);
        Assert.assertTrue(dashboardPage.isDashboardDisplayed(), "Dashboard should be displayed after manager login");
        Assert.assertTrue(
            dashboardPage.getLoggedInUserText().contains(TestData.MANAGER_FULL_NAME),
            "Manager name should be visible"
        );
        Assert.assertTrue(
            dashboardPage.getLoggedInUserText().contains(TestData.MANAGER_ROLE),
            "Manager role should be visible"
        );
    }

    @Test(description = "Login fails with invalid email")
    public void testLoginWithInvalidEmail() {
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigate();

        loginPage.login(TestData.INVALID_EMAIL, TestData.CUSTOMER1_PASSWORD);

        page.waitForSelector("p.error");
        Assert.assertTrue(loginPage.isErrorDisplayed(), "Error message should be displayed for invalid email");
    }

    @Test(description = "Login fails with invalid password")
    public void testLoginWithInvalidPassword() {
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigate();

        loginPage.login(TestData.CUSTOMER1_EMAIL, TestData.INVALID_PASSWORD);

        page.waitForSelector("p.error");
        Assert.assertTrue(loginPage.isErrorDisplayed(), "Error message should be displayed for wrong password");
    }

    @Test(description = "Login page shows Register and Forgot Password links")
    public void testLoginPageLinks() {
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigate();

        Assert.assertTrue(page.isVisible("a[href='/register']"), "Register link should be present");
        Assert.assertTrue(page.isVisible("a[href='/forgot-password']"), "Forgot password link should be present");
    }

    @Test(description = "Unauthenticated user is redirected to login page")
    public void testUnauthenticatedUserRedirectedToLogin() {
        page.navigate(TestData.DASHBOARD_URL);
        page.waitForURL("**/login");
        Assert.assertTrue(page.url().contains("/login"), "Unauthenticated user should be redirected to /login");
    }
}
