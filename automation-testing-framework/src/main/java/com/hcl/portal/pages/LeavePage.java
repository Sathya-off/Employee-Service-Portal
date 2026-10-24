package com.hcl.portal.pages;

import com.hcl.portal.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

/**
 * Leave Management Page Object Model
 * Encapsulates leave submission, validation alerts, table rows, and status assertions.
 */
public class LeavePage extends BasePage {

    // Locators
    private final By pageHeading = By.id("page-heading");
    private final By leaveTypeSelect = By.id("leave-type");
    private final By startDateInput = By.id("start-date");
    private final By endDateInput = By.id("end-date");
    private final By reasonInput = By.id("leave-reason");
    private final By submitBtn = By.id("btn-submit-leave");
    private final By resetBtn = By.id("btn-reset-leave");
    private final By refreshBtn = By.id("btn-refresh-history");
    private final By alertContainer = By.id("leave-alert");
    private final By headerBalanceVal = By.id("header-balance-val");
    private final By leaveHistoryTable = By.id("leave-history-table");

    private final By leaveTypeError = By.id("leave-type-error");
    private final By startDateError = By.id("start-date-error");
    private final By endDateError = By.id("end-date-error");
    private final By reasonError = By.id("leave-reason-error");

    public LeavePage(WebDriver driver) {
        super(driver);
        WaitUtils.waitForUrlContains(driver, "leave-management.html", defaultTimeout);
    }

    public boolean isLoaded() {
        return isDisplayed(pageHeading) && isDisplayed(leaveTypeSelect);
    }

    public LeavePage selectLeaveType(String value) {
        Select select = new Select(find(leaveTypeSelect));
        select.selectByValue(value);
        return this;
    }

    public LeavePage enterStartDate(String dateString) {
        setDateInputValue(startDateInput, dateString);
        return this;
    }

    public LeavePage enterEndDate(String dateString) {
        setDateInputValue(endDateInput, dateString);
        return this;
    }

    public LeavePage enterReason(String reason) {
        clearAndType(reasonInput, reason);
        return this;
    }

    public void clickSubmit() {
        click(submitBtn);
    }

    public void clickReset() {
        click(resetBtn);
    }

    public void clickRefresh() {
        click(refreshBtn);
    }

    public LeavePage applyForLeave(String type, String startDate, String endDate, String reason) {
        selectLeaveType(type);
        enterStartDate(startDate);
        enterEndDate(endDate);
        enterReason(reason);
        clickSubmit();
        return this;
    }

    public String getAlertMessage() {
        return getText(alertContainer);
    }

    public boolean isAlertDisplayed() {
        return isDisplayed(alertContainer);
    }

    public String getAvailableBalance() {
        return getText(headerBalanceVal);
    }

    public boolean isLeaveHistoryTableDisplayed() {
        return isDisplayed(leaveHistoryTable);
    }

    public String getStartDateError() {
        return getText(startDateError);
    }

    public String getEndDateError() {
        return getText(endDateError);
    }

    public String getReasonError() {
        return getText(reasonError);
    }

    public boolean isLeaveRecordPresent(int leaveId) {
        By rowLocator = By.cssSelector(String.format("tr[data-testid='leave-record-%d']", leaveId));
        return isDisplayed(rowLocator);
    }

    public String getLeaveStatusBadgeText(int leaveId) {
        By badgeLocator = By.cssSelector(String.format("span[data-testid='status-badge-%d']", leaveId));
        return getText(badgeLocator);
    }
}
