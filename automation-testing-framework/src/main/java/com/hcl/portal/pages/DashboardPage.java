package com.hcl.portal.pages;

import com.hcl.portal.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Dashboard Page Object Model
 * Encapsulates UI elements and interactions on dashboard.html.
 */
public class DashboardPage extends BasePage {

    // Locators
    private final By welcomeHeading = By.id("welcome-heading");
    private final By navUserName = By.id("nav-user-name");
    private final By navUserRole = By.id("nav-user-role");
    private final By employeeIdVal = By.id("employee-id-val");
    private final By employeeDeptVal = By.id("employee-dept-val");
    private final By leaveBalanceVal = By.id("leave-balance-val");
    private final By profileName = By.id("profile-name");
    private final By profileDesignation = By.id("profile-designation");
    private final By profileEmail = By.id("profile-email");
    private final By profileRole = By.id("profile-role");
    private final By profileJoinDate = By.id("profile-joindate");
    private final By logoutBtn = By.id("btn-logout");
    private final By navLeavesLink = By.id("nav-leaves");
    private final By quickApplyLeaveBtn = By.id("btn-quick-apply-leave");
    private final By recentLeavesTable = By.id("recent-leaves-table");

    public DashboardPage(WebDriver driver) {
        super(driver);
        // Ensure page is loaded
        WaitUtils.waitForUrlContains(driver, "dashboard.html", defaultTimeout);
    }

    public boolean isLoaded() {
        return isDisplayed(welcomeHeading) && isDisplayed(employeeIdVal);
    }

    public String getEmployeeId() {
        return getText(employeeIdVal);
    }

    public String getDepartment() {
        return getText(employeeDeptVal);
    }

    public String getLeaveBalance() {
        return getText(leaveBalanceVal);
    }

    public String getNavUserName() {
        return getText(navUserName);
    }

    public String getNavUserRole() {
        return getText(navUserRole);
    }

    public String getProfileName() {
        return getText(profileName);
    }

    public String getProfileDesignation() {
        return getText(profileDesignation);
    }

    public String getProfileEmail() {
        return getText(profileEmail);
    }

    public LeavePage navigateToLeaveManagement() {
        click(navLeavesLink);
        return new LeavePage(driver);
    }

    public LeavePage clickQuickApplyLeave() {
        click(quickApplyLeaveBtn);
        return new LeavePage(driver);
    }

    public LoginPage clickLogout() {
        click(logoutBtn);
        return new LoginPage(driver);
    }

    public boolean isRecentLeavesTableDisplayed() {
        return isDisplayed(recentLeavesTable);
    }
}
