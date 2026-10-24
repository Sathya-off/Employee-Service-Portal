package com.hcl.portal.bdd;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Cucumber TestNG Test Runner
 * Bridges Cucumber BDD feature files with TestNG execution lifecycle and HTML report generation.
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.hcl.portal.bdd.stepdefs"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber-report.html",
                "json:target/cucumber-reports/cucumber.json"
        },
        monochrome = true
)
public class CucumberTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
