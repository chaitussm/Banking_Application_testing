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

    private static final String HEADING = "h2";
    private static final String LIST_GRID = ".list-grid";
    private static final String LIST_CARDS = ".list-card";
    private static final String SUBTITLE = ".subtitle";

    public UsersPage(Page page) {
        this.page = page;
    }

    public UsersPage navigate() {
        page.navigate(TestData.USERS_URL);
        return this;
    }

    public boolean isUsersPageDisplayed() {
        return page.isVisible(LIST_GRID) && page.textContent(HEADING).equals("Users");
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
