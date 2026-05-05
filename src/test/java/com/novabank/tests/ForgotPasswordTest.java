package com.novabank.tests;

import com.novabank.base.BaseTest;
import com.novabank.pages.ForgotPasswordPage;
import com.novabank.pages.LoginPage;
import com.novabank.utils.TestData;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for the Forgot Password page (/forgot-password).
 * Covers: page accessibility, successful reset, invalid email error.
 */
public class ForgotPasswordTest extends BaseTest {

    @Test(description = "Forgot Password page is accessible")
    public void testForgotPasswordPageIsAccessible() {
        ForgotPasswordPage forgotPasswordPage = new ForgotPasswordPage(page);
        forgotPasswordPage.navigate();

        Assert.assertTrue(forgotPasswordPage.isForgotPasswordPageDisplayed(),
            "Forgot Password page should be displayed");
    }

    @Test(description = "Forgot Password page has a link back to login")
    public void testForgotPasswordPageHasLoginLink() {
        ForgotPasswordPage forgotPasswordPage = new ForgotPasswordPage(page);
        forgotPasswordPage.navigate();

        Assert.assertTrue(page.isVisible("a[href='/login']"), "Login link should be present");

        forgotPasswordPage.clickLoginLink();
        page.waitForURL("**/login");
        Assert.assertTrue(page.url().contains("/login"), "Should navigate back to login page");
    }

    @Test(description = "Password reset fails for non-existent email")
    public void testPasswordResetFailsForUnknownEmail() {
        ForgotPasswordPage forgotPasswordPage = new ForgotPasswordPage(page);
        forgotPasswordPage.navigate();

        forgotPasswordPage.resetPassword(TestData.INVALID_EMAIL, "NewPassword@1");

        page.waitForSelector("p.error");
        Assert.assertTrue(forgotPasswordPage.isErrorDisplayed(),
            "Error should be shown for non-existent email");
    }

    @Test(description = "Password reset succeeds for existing user and shows success message")
    public void testPasswordResetSuccessForExistingUser() {
        ForgotPasswordPage forgotPasswordPage = new ForgotPasswordPage(page);
        forgotPasswordPage.navigate();

        // Reset to a new password, then restore original using another reset
        forgotPasswordPage.resetPassword(TestData.CUSTOMER2_EMAIL, "TempPassword@1");

        page.waitForSelector("p.credit");
        Assert.assertTrue(forgotPasswordPage.isSuccessMessageDisplayed(),
            "Success message should appear after valid reset");

        // Restore original password so other tests are not affected
        forgotPasswordPage.resetPassword(TestData.CUSTOMER2_EMAIL, TestData.CUSTOMER2_PASSWORD);
        page.waitForSelector("p.credit");
    }

    @Test(description = "User can login with new password after reset")
    public void testUserCanLoginAfterPasswordReset() {
        String newPassword = "ResetPass@99";

        // Reset password
        ForgotPasswordPage forgotPasswordPage = new ForgotPasswordPage(page);
        forgotPasswordPage.navigate();
        forgotPasswordPage.resetPassword(TestData.CUSTOMER1_EMAIL, newPassword);
        page.waitForSelector("p.credit");

        // Login with new password
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigate();
        loginPage.login(TestData.CUSTOMER1_EMAIL, newPassword);
        page.waitForURL("**/dashboard");
        Assert.assertTrue(page.url().contains("/dashboard"), "User should be able to login after password reset");

        // Logout so we can access the forgot-password page to restore original password
        page.click("button.logout-btn");
        page.waitForURL("**/login");

        // Restore original password
        forgotPasswordPage.navigate();
        forgotPasswordPage.resetPassword(TestData.CUSTOMER1_EMAIL, TestData.CUSTOMER1_PASSWORD);
        page.waitForSelector("p.credit");
    }
}
