package com.novabank.pages;

import com.microsoft.playwright.Page;
import com.novabank.utils.TestData;

/**
 * Page Object for the Dashboard page (/dashboard).
 * Matches DashboardPage.jsx from chaitussm/Banking_Application frontend.
 * Shows stats: Users, Accounts, Total Balance, Transactions.
 */
public class DashboardPage {

    private final Page page;

    private static final String HEADING = "h2:has-text('Dashboard')";
    private static final String STAT_CARDS = ".stat-card";
    private static final String LOGOUT_BUTTON = "button.logout-btn";
    private static final String HERO_SUBTITLE = ".hero-card .subtitle";
    private static final String ERROR_MESSAGE = "p.error";

    public DashboardPage(Page page) {
        this.page = page;
    }

    public DashboardPage navigate() {
        page.navigate(TestData.DASHBOARD_URL);
        return this;
    }

    public boolean isDashboardDisplayed() {
        try {
            page.waitForSelector(STAT_CARDS, new Page.WaitForSelectorOptions().setTimeout(5000));
            return page.locator(STAT_CARDS).count() >= 4;
        } catch (Exception ignored) {
            return false;
        }
    }

    public String getHeading() {
        return page.textContent(HEADING);
    }

    public String getLoggedInUserText() {
        return page.textContent(HERO_SUBTITLE);
    }

    public int getStatCardCount() {
        return page.locator(STAT_CARDS).count();
    }

    public String getStatCardValue(int index) {
        return page.locator(STAT_CARDS).nth(index).locator("strong").textContent();
    }

    public String getStatCardLabel(int index) {
        return page.locator(STAT_CARDS).nth(index).locator("p").textContent();
    }

    public boolean isErrorDisplayed() {
        return page.isVisible(ERROR_MESSAGE);
    }

    public void clickLogout() {
        page.click(LOGOUT_BUTTON);
    }

    public String getCurrentUrl() {
        return page.url();
    }
}
