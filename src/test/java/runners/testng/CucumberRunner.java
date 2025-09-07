package runners.testng;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;


/**
 * @author Dauli Sengar
 * @since 15th May 2022
 * CucumberRunner class for running Cucumber scenarios using TestNG.
 */
@CucumberOptions(features = {"src/test/resources/features"},
        glue = {"steps"},
        plugin = {"com.adda52.listeners.TestExecutionLifecycleListener","com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:",
                "summary", "json:target/cucumber.json",
                "rerun:target/rerun.txt" // This plugin will store failed scenarios
        })
public class CucumberRunner extends AbstractTestNGCucumberTests {

    /**
     * Overrides the scenarios method to provide parallel execution support.
     *
     * @return Object[][] containing scenarios for execution.
     */
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
