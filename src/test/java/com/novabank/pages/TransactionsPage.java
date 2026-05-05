package com.novabank.pages;

import com.microsoft.playwright.Page;
import com.novabank.utils.TestData;

/**
 * Page Object for the Transactions page (/transactions).
 * Matches TransactionsPage.jsx from chaitussm/Banking_Application frontend.
 * Supports creating debit/credit transactions and viewing transaction history.
 */
public class TransactionsPage {

    private final Page page;

    private static final String HEADING = "h2:has-text('Transactions')";
    private static final String ACCOUNT_SELECT = "select:first-of-type";
    private static final String KIND_SELECT = "select:last-of-type";
    private static final String SUBMIT_BUTTON = "button[type='submit']";
    private static final String ERROR_MESSAGE = "p.error";

    public TransactionsPage(Page page) {
        this.page = page;
    }

    public TransactionsPage navigate() {
        page.navigate(TestData.TRANSACTIONS_URL);
        return this;
    }

    public boolean isTransactionsPageDisplayed() {
        try {
            page.waitForSelector(HEADING, new Page.WaitForSelectorOptions().setTimeout(5000));
            return page.url().contains("/transactions") && page.locator(HEADING).isVisible();
        } catch (Exception ignored) {
            return false;
        }
    }

    public void selectAccount(String accountNameOrId) {
        page.selectOption(ACCOUNT_SELECT, new com.microsoft.playwright.options.SelectOption().setLabel(accountNameOrId));
    }

    public void selectTransactionKind(String kind) {
        page.selectOption(KIND_SELECT, kind);
    }

    public void enterAmount(String amount) {
        // Find amount input: the form has account select, kind select, amount input, note input
        page.locator(".simple-form input").first().fill(amount);
    }

    public void enterNote(String note) {
        page.locator(".simple-form input").last().fill(note);
    }

    public void submitTransaction() {
        page.click(SUBMIT_BUTTON);
    }

    public boolean isErrorDisplayed() {
        return page.isVisible(ERROR_MESSAGE);
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
