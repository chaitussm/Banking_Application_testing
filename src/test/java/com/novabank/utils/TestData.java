package com.novabank.utils;

/**
 * Centralized test data sourced from chaitussm/Banking_Application repository.
 * Frontend: http://localhost:5173 (proxies /api to backend at http://localhost:4000)
 */
public class TestData {

    // Application URLs
    public static final String BASE_URL = "http://localhost:5173";
    public static final String BACKEND_URL = "http://localhost:4000";

    // Page paths
    public static final String LOGIN_PATH = "/login";
    public static final String REGISTER_PATH = "/register";
    public static final String FORGOT_PASSWORD_PATH = "/forgot-password";
    public static final String DASHBOARD_PATH = "/dashboard";
    public static final String USERS_PATH = "/users";
    public static final String ACCOUNTS_PATH = "/accounts";
    public static final String TRANSACTIONS_PATH = "/transactions";
    public static final String TRANSFERS_PATH = "/transfers";
    public static final String UNAUTHORIZED_PATH = "/unauthorized";

    // Full URLs
    public static final String LOGIN_URL = BASE_URL + LOGIN_PATH;
    public static final String REGISTER_URL = BASE_URL + REGISTER_PATH;
    public static final String FORGOT_PASSWORD_URL = BASE_URL + FORGOT_PASSWORD_PATH;
    public static final String DASHBOARD_URL = BASE_URL + DASHBOARD_PATH;
    public static final String USERS_URL = BASE_URL + USERS_PATH;
    public static final String ACCOUNTS_URL = BASE_URL + ACCOUNTS_PATH;
    public static final String TRANSACTIONS_URL = BASE_URL + TRANSACTIONS_PATH;
    public static final String TRANSFERS_URL = BASE_URL + TRANSFERS_PATH;

    // Seeded Users - from backend/src/db/database.js seedDatabase()
    public static final String CUSTOMER1_EMAIL = "ava.smith@novabank.com";
    public static final String CUSTOMER1_PASSWORD = "ava@123";
    public static final String CUSTOMER1_FULL_NAME = "Ava Smith";
    public static final String CUSTOMER1_ROLE = "customer";
    public static final String CUSTOMER1_ID = "user-1001";

    public static final String CUSTOMER2_EMAIL = "noah.patel@novabank.com";
    public static final String CUSTOMER2_PASSWORD = "noah@123";
    public static final String CUSTOMER2_FULL_NAME = "Noah Patel";
    public static final String CUSTOMER2_ROLE = "customer";
    public static final String CUSTOMER2_ID = "user-1002";

    public static final String MANAGER_EMAIL = "mia.johnson@novabank.com";
    public static final String MANAGER_PASSWORD = "mia@123";
    public static final String MANAGER_FULL_NAME = "Mia Johnson";
    public static final String MANAGER_ROLE = "manager";
    public static final String MANAGER_ID = "user-1003";

    // Seeded Account IDs
    public static final String ACCOUNT1_ID = "acc-1001"; // Ava Smith - checking, $4200
    public static final String ACCOUNT2_ID = "acc-1002"; // Noah Patel - savings, $8950
    public static final String ACCOUNT3_ID = "acc-1003"; // Mia Johnson - checking, $12400

    // Invalid credentials
    public static final String INVALID_EMAIL = "invalid@novabank.com";
    public static final String INVALID_PASSWORD = "wrongpassword";

    // Test transaction data
    public static final String TRANSACTION_AMOUNT = "100";
    public static final String TRANSACTION_NOTE = "Test transaction";
    public static final String TRANSFER_AMOUNT = "50";
    public static final String TRANSFER_NOTE = "Test transfer";

    private TestData() {
        // Utility class - no instantiation
    }
}
