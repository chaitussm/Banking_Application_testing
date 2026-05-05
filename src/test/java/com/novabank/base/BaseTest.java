package com.novabank.base;

import com.aventstack.extentreports.ExtentTest;
import com.microsoft.playwright.*;
import com.novabank.utils.ExtentManager;
import com.novabank.utils.TestData;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * Base test class that manages Playwright lifecycle.
 * Each test method gets a fresh browser context and page.
 */
public class BaseTest {

    protected static Playwright playwright;
    protected static Browser browser;
    private static final String REQUIRE_APP_ENV = "UI_TESTS_REQUIRE_APP";
    private static final boolean REQUIRE_APP = Boolean.parseBoolean(
        System.getenv().getOrDefault(REQUIRE_APP_ENV, "false")
    );
    private static volatile boolean appAvailabilityChecked = false;
    private static volatile boolean appAvailable = false;
    private static volatile String appCheckMessage = "";

    protected BrowserContext context;
    protected Page page;
    protected ExtentTest extentTest;

    @BeforeSuite
    public void launchBrowser() {
        playwright = Playwright.create();
        boolean headless = Boolean.parseBoolean(System.getenv().getOrDefault("HEADLESS", "true"));
        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions()
                .setHeadless(headless)
        );
    }

    @AfterSuite
    public void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
        ExtentManager.getInstance().flush();
    }

    @BeforeMethod
    public void createContextAndPage(Method method) {
        // Create extent test entry first so @AfterMethod can always log to it
        Test testAnnotation = method.getAnnotation(Test.class);
        String description = (testAnnotation != null && !testAnnotation.description().isEmpty())
            ? testAnnotation.description()
            : method.getName();
        extentTest = ExtentManager.getInstance().createTest(
            getClass().getSimpleName() + " :: " + method.getName(), description);

        ensureApplicationIsReachable();

        context = browser.newContext(
            new Browser.NewContextOptions()
                .setViewportSize(1280, 800)
        );
        page = context.newPage();
    }

    private void ensureApplicationIsReachable() {
        if (appAvailabilityChecked) {
            if (!appAvailable) {
                handleUnavailableApp();
            }
            return;
        }

        synchronized (BaseTest.class) {
            if (!appAvailabilityChecked) {
                appAvailable = isReachable(TestData.BASE_URL + TestData.LOGIN_PATH);
                if (appAvailable) {
                    appCheckMessage = "UI health-check passed: reachable at " + TestData.BASE_URL;
                } else {
                    appCheckMessage = "UI health-check failed: frontend is not reachable at " + TestData.BASE_URL;
                }
                Reporter.log(appCheckMessage, true);
                System.out.println(appCheckMessage);
                appAvailabilityChecked = true;
            }
        }

        if (!appAvailable) {
            handleUnavailableApp();
        }
    }

    private void handleUnavailableApp() {
        String modeHint = REQUIRE_APP
            ? " Set " + REQUIRE_APP_ENV + "=false to skip instead."
            : " Set " + REQUIRE_APP_ENV + "=true to fail fast instead.";
        String message = appCheckMessage + modeHint;
        if (REQUIRE_APP) {
            throw new IllegalStateException(message);
        }
        throw new SkipException("Skipping UI tests: " + message);
    }

    private boolean isReachable(String url) {
        for (int attempt = 0; attempt < 5; attempt++) {
            BrowserContext probeContext = null;
            Page probePage = null;
            try {
                probeContext = browser.newContext();
                probePage = probeContext.newPage();
                probePage.navigate(url, new Page.NavigateOptions().setTimeout(Duration.ofSeconds(3).toMillis()));
                return true;
            } catch (Exception ignored) {
                // Retry a few times because frontend startup can race with test startup.
            } finally {
                if (probePage != null) {
                    probePage.close();
                }
                if (probeContext != null) {
                    probeContext.close();
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return false;
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult result) {
        // Capture screenshot before closing page
        if (page != null) {
            try {
                String screenshotDir = "test-output/screenshots";
                new File(screenshotDir).mkdirs();

                String status = switch (result.getStatus()) {
                    case ITestResult.FAILURE -> "FAIL";
                    case ITestResult.SKIP    -> "SKIP";
                    default                  -> "PASS";
                };

                String fileName = result.getTestClass().getRealClass().getSimpleName()
                    + "_" + result.getMethod().getMethodName()
                    + "_" + status
                    + "_" + System.currentTimeMillis() + ".png";

                Path screenshotPath = Paths.get(screenshotDir, fileName);
                page.screenshot(new Page.ScreenshotOptions().setPath(screenshotPath).setFullPage(true));

                if (extentTest != null) {
                    String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(screenshotPath));
                    extentTest.addScreenCaptureFromBase64String(base64, status + " Screenshot");
                }
            } catch (Exception ignored) {
                // Screenshot capture is best-effort; don't fail the test for it
            }
        }

        // Log pass/fail/skip to extent report
        if (extentTest != null) {
            switch (result.getStatus()) {
                case ITestResult.FAILURE -> extentTest.fail(result.getThrowable());
                case ITestResult.SKIP    -> extentTest.skip(
                    result.getThrowable() != null ? result.getThrowable().getMessage() : "Skipped");
                default                  -> extentTest.pass("Test passed");
            }
        }

        if (page != null) {
            page.close();
        }
        if (context != null) {
            context.close();
        }
    }
}
