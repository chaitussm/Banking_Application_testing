package com.novabank.pages;

import com.microsoft.playwright.Page;
import com.novabank.utils.TestData;

/**
 * Page Object for the Forgot Password page (/forgot-password).
 * Matches ForgotPasswordPage.jsx from chaitussm/Banking_Application frontend.
 */
public class ForgotPasswordPage {

    private final Page page;

    private static final String EMAIL_INPUT = "input[type='email']";
    private static final String NEW_PASSWORD_INPUT = "input[type='password']";
    private static final String RESET_BUTTON = "button[type='submit']";
    private static final String SUCCESS_MESSAGE = "p.credit";
    private static final String ERROR_MESSAGE = "p.error";
    private static final String LOGIN_LINK = "a[href='/login']";

    public ForgotPasswordPage(Page page) {
        this.page = page;
    }

    public ForgotPasswordPage navigate() {
        page.navigate(TestData.FORGOT_PASSWORD_URL);
        return this;
    }

    public ForgotPasswordPage enterEmail(String email) {
        page.fill(EMAIL_INPUT, email);
        return this;
    }

    public ForgotPasswordPage enterNewPassword(String newPassword) {
        page.fill(NEW_PASSWORD_INPUT, newPassword);
        return this;
    }

    public void clickReset() {
        page.click(RESET_BUTTON);
    }

    public void resetPassword(String email, String newPassword) {
        enterEmail(email);
        enterNewPassword(newPassword);
        clickReset();
    }

    public String getSuccessMessage() {
        return page.textContent(SUCCESS_MESSAGE);
    }

    public String getErrorMessage() {
        return page.textContent(ERROR_MESSAGE);
    }

    public boolean isSuccessMessageDisplayed() {
        return page.isVisible(SUCCESS_MESSAGE);
    }

    public boolean isErrorDisplayed() {
        return page.isVisible(ERROR_MESSAGE);
    }

    public boolean isForgotPasswordPageDisplayed() {
        return page.isVisible(RESET_BUTTON) && page.textContent("h2").equals("Forgot Password");
    }

    public void clickLoginLink() {
        page.click(LOGIN_LINK);
    }
}
