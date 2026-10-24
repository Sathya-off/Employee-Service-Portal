@smoke @login
Feature: Enterprise Employee Portal Authentication
  As an employee of HCLTech
  I want to securely log into the employee portal
  So that I can access my dashboard and submit time-off requests

  Background:
    Given the user navigates to the portal login page

  Scenario: Successful login with valid employee credentials
    When the user enters username "sathya.narayan" and password "Password@123"
    And clicks on the login button
    Then the user should be redirected to the employee dashboard
    And the employee ID should be displayed as "HCL10482"

  Scenario Outline: Rejected authentication with invalid credentials
    When the user enters username "<username>" and password "<password>"
    And clicks on the login button
    Then an error alert should be displayed with message "<error_message>"

    Examples:
      | username        | password         | error_message                  |
      | sathya.narayan  | WrongPassword99! | Invalid username or password   |
      | invalid.user    | AnyPassword123   | Invalid username or password   |
      |                 |                  | Username and password are required |
