# Test Plan & Test Case Specification
## Enterprise Employee Portal Automation Testing Capstone

**Project Name**: Enterprise Employee Portal Automation Testing Framework  
**Organization / Context**: HCLTech Placement Portfolio Capstone  
**Target Environment**: Localhost (`http://localhost:8080`)  
**Execution Engine**: Java 21, Maven 3.9.9, Selenium WebDriver 4.25, TestNG 7.10, REST Assured 5.5, Cucumber 7.18  
**Total Test Cases**: 34  
**Passed**: 34 (100%) | **Failed**: 0 | **Skipped**: 0  

---

## 1. Test Strategy Overview

| Tier | Tool / Framework | Objective | Test Count |
| :--- | :--- | :--- | :---: |
| **UI Automation** | Selenium WebDriver + Page Object Model + TestNG | Verify end-to-end user workflows, navigation, and client validation | 15 |
| **API Testing** | REST Assured + Hamcrest matchers | Validate HTTP status codes, headers, response schemas, and auth | 12 |
| **Database Validation** | JDBC + PreparedStatements / JSON Store | Assert transactional data persistence across application layers | 3 |
| **Behavior-Driven** | Cucumber + Gherkin (`.feature`) | Validate business scenarios in readable English specifications | 4 |
| **Total** | | | **34** |

---

## 2. Comprehensive Test Case Matrix & Verified Results

### A. Authentication & Access Control (UI & Security)
| Test ID | Test Name | Group | Input Data | Expected Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC-UI-01** | `testValidLogin` | `smoke`, `regression` | `sathya.narayan` / `Password@123` | Redirects to `dashboard.html`, shows ID `HCL10482` | **VERIFIED** |
| **TC-UI-02** | `testInvalidPassword` | `regression` | `sathya.narayan` / `WrongPassword!` | Displays error banner: `Invalid username or password` | **VERIFIED** |
| **TC-UI-03** | `testInvalidUsername` | `regression` | `nonexistent.user` / `AnyPass123` | Displays error banner: `Invalid username or password` | **VERIFIED** |
| **TC-UI-04** | `testEmptyFieldsValidation`| `regression` | Empty username and password | Displays validation alert: `Username and password are required` | **VERIFIED** |
| **TC-UI-05** | `testLogoutWorkflow` | `smoke`, `regression` | Valid session -> Click logout | Invalidates session, redirects to `index.html?message=logged_out` | **VERIFIED** |
| **TC-UI-06** | `testDirectAccessToProtectedDashboard` | `security` | Direct URL `/dashboard.html` without token | Intercepted, redirects to `index.html?error=unauthorized` | **VERIFIED** |

### B. Employee Dashboard (UI)
| Test ID | Test Name | Group | Expected Output / Assertion | Status |
| :--- | :--- | :--- | :--- | :--- |
| **TC-UI-07** | `testDashboardMetricCardsDisplay` | `smoke`, `regression` | ID matches `HCL10482`, Dept matches `Quality Engineering & Testing` | **VERIFIED** |
| **TC-UI-08** | `testEmployeeProfileDetailsDisplay` | `regression` | Name is `Sathya Narayan`, Title is `Associate Test Engineer` | **VERIFIED** |
| **TC-UI-09** | `testNavbarNavigationToLeaveManagement` | `regression` | Navigates to `/leave-management.html` successfully | **VERIFIED** |
| **TC-UI-10** | `testQuickActionApplyLeaveNavigation` | `regression` | `+ Apply for Leave` routes directly to leave form | **VERIFIED** |
| **TC-UI-11** | `testRecentLeavesSummaryWidget` | `regression` | Summary table displays recent employee leave requests | **VERIFIED** |

### C. Leave Management (UI & Business Rules)
| Test ID | Test Name | Group | Business Rule / Input | Expected Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC-UI-12** | `testSubmitValidLeaveRequest` | `smoke`, `regression` | Future dates: `2026-12-15` to `2026-12-16` | Shows confirmation: `submitted successfully` | **VERIFIED** |
| **TC-UI-13** | `testRejectInvalidDateRange` | `regression` | End date (`2026-11-15`) < Start date (`2026-11-20`) | Rejects submission: `End date cannot be earlier than start date` | **VERIFIED** |
| **TC-UI-14** | `testRejectMissingRequiredFields`| `regression` | Submitting empty leave form | Rejects: `Please complete all required fields.` | **VERIFIED** |
| **TC-UI-15** | `testLeaveHistoryTablePopulated` | `regression` | Verifying historical table | Displays record `#1`, leave type `CASUAL`, badge `APPROVED` | **VERIFIED** |

### D. REST Assured API Testing
| Test ID | Method & Endpoint | Payload / Header | Expected Status & Assertions | Status |
| :--- | :--- | :--- | :--- | :--- |
| **TC-API-01** | `POST /api/auth/login` | Valid employee credentials | HTTP 200, non-empty `token`, `user.employeeId == "HCL10482"` | **VERIFIED** |
| **TC-API-02** | `POST /api/auth/login` | Invalid password | HTTP 401, `success == false`, `message == "Invalid username or password"` | **VERIFIED** |
| **TC-API-03** | `POST /api/auth/login` | Blank credentials | HTTP 400, `success == false`, `message == "Username and password are required"` | **VERIFIED** |
| **TC-API-04** | `POST /api/auth/logout` | `Authorization: Bearer <token>` | HTTP 200, `success == true`, `message == "Logged out successfully"` | **VERIFIED** |
| **TC-API-05** | `GET /api/employees/me` | Valid Bearer token | HTTP 200, returns profile matching authenticated employee | **VERIFIED** |
| **TC-API-06** | `GET /api/employees/me` | No token header | HTTP 401 Unauthorized, access rejected | **VERIFIED** |
| **TC-API-07** | `GET /api/employees/{id}`| `HCL10482` | HTTP 200, returns matching employee details | **VERIFIED** |
| **TC-API-08** | `GET /api/employees/{id}`| Non-existent ID `99999` | HTTP 404 Not Found, error payload returned | **VERIFIED** |
| **TC-API-09** | `POST /api/leaves` | Valid leave JSON object | HTTP 201 Created, `leave.status == "PENDING"`, `days == 3` | **VERIFIED** |
| **TC-API-10** | `POST /api/leaves` | End date before start date | HTTP 400 Bad Request, rejection message | **VERIFIED** |
| **TC-API-11** | `POST /api/leaves` | Missing required fields | HTTP 400 Bad Request | **VERIFIED** |
| **TC-API-12** | `PUT /api/leaves/2/status`| Manager token, status `APPROVED`| HTTP 200 OK, `leave.status == "APPROVED"` | **VERIFIED** |

### E. Database Validation (Direct JDBC Queries)
| Test ID | Test Name | Target Table | Validation Query / Assertion | Status |
| :--- | :--- | :--- | :--- | :--- |
| **TC-DB-01** | `testVerifyPersistedEmployeeRecord` | `employees` | Asserts `full_name`, `department`, and `leave_balance` exist in DB | **VERIFIED** |
| **TC-DB-02** | `testVerifyPersistedHistoricalLeave` | `leaves` | Asserts record `#1` has `leave_type == "CASUAL"` and `status == "APPROVED"` | **VERIFIED** |
| **TC-DB-03** | `testCrossLayerPersistenceIntegrity`| `leaves` | Creates record via REST API, then queries DB to verify record in DB | **VERIFIED** |

### F. Cucumber BDD Gherkin Feature Scenarios
| Scenario ID | Feature & Scenario | Execution Hook | Status |
| :--- | :--- | :--- | :--- |
| **TC-BDD-01** | Successful login with valid employee credentials | `LoginSteps.userRedirectedToDashboard` | **VERIFIED** |
| **TC-BDD-02** | Outline: Rejected login with wrong password | `LoginSteps.errorAlertDisplayed` | **VERIFIED** |
| **TC-BDD-03** | Outline: Rejected login with unknown username | `LoginSteps.errorAlertDisplayed` | **VERIFIED** |
| **TC-BDD-04** | Outline: Rejected login with empty fields | `LoginSteps.errorAlertDisplayed` | **VERIFIED** |

---

## 3. Real-World Autonomous Debugging Log

| Issue Detected | Root Cause | Engineering Solution Implemented | Verification |
| :--- | :--- | :--- | :--- |
| **`JAVA_HOME` Batch Failure** | Windows machine env variable contained trailing backslash (`hotspot\`), causing `mvn.cmd` path expansion to evaluate incorrectly. | Stripped trailing slash in `JAVA_HOME`. | `mvn -version` successfully recognized JDK 21. |
| **Surefire XML Parsing Error** | Unescaped ampersand `&` in `testng.xml` test names caused `SAXParseException`. | Replaced `&` with `and` in test suite titles. | TestNG parser parsed suite without warnings. |
| **HTML5 Date Picker Locale Mismatch** | Direct `sendKeys("2026-12-15")` into `<input type="date">` was interpreted as invalid format by Chrome Windows locale, resulting in incorrect year delta. | Implemented `setDateInputValue` via JavascriptExecutor with bubbling change event. | Form correctly computed 2 days and test passed seamlessly. |
