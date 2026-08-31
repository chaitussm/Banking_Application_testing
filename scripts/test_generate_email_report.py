#!/usr/bin/env python3
"""Unit tests for module-level email report generation."""

import tempfile
import unittest
from pathlib import Path

from generate_email_report import (
    TestResult,
    build_module_stats,
    discover_source_tests,
    merge_source_and_results,
    parse_junit_files,
    parse_testng_results,
    render_html,
)


class ParseAndCountTests(unittest.TestCase):
    def test_junit_counts_pass_fail_skip_per_module(self):
        xml = """<?xml version="1.0"?>
        <testsuite name="com.novabank.tests.LoginTest" tests="3" failures="1" skipped="1">
          <testcase classname="com.novabank.tests.LoginTest" name="testCustomer1LoginSuccess" time="0.12"/>
          <testcase classname="com.novabank.tests.LoginTest" name="testLoginWithInvalidEmail" time="0.08">
            <failure message="expected true"/>
          </testcase>
          <testcase classname="com.novabank.tests.LoginTest" name="testLoginPageShowsLinks" time="0">
            <skipped message="disabled"/>
          </testcase>
        </testsuite>
        """
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "TEST-LoginTest.xml"
            path.write_text(xml, encoding="utf-8")
            results = parse_junit_files([str(path)])

        modules = build_module_stats(results)
        login = modules["Login"]
        self.assertEqual(login.total, 3)
        self.assertEqual(login.passed, 1)
        self.assertEqual(login.failed, 1)
        self.assertEqual(login.skipped, 1)
        self.assertEqual(login.status_label(), "FAILED")

    def test_testng_results_group_by_module(self):
        xml = """<?xml version="1.0"?>
        <testng-results>
          <suite name="NovaBank">
            <test name="Accounts Tests">
              <class name="com.novabank.tests.AccountsTest">
                <test-method status="PASS" name="testAccountsPageAccessible" duration-ms="110" is-config="false"/>
                <test-method status="FAIL" name="testAccountsHeading" duration-ms="90" is-config="false">
                  <exception message="heading mismatch"/>
                </test-method>
                <test-method status="SKIP" name="testUnauthenticatedRedirect" duration-ms="0" is-config="false"/>
                <test-method status="PASS" name="beforeMethod" duration-ms="5" is-config="true"/>
              </class>
            </test>
          </suite>
        </testng-results>
        """
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "testng-results.xml"
            path.write_text(xml, encoding="utf-8")
            results = parse_testng_results([str(path)])

        self.assertEqual(len(results), 3)
        modules = build_module_stats(results)
        accounts = modules["Accounts"]
        self.assertEqual(accounts.total, 3)
        self.assertEqual(accounts.passed, 1)
        self.assertEqual(accounts.failed, 1)
        self.assertEqual(accounts.skipped, 1)

    def test_source_scan_counts_all_test_methods(self):
        java = """
        package com.novabank.tests;
        import org.testng.annotations.Test;
        public class TransfersTest {
            @Test(description = "one")
            public void testOne() {}

            @Test
            public void testTwo() {}
        }
        """
        with tempfile.TemporaryDirectory() as tmp:
            java_path = Path(tmp) / "TransfersTest.java"
            java_path.write_text(java, encoding="utf-8")
            discovered = discover_source_tests(Path(tmp))

        self.assertEqual(
            discovered,
            [
                ("Transfers", "com.novabank.tests.TransfersTest", "testOne"),
                ("Transfers", "com.novabank.tests.TransfersTest", "testTwo"),
            ],
        )

    def test_missing_executed_tests_are_counted_as_skipped(self):
        source = [
            ("Login", "com.novabank.tests.LoginTest", "testA"),
            ("Login", "com.novabank.tests.LoginTest", "testB"),
        ]
        executed = [
            TestResult("Login", "com.novabank.tests.LoginTest", "testA", "PASS", "0.1s", "—"),
        ]
        merged = merge_source_and_results(source, executed)
        modules = build_module_stats(merged)
        login = modules["Login"]
        self.assertEqual(login.total, 2)
        self.assertEqual(login.passed, 1)
        self.assertEqual(login.skipped, 1)
        self.assertEqual(login.failed, 0)

    def test_html_includes_module_summary(self):
        results = [
            TestResult("Login", "LoginTest", "a", "PASS", "0.1s", "—"),
            TestResult("Login", "LoginTest", "b", "FAIL", "0.2s", "boom"),
            TestResult("Users", "UsersTest", "c", "SKIP", "0.0s", "Not executed"),
        ]
        modules = build_module_stats(results)
        html_doc = render_html(modules, results, "failure", "master", "abc12345", "https://example.com")
        self.assertIn("Module Summary", html_doc)
        self.assertIn("Login", html_doc)
        self.assertIn("Users", html_doc)
        self.assertIn("Test Cases", html_doc)
        self.assertIn("PASS", html_doc)
        self.assertIn("FAILED", html_doc)
        self.assertIn("SKIPPED", html_doc)


if __name__ == "__main__":
    unittest.main()
