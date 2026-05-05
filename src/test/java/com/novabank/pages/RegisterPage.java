package com.novabank.pages;

import com.microsoft.playwright.Page;
import com.novabank.utils.TestData;

/**
 * Page Object for the Register page (/register).
 * Matches RegisterPage.jsx from chaitussm/Banking_Application frontend.
 */
public class RegisterPage {

    private final Page page;

    private static final String FULL_NAME_INPUT = "input:not([type])";
    private static final String EMAIL_INPUT = "input[type='email']";
    private static final String PASSWORD_INPUT = "input[type='password']";
    private static final String CREATE_ACCOUNT_BUTTON = "button[type='submit']";
    private static final String ERROR_MESSAGE = "p.error";
    private static final String LOGIN_LINK = "a[href='/login']";

    public RegisterPage(Page page) {
        this.page = page;
    }

    public RegisterPage navigate() {
        page.navigate(TestData.REGISTER_URL);
        return this;
    }

    public RegisterPage enterFullName(String fullName) {
        page.fill(FULL_NAME_INPUT, fullName);
        return this;
    }

    public RegisterPage enterEmail(String email) {
        page.fill(EMAIL_INPUT, email);
        return this;
    }

    public RegisterPage enterPassword(String password) {
        page.fill(PASSWORD_INPUT, password);
        return this;
    }

    public void clickCreateAccount() {
        page.click(CREATE_ACCOUNT_BUTTON);
    }

    public void register(String fullName, String email, String password) {
        enterFullName(fullName);
        enterEmail(email);
        enterPassword(password);
        clickCreateAccount();
    }

    public String getErrorMessage() {
        return page.textContent(ERROR_MESSAGE);
    }

    public boolean isErrorDisplayed() {
        return page.isVisible(ERROR_MESSAGE);
    }

    public boolean isRegisterPageDisplayed() {
        return page.isVisible(CREATE_ACCOUNT_BUTTON) && page.textContent("h2").equals("Register");
    }

    public void clickLoginLink() {
        page.click(LOGIN_LINK);
    }
}
