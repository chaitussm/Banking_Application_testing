package com.novabank.tests;

import com.novabank.base.BaseTest;
import com.novabank.pages.RegisterPage;
import com.novabank.utils.TestData;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for the Register page (/register).
 * Covers: successful registration, duplicate email, missing fields navigation.
 */
public class RegisterTest extends BaseTest {

    @Test(description = "Register page is accessible from login page")
    public void testRegisterPageIsAccessible() {
        page.navigate(TestData.REGISTER_URL);
        RegisterPage registerPage = new RegisterPage(page);
        Assert.assertTrue(registerPage.isRegisterPageDisplayed(), "Register page should be displayed");
    }

    @Test(description = "New user can register successfully and is redirected to dashboard")
    public void testNewUserRegistrationSuccess() {
        RegisterPage registerPage = new RegisterPage(page);
        registerPage.navigate();

        String uniqueEmail = "testuser_" + System.currentTimeMillis() + "@novabank.com";
        registerPage.register("Test User", uniqueEmail, "Test@1234");

        page.waitForURL("**/dashboard");
        Assert.assertTrue(page.url().contains("/dashboard"), "After registration user should be on dashboard");
    }

    @Test(description = "Registration fails with already-registered email")
    public void testRegistrationFailsWithDuplicateEmail() {
        RegisterPage registerPage = new RegisterPage(page);
        registerPage.navigate();

        // Attempt to register with a seeded user email
        registerPage.register(TestData.CUSTOMER1_FULL_NAME, TestData.CUSTOMER1_EMAIL, TestData.CUSTOMER1_PASSWORD);

        page.waitForSelector("p.error");
        Assert.assertTrue(registerPage.isErrorDisplayed(), "Error should be shown for duplicate email");
    }

    @Test(description = "Register page has a link back to login")
    public void testRegisterPageHasLoginLink() {
        RegisterPage registerPage = new RegisterPage(page);
        registerPage.navigate();

        Assert.assertTrue(page.isVisible("a[href='/login']"), "Login link should be present on register page");

        registerPage.clickLoginLink();
        page.waitForURL("**/login");
        Assert.assertTrue(page.url().contains("/login"), "Should navigate to login page");
    }
}
