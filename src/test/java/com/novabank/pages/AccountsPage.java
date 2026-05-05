package com.novabank.pages;

import com.microsoft.playwright.Page;
import com.novabank.utils.TestData;

/**
 * Page Object for the Accounts page (/accounts).
 * Matches AccountsPage.jsx from chaitussm/Banking_Application frontend.
 */
public class AccountsPage {

    private final Page page;

    private static final String HEADING = "section.panel h2:has-text('Accounts'), h2:has-text('Accounts')";
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
        try {
            page.waitForSelector(HEADING, new Page.WaitForSelectorOptions().setTimeout(3000));
            return page.url().contains("/accounts") && page.locator(HEADING).first().isVisible();
        } catch (Exception ignored) {
            return false;
        }
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
        String heading = page.textContent(HEADING);
        return heading == null ? "" : heading.trim();
    }

    public String getCurrentUrl() {
        return page.url();
    }

    public void clickAccountsNavLink() {
        page.click(NAV_ACCOUNTS_LINK);
    }
}
