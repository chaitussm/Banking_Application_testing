package com.novabank.pages;

import com.microsoft.playwright.Page;
import com.novabank.utils.TestData;

/**
 * Page Object for the Login page (/login).
 * Matches LoginPage.jsx from chaitussm/Banking_Application frontend.
 */
public class LoginPage {

    private final Page page;

    // Locators
    private static final String EMAIL_INPUT = "input[type='email']";
    private static final String PASSWORD_INPUT = "input[type='password']";
    private static final String SIGN_IN_BUTTON = "button[type='submit']";
    private static final String ERROR_MESSAGE = "p.error";
    private static final String CREATE_ACCOUNT_LINK = "a[href='/register']";
    private static final String FORGOT_PASSWORD_LINK = "a[href='/forgot-password']";

    public LoginPage(Page page) {
        this.page = page;
    }

    public LoginPage navigate() {
        page.navigate(TestData.LOGIN_URL);
        return this;
    }

    public LoginPage enterEmail(String email) {
        page.fill(EMAIL_INPUT, email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        page.fill(PASSWORD_INPUT, password);
        return this;
    }

    public void clickSignIn() {
        page.click(SIGN_IN_BUTTON);
    }

    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickSignIn();
    }

    public String getErrorMessage() {
        return page.textContent(ERROR_MESSAGE);
    }

    public boolean isErrorDisplayed() {
        return page.isVisible(ERROR_MESSAGE);
    }

    public String getPageTitle() {
        return page.textContent("h2");
    }

    public String getCurrentUrl() {
        return page.url();
    }

    public void clickCreateAccountLink() {
        page.click(CREATE_ACCOUNT_LINK);
    }

    public void clickForgotPasswordLink() {
        page.click(FORGOT_PASSWORD_LINK);
    }

    public boolean isLoginPageDisplayed() {
        return page.isVisible(SIGN_IN_BUTTON) && page.textContent("h2").equals("Login");
    }
}
