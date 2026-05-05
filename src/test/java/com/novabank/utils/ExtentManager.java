package com.novabank.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;

/**
 * Singleton manager for ExtentReports.
 * Produces an HTML report at test-output/ExtentReport.html.
 */
public class ExtentManager {

    private static ExtentReports extentReports;
    private static final String REPORT_PATH = "test-output/ExtentReport.html";

    private ExtentManager() {}

    public static synchronized ExtentReports getInstance() {
        if (extentReports == null) {
            new File("test-output").mkdirs();

            ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_PATH);
            spark.config().setTheme(Theme.DARK);
            spark.config().setDocumentTitle("NovaBank Test Report");
            spark.config().setReportName("NovaBank Banking Application Test Suite");
            spark.config().setTimeStampFormat("MMM dd, yyyy HH:mm:ss");

            extentReports = new ExtentReports();
            extentReports.attachReporter(spark);
            extentReports.setSystemInfo("Application", "NovaBank Banking Application");
            extentReports.setSystemInfo("Base URL", TestData.BASE_URL);
        }
        return extentReports;
    }
}
