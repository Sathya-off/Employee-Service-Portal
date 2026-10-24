# Enterprise Employee Portal Automation Testing Framework

> **Independent Educational Portfolio Project**  
> Developed for HCLTech placement preparation to demonstrate enterprise-grade test automation skills across UI, API, Database, and BDD layers.  
> *Notice: This is an independent educational portfolio project, not an official HCLTech application or an HCLTech-endorsed solution.*

---

## 1. Project Overview & Business Value

The **Enterprise Employee Portal Automation Testing Framework** is a multi-tier Quality Engineering solution consisting of:
1. **The System Under Test (SUT)**: A responsive, corporate Employee Self-Service Web Portal providing secure authentication, employee profile dashboards, and an enterprise leave application workflow.
2. **The Automation Testing Framework**: A decoupled, maintainable Java 21 test framework built with **Selenium WebDriver (Page Object Model)**, **TestNG**, **REST Assured**, **JDBC Database Validation**, and **Cucumber BDD**.

---

## 2. Technology Stack & Versions

| Layer / Role | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language & Runtime** | Java (Eclipse Temurin JDK) | `21.0.12 (LTS)` | Core automation framework language |
| **Build & Dependency Tool** | Apache Maven | `3.9.9` | Dependency management & lifecycle orchestration |
| **Browser Automation** | Selenium WebDriver | `4.25.0` | Browser driver automation with Selenium Manager |
| **Test Lifecycle & Runner** | TestNG | `7.10.2` | Test execution, parameterization, and suite XMLs |
| **Design Pattern** | Page Object Model (POM) | Clean POM | Separation of UI locators from test logic |
| **API Automation** | REST Assured | `5.5.0` | Fluent REST HTTP validation and JSON assertions |
| **Database Validation** | JDBC & MySQL Connector/J | `8.4.0` | Direct transactional SQL database state validation |
| **Behavior-Driven Testing** | Cucumber Java & TestNG | `7.18.1` | Readable Gherkin feature files for business users |
| **Logging Framework** | Apache Log4j2 & SLF4J | `2.24.1` | Structured file & console audit logging |
| **CI/CD Pipeline** | Jenkins Pipeline | Declarative | Automated CI test execution and artifact archiving |
| **Performance Testing** | Apache JMeter | `5.6.3` | Multi-threaded API load testing demonstration |

---

## 3. Architecture & Project Layout

```text
hcl-employee-portal-capstone/
├── .gitignore                          # Excludes binaries, logs, and sensitive data
├── Jenkinsfile                         # Declarative CI/CD pipeline
├── README.md                           # Master project documentation
├── TEST_PLAN_AND_CASES.md              # 34-case test specification matrix
│
├── employee-portal-app/                # System Under Test (SUT)
│   ├── backend/
│   │   ├── server.js                   # REST API & static server
│   │   └── database/
│   │       ├── schema.sql              # Relational SQL schema
│   │       └── portal_data.json        # Persistent transactional data store
│   └── frontend/                       # Corporate Enterprise Web UI
│       ├── index.html                  # Portal Login & Gateway
│       ├── dashboard.html              # Employee Dashboard
│       ├── leave-management.html       # Leave Application & History Table
│       ├── css/styles.css              # Corporate enterprise styling
│       └── js/                         # auth.js, dashboard.js, leave.js
│
├── performance/
│   └── employee_portal_load_test.jmx   # JMeter concurrency test plan
│
└── automation-testing-framework/       # Java Test Automation Project
    ├── pom.xml                         # Maven build configuration
    ├── testng.xml                      # Master regression test suite
    ├── testng-smoke.xml                # Sanity smoke test suite
    └── src/
        ├── main/java/com/hcl/portal/
        │   ├── base/                   # DriverFactory & BaseTest lifecycle
        │   ├── config/                 # ConfigManager (properties loader)
        │   ├── pages/                  # Page Object Model classes
        │   └── utils/                  # WaitUtils, ScreenshotUtil, DatabaseUtil
        └── test/
            ├── java/com/hcl/portal/
            │   ├── ui/                 # LoginTest, DashboardTest, LeaveManagementTest
            │   ├── api/                # AuthApiTest, EmployeeApiTest, LeaveApiTest
            │   ├── db/                 # DatabaseValidationTest
            │   ├── bdd/                # CucumberTestRunner & step definitions
            │   └── listeners/          # TestListener (failure screenshot hooks)
            └── resources/
                ├── config.properties   # SUT URLs, timeouts, test credentials
                ├── log4j2.xml          # Logging layout and rolling appender
                └── features/           # Cucumber Gherkin feature files
```

---

## 4. Quick Start & Execution Guide

### Prerequisites
* Java Development Kit (JDK 21 LTS)
* Apache Maven 3.9+
* Node.js v18+ (for SUT backend)
* Google Chrome or Microsoft Edge

### Step 1: Start the Target Application (SUT)
```powershell
cd employee-portal-app\backend
node server.js
```
The application will launch on `http://localhost:8080`. Verify health: `http://localhost:8080/api/health`.

### Step 2: Execute Test Suites

#### Run the Full Master Regression Suite (All 34 Tests):
```powershell
cd automation-testing-framework
mvn clean test
```

#### Run Only Smoke Sanity Suite:
```powershell
mvn test -DsuiteXmlFile=testng-smoke.xml
```

#### Run in Headed Mode (Watch Browser Execute):
```powershell
mvn test -Dheadless=false
```

#### Run with Microsoft Edge Browser:
```powershell
mvn test -Dbrowser=edge
```

---

## 5. Test Reports & Artifacts

1. **TestNG HTML Reports**: Generated automatically after every test run at `target/surefire-reports/index.html` and `target/surefire-reports/emailable-report.html`.
2. **Cucumber BDD Report**: Generated at `target/cucumber-reports/cucumber-report.html`.
3. **Failure Screenshots**: If any UI test encounters an unexpected state or regression, a full browser viewport screenshot is saved automatically into `screenshots/{testMethodName}_{timestamp}.png`.
4. **Structured Logs**: Comprehensive trace logs are recorded into `logs/automation.log` using Log4j2.

---

## 6. Synthetic Demonstration Credentials

| Role | Username | Password | Employee ID | Department |
| :--- | :--- | :--- | :--- | :--- |
| **Employee** | `sathya.narayan` | `Password@123` | `HCL10482` | Quality Engineering & Testing |
| **Manager** | `rajesh.kumar` | `Manager@123` | `HCL10051` | Quality Engineering & Testing |

---

## 7. Known Limitations & Future Enhancements

* **Cross-Browser Cloud Grid**: Currently configured for local Chrome and Edge; future enhancements will integrate Selenium Grid / Docker containers for parallel cross-browser execution.
* **Email Notifications**: Future iterations can hook into corporate SMTP servers to dispatch automated email notifications upon leave approvals.
