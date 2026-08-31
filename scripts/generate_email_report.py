#!/usr/bin/env python3
"""Generate an HTML email report with per-module test counts and statuses."""

from __future__ import annotations

import glob
import html
import os
import re
import xml.etree.ElementTree as ET
from collections import OrderedDict
from dataclasses import dataclass, field
from pathlib import Path


@dataclass
class TestResult:
    module: str
    classname: str
    name: str
    status: str  # PASS, FAIL, SKIP
    duration: str
    message: str
    suite: str = ""


@dataclass
class ModuleStats:
    name: str
    total: int = 0
    passed: int = 0
    failed: int = 0
    skipped: int = 0
    tests: list[TestResult] = field(default_factory=list)

    def status_label(self) -> str:
        if self.failed > 0:
            return "FAILED"
        if self.skipped > 0 and self.passed == 0:
            return "SKIPPED"
        if self.skipped > 0:
            return "PASSED (with skips)"
        if self.passed > 0:
            return "PASSED"
        return "NO RESULTS"

    def status_color(self) -> str:
        if self.failed > 0:
            return "#e74c3c"
        if self.skipped > 0 and self.passed == 0:
            return "#f39c12"
        if self.skipped > 0:
            return "#27ae60"
        if self.passed > 0:
            return "#27ae60"
        return "#7f8c8d"


MODULE_NAMES = OrderedDict(
    [
        ("LoginTest", "Login"),
        ("RegisterTest", "Register"),
        ("ForgotPasswordTest", "Forgot Password"),
        ("DashboardTest", "Dashboard"),
        ("UsersTest", "Users"),
        ("AccountsTest", "Accounts"),
        ("TransactionsTest", "Transactions"),
        ("TransfersTest", "Transfers"),
    ]
)

TESTNG_TEST_TO_MODULE = {
    "Login Tests": "Login",
    "Register Tests": "Register",
    "Forgot Password Tests": "Forgot Password",
    "Dashboard Tests": "Dashboard",
    "Users Tests": "Users",
    "Accounts Tests": "Accounts",
    "Transactions Tests": "Transactions",
    "Transfers Tests": "Transfers",
}

TEST_METHOD_RE = re.compile(
    r"@Test\b[^\n]*\n(?:[ \t]*@[^\n]+\n)*[ \t]*(?:public|protected|private)?[ \t]*"
    r"(?:void|[A-Za-z0-9_.<>,\[\] ]+)\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(",
    re.MULTILINE,
)


def format_duration(value) -> str:
    try:
        return f"{float(value):.3f}s"
    except (TypeError, ValueError):
        return f"{value}s" if value else "0.000s"


def extract_message(node) -> str:
    if node is None:
        return "—"
    msg = (node.attrib.get("message") or node.text or "").strip()
    msg = " ".join(msg.split())
    if not msg:
        return "—"
    return msg[:500] + ("…" if len(msg) > 500 else "")


def module_from_classname(classname: str) -> str:
    simple = classname.rsplit(".", 1)[-1]
    if simple in MODULE_NAMES:
        return MODULE_NAMES[simple]
    if simple.endswith("Test") and len(simple) > 4:
        return simple[:-4]
    return simple or "Unknown"


def collect_junit_suites(root, fallback_name: str):
    if root.tag == "testsuite":
        return [(root.attrib.get("name", fallback_name), root.findall("testcase"))]
    if root.tag == "testsuites":
        suites = []
        for suite in root.findall("testsuite"):
            suite_name = suite.attrib.get("name", fallback_name)
            suites.append((suite_name, suite.findall("testcase")))
        return suites
    return [(root.attrib.get("name", fallback_name), root.findall(".//testcase"))]


def parse_junit_files(xml_files: list[str]) -> list[TestResult]:
    results: list[TestResult] = []
    seen: set[tuple[str, str]] = set()

    for xf in xml_files:
        try:
            tree = ET.parse(xf)
            root = tree.getroot()
            fallback_name = os.path.basename(xf)
            for suite_name, testcases in collect_junit_suites(root, fallback_name):
                for tc in testcases:
                    classname = tc.attrib.get("classname", "")
                    name = tc.attrib.get("name", "")
                    key = (classname, name)
                    if not name or key in seen:
                        continue
                    seen.add(key)

                    failure = tc.find("failure")
                    error = tc.find("error")
                    skipped_node = tc.find("skipped")

                    if failure is not None:
                        status, message = "FAIL", extract_message(failure)
                    elif error is not None:
                        status, message = "FAIL", extract_message(error)
                    elif skipped_node is not None:
                        status, message = "SKIP", extract_message(skipped_node)
                    else:
                        status, message = "PASS", "—"

                    results.append(
                        TestResult(
                            module=module_from_classname(classname or suite_name),
                            classname=classname,
                            name=name,
                            status=status,
                            duration=format_duration(tc.attrib.get("time", "0")),
                            message=message,
                            suite=suite_name,
                        )
                    )
        except Exception as exc:  # pragma: no cover - parse errors are reported in HTML
            results.append(
                TestResult(
                    module="Parse Error",
                    classname=os.path.basename(xf),
                    name="(parse failed)",
                    status="FAIL",
                    duration="0.000s",
                    message=str(exc),
                    suite=os.path.basename(xf),
                )
            )
    return results


def parse_testng_results(xml_files: list[str]) -> list[TestResult]:
    results: list[TestResult] = []
    seen: set[tuple[str, str]] = set()

    for xf in xml_files:
        try:
            tree = ET.parse(xf)
            root = tree.getroot()
            for test_el in root.findall(".//test"):
                test_name = test_el.attrib.get("name", "")
                module = TESTNG_TEST_TO_MODULE.get(test_name, test_name or "Unknown")
                for class_el in test_el.findall("class"):
                    classname = class_el.attrib.get("name", "")
                    if classname:
                        module = module_from_classname(classname)
                    for method in class_el.findall("test-method"):
                        if method.attrib.get("is-config", "false").lower() == "true":
                            continue
                        name = method.attrib.get("name", "")
                        key = (classname, name)
                        if not name or key in seen:
                            continue
                        seen.add(key)

                        raw_status = (method.attrib.get("status") or "").upper()
                        if raw_status in {"FAIL", "FAILURE"}:
                            status = "FAIL"
                        elif raw_status in {"SKIP", "SKIPPED"}:
                            status = "SKIP"
                        else:
                            status = "PASS"

                        duration_ms = method.attrib.get("duration-ms", "0")
                        try:
                            duration = format_duration(float(duration_ms) / 1000.0)
                        except (TypeError, ValueError):
                            duration = format_duration(duration_ms)

                        exception = method.find("exception")
                        message = extract_message(exception) if exception is not None else "—"
                        results.append(
                            TestResult(
                                module=module,
                                classname=classname,
                                name=name,
                                status=status,
                                duration=duration,
                                message=message,
                                suite=test_name,
                            )
                        )
        except Exception as exc:
            results.append(
                TestResult(
                    module="Parse Error",
                    classname=os.path.basename(xf),
                    name="(parse failed)",
                    status="FAIL",
                    duration="0.000s",
                    message=str(exc),
                    suite=os.path.basename(xf),
                )
            )
    return results


def discover_source_tests(src_root: Path) -> list[tuple[str, str, str]]:
    """Return (module, classname, method) for every @Test method in Java sources."""
    discovered: list[tuple[str, str, str]] = []
    if not src_root.exists():
        return discovered

    for java_file in sorted(src_root.rglob("*Test.java")):
        text = java_file.read_text(encoding="utf-8")
        pkg_match = re.search(r"^\s*package\s+([\w.]+)\s*;", text, re.MULTILINE)
        class_match = re.search(r"\bclass\s+(\w+)", text)
        if not class_match:
            continue
        simple = class_match.group(1)
        classname = f"{pkg_match.group(1)}.{simple}" if pkg_match else simple
        module = module_from_classname(simple)
        for method in TEST_METHOD_RE.findall(text):
            discovered.append((module, classname, method))
    return discovered


def merge_source_and_results(
    source_tests: list[tuple[str, str, str]],
    executed: list[TestResult],
) -> list[TestResult]:
    executed_by_key = {(r.classname, r.name): r for r in executed}
    merged: list[TestResult] = []
    used: set[tuple[str, str]] = set()

    for module, classname, name in source_tests:
        key = (classname, name)
        if key in executed_by_key:
            merged.append(executed_by_key[key])
            used.add(key)
        else:
            merged.append(
                TestResult(
                    module=module,
                    classname=classname,
                    name=name,
                    status="SKIP",
                    duration="0.000s",
                    message="Not executed",
                    suite=module,
                )
            )

    for key, result in executed_by_key.items():
        if key not in used:
            merged.append(result)
    return merged


def build_module_stats(results: list[TestResult]) -> OrderedDict[str, ModuleStats]:
    stats: OrderedDict[str, ModuleStats] = OrderedDict()
    for name in MODULE_NAMES.values():
        stats[name] = ModuleStats(name=name)

    for result in results:
        module = result.module if result.module in stats else result.module
        if module not in stats:
            stats[module] = ModuleStats(name=module)
        bucket = stats[module]
        bucket.total += 1
        bucket.tests.append(result)
        if result.status == "FAIL":
            bucket.failed += 1
        elif result.status == "SKIP":
            bucket.skipped += 1
        else:
            bucket.passed += 1

    # Drop empty known modules that never appeared
    return OrderedDict((k, v) for k, v in stats.items() if v.total > 0)


def status_badge(status: str) -> str:
    colors = {
        "PASS": "#27ae60",
        "PASSED": "#27ae60",
        "PASSED (with skips)": "#27ae60",
        "FAIL": "#e74c3c",
        "FAILED": "#e74c3c",
        "SKIP": "#f39c12",
        "SKIPPED": "#f39c12",
        "NO RESULTS": "#7f8c8d",
    }
    color = colors.get(status, "#7f8c8d")
    return (
        f'<span style="display:inline-block;padding:4px 10px;border-radius:999px;'
        f'color:#fff;background:{color};font-weight:bold;">{html.escape(status)}</span>'
    )


def render_html(
    modules: OrderedDict[str, ModuleStats],
    results: list[TestResult],
    job_status: str,
    branch: str,
    sha: str,
    run_url: str,
) -> str:
    total = passed = failed = skipped = 0
    for mod in modules.values():
        total += mod.total
        passed += mod.passed
        failed += mod.failed
        skipped += mod.skipped

    banner_color = "#27ae60" if job_status == "success" and failed == 0 else "#e74c3c"
    artifacts_url = run_url + "#artifacts"

    module_rows = []
    for mod in modules.values():
        module_rows.append(
            "<tr>"
            f"<td><strong>{html.escape(mod.name)}</strong></td>"
            f'<td style="text-align:center;">{mod.total}</td>'
            f'<td style="text-align:center;color:#27ae60;font-weight:bold;">{mod.passed}</td>'
            f'<td style="text-align:center;color:#e74c3c;font-weight:bold;">{mod.failed}</td>'
            f'<td style="text-align:center;color:#f39c12;font-weight:bold;">{mod.skipped}</td>'
            f'<td style="text-align:center;">{status_badge(mod.status_label())}</td>'
            "</tr>"
        )
    if not module_rows:
        module_rows.append('<tr><td colspan="6">No test results found.</td></tr>')

    detail_rows = []
    for result in results:
        detail_rows.append(
            "<tr>"
            f"<td>{html.escape(result.module)}</td>"
            f"<td>{html.escape(result.classname)}</td>"
            f"<td>{html.escape(result.name)}</td>"
            f'<td style="text-align:center;">{status_badge(result.status)}</td>'
            f'<td style="text-align:right;">{html.escape(result.duration)}</td>'
            f'<td style="max-width:460px;white-space:pre-wrap;word-break:break-word;">'
            f"{html.escape(result.message)}</td>"
            "</tr>"
        )
    if not detail_rows:
        detail_rows.append('<tr><td colspan="6">No test results found.</td></tr>')

    return f"""<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>TestNG Report</title>
</head>
<body style="font-family:Arial,sans-serif;margin:0;padding:20px;background:#f4f4f4">
  <div style="max-width:1200px;margin:auto;background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.15)">
    <div style="background:{banner_color};color:#fff;padding:20px 30px">
      <h2 style="margin:0">TestNG Pipeline Report</h2>
      <p style="margin:4px 0 0">Status: <strong>{html.escape(job_status.upper())}</strong></p>
    </div>
    <div style="padding:20px 30px">
      <table style="border-collapse:collapse;width:100%;margin-bottom:20px">
        <tr>
          <td style="padding:8px 16px;background:#ecf0f1"><strong>Branch</strong></td>
          <td style="padding:8px 16px">{html.escape(branch)}</td>
          <td style="padding:8px 16px;background:#ecf0f1"><strong>Commit</strong></td>
          <td style="padding:8px 16px">{html.escape(sha[:8])}</td>
        </tr>
        <tr>
          <td style="padding:8px 16px;background:#ecf0f1"><strong>Total Test Cases</strong></td>
          <td style="padding:8px 16px">{total}</td>
          <td style="padding:8px 16px;background:#ecf0f1"><strong>Passed / Failed / Skipped</strong></td>
          <td style="padding:8px 16px">
            <span style="color:#27ae60">{passed}</span> /
            <span style="color:#e74c3c">{failed}</span> /
            <span style="color:#f39c12">{skipped}</span>
          </td>
        </tr>
      </table>

      <h3 style="margin:0 0 10px">Module Summary</h3>
      <p style="margin:0 0 12px;color:#555">Module name — number of test cases — passed / failed / skipped</p>
      <table border="1" cellpadding="8" cellspacing="0"
             style="border-collapse:collapse;width:100%;font-size:13px;border-color:#d0d7de;margin-bottom:28px;">
        <thead style="background:#2c3e50;color:#fff;text-align:left;">
          <tr>
            <th>Module</th>
            <th style="text-align:center;">Test Cases</th>
            <th style="text-align:center;">Passed</th>
            <th style="text-align:center;">Failed</th>
            <th style="text-align:center;">Skipped</th>
            <th style="text-align:center;">Status</th>
          </tr>
        </thead>
        <tbody>
          {''.join(module_rows)}
        </tbody>
      </table>

      <h3 style="margin:0 0 10px">All Test Cases</h3>
      <table border="1" cellpadding="8" cellspacing="0"
             style="border-collapse:collapse;width:100%;font-size:13px;border-color:#d0d7de;">
        <thead style="background:#2c3e50;color:#fff;text-align:left;">
          <tr>
            <th>Module</th>
            <th>Class</th>
            <th>Test Case</th>
            <th style="text-align:center;">Status</th>
            <th style="text-align:right;">Time</th>
            <th>Message</th>
          </tr>
        </thead>
        <tbody>
          {''.join(detail_rows)}
        </tbody>
      </table>
      <p style="margin-top:20px">
        <a href="{html.escape(run_url)}" style="background:#2c3e50;color:#fff;padding:10px 20px;text-decoration:none;border-radius:4px">
          View Full Run
        </a>
        &nbsp;
        <a href="{html.escape(artifacts_url)}" style="background:#2980b9;color:#fff;padding:10px 20px;text-decoration:none;border-radius:4px">
          Download Reports
        </a>
      </p>
    </div>
  </div>
</body>
</html>"""


def write_github_step_summary(summary: dict) -> None:
    step_summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if not step_summary:
        return
    lines = [
        "## Module test summary",
        "",
        "| Module | Test Cases | Passed | Failed | Skipped | Status |",
        "| --- | ---: | ---: | ---: | ---: | --- |",
    ]
    for name, stats in summary["modules"].items():
        lines.append(
            f"| {name} | {stats['total']} | {stats['passed']} | {stats['failed']} | "
            f"{stats['skipped']} | {stats['status']} |"
        )
    lines.extend(
        [
            "",
            f"**Total:** {summary['total']}  |  **Passed:** {summary['passed']}  |  "
            f"**Failed:** {summary['failed']}  |  **Skipped:** {summary['skipped']}",
        ]
    )
    with Path(step_summary).open("a", encoding="utf-8") as handle:
        handle.write("\n".join(lines) + "\n")


def collect_result_files(workspace: Path) -> tuple[list[str], list[str]]:
    junit_globs = [
        "target/surefire-reports/junitreports/TEST-*.xml",
        "target/surefire-reports/TEST-*.xml",
        "test-output/junitreports/TEST-*.xml",
    ]
    junit_files: list[str] = []
    seen = set()
    for pattern in junit_globs:
        for path in sorted(glob.glob(str(workspace / pattern))):
            if path in seen:
                continue
            seen.add(path)
            junit_files.append(path)

    testng_files = []
    for path in [
        workspace / "target/surefire-reports/testng-results.xml",
        workspace / "test-output/testng-results.xml",
    ]:
        if path.exists():
            testng_files.append(str(path))
    return junit_files, testng_files


def generate_report(workspace: Path | None = None, output_path: Path | None = None) -> dict:
    workspace = workspace or Path.cwd()
    output_path = output_path or workspace / "email-report.html"

    junit_files, testng_files = collect_result_files(workspace)
    executed = parse_testng_results(testng_files)
    if not executed:
        executed = parse_junit_files(junit_files)

    source_tests = discover_source_tests(workspace / "src/test/java")
    results = merge_source_and_results(source_tests, executed)
    modules = build_module_stats(results)

    job_status = os.environ.get("JOB_STATUS", "unknown")
    branch = os.environ.get("BRANCH", "")
    sha = os.environ.get("SHA", "")
    run_url = os.environ.get("RUN_URL", "#")

    report_html = render_html(modules, results, job_status, branch, sha, run_url)
    output_path.write_text(report_html, encoding="utf-8")

    summary = {
        "modules": {
            name: {
                "total": mod.total,
                "passed": mod.passed,
                "failed": mod.failed,
                "skipped": mod.skipped,
                "status": mod.status_label(),
            }
            for name, mod in modules.items()
        },
        "total": sum(m.total for m in modules.values()),
        "passed": sum(m.passed for m in modules.values()),
        "failed": sum(m.failed for m in modules.values()),
        "skipped": sum(m.skipped for m in modules.values()),
        "output": str(output_path),
    }
    write_github_step_summary(summary)
    return summary


def main() -> None:
    summary = generate_report()
    print("email-report.html written successfully.")
    print(
        f"Total test cases: {summary['total']} "
        f"(passed={summary['passed']}, failed={summary['failed']}, skipped={summary['skipped']})"
    )
    print("Module name - number of test cases - passed / failed / skipped")
    for name, stats in summary["modules"].items():
        print(
            f"  {name} - {stats['total']} - "
            f"passed {stats['passed']} / failed {stats['failed']} / skipped {stats['skipped']} "
            f"[{stats['status']}]"
        )


if __name__ == "__main__":
    main()
