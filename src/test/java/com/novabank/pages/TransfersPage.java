package com.novabank.pages;

import com.microsoft.playwright.Page;
import com.novabank.utils.TestData;

/**
 * Page Object for the Transfers page (/transfers).
 * Matches TransfersPage.jsx from chaitussm/Banking_Application frontend.
 * Supports account-to-account transfers.
 */
public class TransfersPage {

    private final Page page;

    private static final String HEADING = "h2:has-text('Transfers')";
    private static final String FROM_ACCOUNT_SELECT = "select:first-of-type";
    private static final String TO_ACCOUNT_SELECT = "select:last-of-type";
    private static final String AMOUNT_INPUT = ".simple-form input:first-of-type";
    private static final String NOTE_INPUT = ".simple-form input:last-of-type";
    private static final String SUBMIT_BUTTON = "button[type='submit']";
    private static final String SUCCESS_MESSAGE = "p.credit";
    private static final String ERROR_MESSAGE = "p.error";

    public TransfersPage(Page page) {
        this.page = page;
    }

    public TransfersPage navigate() {
        page.navigate(TestData.TRANSFERS_URL);
        return this;
    }

    public boolean isTransfersPageDisplayed() {
        try {
            page.waitForSelector(HEADING, new Page.WaitForSelectorOptions().setTimeout(5000));
            return page.url().contains("/transfers") && page.locator(HEADING).isVisible();
        } catch (Exception ignored) {
            return false;
        }
    }

    public void selectFromAccount(String accountLabel) {
        page.selectOption(FROM_ACCOUNT_SELECT, new com.microsoft.playwright.options.SelectOption().setLabel(accountLabel));
    }

    public void selectToAccount(String accountLabel) {
        page.selectOption(TO_ACCOUNT_SELECT, new com.microsoft.playwright.options.SelectOption().setLabel(accountLabel));
    }

    public void enterAmount(String amount) {
        page.fill(AMOUNT_INPUT, amount);
    }

    public void enterNote(String note) {
        page.fill(NOTE_INPUT, note);
    }

    public void submitTransfer() {
        page.click(SUBMIT_BUTTON);
    }

    public boolean isSuccessMessageDisplayed() {
        return page.isVisible(SUCCESS_MESSAGE);
    }

    public boolean isErrorDisplayed() {
        return page.isVisible(ERROR_MESSAGE);
    }

    public String getSuccessMessage() {
        return page.textContent(SUCCESS_MESSAGE);
    }

    public String getErrorMessage() {
        return page.textContent(ERROR_MESSAGE);
    }

    public String getHeading() {
        return page.textContent(HEADING);
    }

    public String getCurrentUrl() {
        return page.url();
    }
}
