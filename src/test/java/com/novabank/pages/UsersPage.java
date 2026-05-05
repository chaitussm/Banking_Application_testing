package com.novabank.pages;

import com.microsoft.playwright.Page;
import com.novabank.utils.TestData;

/**
 * Page Object for the Users page (/users).
 * Matches UsersPage.jsx from chaitussm/Banking_Application frontend.
 * Accessible only by manager role.
 */
public class UsersPage {

    private final Page page;

    private static final String HEADING = "h2:has-text('Users')";
    private static final String LIST_GRID = ".list-grid";
    private static final String LIST_CARDS = ".list-card";
    private static final String SUBTITLE = "section.panel.content .subtitle";

    public UsersPage(Page page) {
        this.page = page;
    }

    public UsersPage navigate() {
        page.navigate(TestData.USERS_URL);
        return this;
    }

    public boolean isUsersPageDisplayed() {
        try {
            page.waitForSelector(HEADING, new Page.WaitForSelectorOptions().setTimeout(5000));
            return page.url().contains("/users") && page.locator(HEADING).isVisible();
        } catch (Exception ignored) {
            return false;
        }
    }

    public int getUserCardCount() {
        return page.locator(LIST_CARDS).count();
    }

    public String getUserName(int index) {
        return page.locator(LIST_CARDS).nth(index).locator("h3").textContent();
    }

    public String getUserEmail(int index) {
        return page.locator(LIST_CARDS).nth(index).locator("p").textContent();
    }

    public String getUserRole(int index) {
        return page.locator(LIST_CARDS).nth(index).locator("small").textContent();
    }

    public String getSubtitle() {
        return page.textContent(SUBTITLE);
    }

    public String getHeading() {
        return page.textContent(HEADING);
    }

    public String getCurrentUrl() {
        return page.url();
    }
}
