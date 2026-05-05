package com.novabank.pages;

import com.microsoft.playwright.Page;
import com.novabank.utils.TestData;

/**
 * Page Object for the Accounts page (/accounts).
 * Matches AccountsPage.jsx from chaitussm/Banking_Application frontend.
 */
public class AccountsPage {

    private final Page page;

    private static final String HEADING = "h2";
    private static final String LIST_GRID = ".list-grid";
    private static final String LIST_CARDS = ".list-card";
    private static final String NAV_ACCOUNTS_LINK = "a[href='/accounts']";

    public AccountsPage(Page page) {
        this.page = page;
    }

    public AccountsPage navigate() {
        page.navigate(TestData.ACCOUNTS_URL);
        return this;
    }

    public boolean isAccountsPageDisplayed() {
        return page.isVisible(LIST_GRID) && page.textContent(HEADING).equals("Accounts");
    }

    public int getAccountCardCount() {
        return page.locator(LIST_CARDS).count();
    }

    public String getAccountHolderName(int index) {
        return page.locator(LIST_CARDS).nth(index).locator("h3").textContent();
    }

    public String getAccountDetails(int index) {
        return page.locator(LIST_CARDS).nth(index).textContent();
    }

    public String getHeading() {
        return page.textContent(HEADING);
    }

    public String getCurrentUrl() {
        return page.url();
    }

    public void clickAccountsNavLink() {
        page.click(NAV_ACCOUNTS_LINK);
    }
}
