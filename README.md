# Banking_Application_testing

## Quick Start (Local)

1. Install Java 17 and Maven.
2. Install Playwright-required Linux dependencies:

   ```bash
   sudo apt-get update
   sudo apt-get install -y --no-install-recommends \
     libatk1.0-0 libatk-bridge2.0-0 libcups2 libdrm2 libatspi2.0-0 \
     libxcomposite1 libxdamage1 libxfixes3 libxrandr2 libgbm1 \
     libxkbcommon0 libasound2
   ```

3. Run the test suite:

   ```bash
   mvn -B clean test
   ```

	Optional: fail fast when frontend is down instead of skipping UI tests:

	```bash
	UI_TESTS_REQUIRE_APP=true mvn -B clean test
	```

4. Generate and open HTML reports:

   ```bash
   mvn -B surefire-report:report
   ```

   Open one of these files in a browser:
   - `target/surefire-reports/index.html`
   - `target/surefire-reports/emailable-report.html`
   - `target/site/surefire-report.html` (if present)

## CI Pipeline and HTML Reports

This project uses a GitHub Actions workflow at `.github/workflows/testng-pipeline.yml`.

### When the workflow runs

1. On every push to `master`
2. Every day at 11:00 PM IST (scheduled cron)
3. Manually from the GitHub Actions UI (`workflow_dispatch`)

### What the workflow does

1. Sets up Java 17 and Node.js
2. Checks out `chaitussm/Banking_Application` and starts backend (`:4000`) plus frontend (`:5173`)
3. Installs Playwright Linux dependencies and Chromium
4. Runs TestNG tests with Maven (`mvn -B clean test`) against the running app
5. Generates Surefire HTML report (`mvn -B surefire-report:report-only`)
6. Uploads report artifacts even when tests fail

### Download and view HTML reports

1. Open repository -> Actions -> `TestNG Pipeline` -> select a run.
2. In the run summary, open the **Artifacts** section and download:
	- `testng-html-report`
	- `surefire-html-report`
3. Extract the downloaded zip files.
4. Open these files in a browser:
	- `test-output/emailable-report.html` (if present)
	- `target/surefire-reports/emailable-report.html`
	- `target/surefire-reports/index.html`
	- `target/site/surefire-report.html` (if present)

### Passed/Failed test case visibility

The HTML reports show per-test status:

1. Passed test cases
2. Failed test cases with stack traces
3. Skipped test cases
4. Suite-level summary totals

### Troubleshooting

1. Browser launch fails with missing Linux dependencies
	- Symptom: Playwright throws "Host system is missing dependencies to run browsers".
	- Fix locally:

	  ```bash
	  sudo apt-get update
	  sudo apt-get install -y --no-install-recommends \
	    libatk1.0-0 libatk-bridge2.0-0 libcups2 libdrm2 libatspi2.0-0 \
	    libxcomposite1 libxdamage1 libxfixes3 libxrandr2 libgbm1 \
	    libxkbcommon0 libasound2
	  ```

2. Tests are skipped after one setup failure
	- Symptom: One configuration failure causes many skipped tests in TestNG.
	- Fix: Open `target/surefire-reports/TestSuite.txt` and identify the first failure in setup methods (for example browser launch in `BaseTest`).

3. No artifacts visible in an Actions run
	- Symptom: Expected report artifact is missing.
	- Fix:
	  - Confirm the run reached upload steps.
	  - Check whether `test-output` or `target/site` was generated.
	  - Download available artifacts and open `target/surefire-reports/index.html` first.

4. Maven build fails locally
	- Symptom: `mvn -B clean test` exits with code 1.
	- Fix checklist:
	  - Ensure Java 17 is available.
	  - Ensure Linux dependencies above are installed.
	  - If frontend is intentionally offline, run with `UI_TESTS_REQUIRE_APP=false` (default) to skip UI tests.
	  - If CI must fail when frontend is offline, run with `UI_TESTS_REQUIRE_APP=true`.
	  - Re-run with debug logs: `mvn -e -X clean test`.
	  - Review `target/surefire-reports/` for root cause details.