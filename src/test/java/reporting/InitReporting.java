package reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.ExtentHtmlReporterConfig;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.adda52.driver.DriverType;
import com.adda52.logging.Logging;
import utils.support.SupportUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author Dauli Sengar
 * @since 24th Jan 2024
 * Initializes and configures ExtentReports for test reporting.
 */
public class InitReporting implements Logging {

    /**
     * Constructor for InitReporting class.
     *
     * @param extentReports The ExtentReports instance to be configured.
     */
    public InitReporting(ExtentReports extentReports) {
        getLogger().info("Initializing extent reports");

        String platform = Constants.getPlatform().toLowerCase();
        String testCycle = Constants.getTestCycle().toLowerCase();
        String currentYear = SupportUtils.getCurrentDate("yyyy");
        String currentMonth = SupportUtils.getCurrentDate("MM");
        String currentDay = SupportUtils.getCurrentDate("dd");
        String currentDateTime = SupportUtils.getCurrentDateTime("yyyyMMddHHmmss",
                LocalDateTime.now(ZoneId.of("Asia/Kolkata"))).toLowerCase();
        String reportPath = String.format("test-output/reports/%s/%s/%s/%s/%s/%s.html",
                platform, testCycle, currentYear, currentMonth, currentDay, currentDateTime);

        ExtentHtmlReporter reporter = new ExtentHtmlReporter(reportPath);
        String reportName = String.format("LYNX Automation Report || Test Cycle: %s || %s || Release: %s || Iteration: %s",
                Constants.getTestCycle(), Constants.getPlatform(), Constants.getRelease(), Constants.getIteration());

        reporter.config(ExtentHtmlReporterConfig.builder().theme(Theme.DARK).documentTitle("LYNX").build());
        reporter.config().setReportName(reportName);
        extentReports.attachReporter(reporter);
        setupSystemInfo(extentReports);
    }

    /**
     * Sets up system information in ExtentReports based on the browser type.
     *
     * @param extentReports The ExtentReports instance to which system information is added.
     */
    private static void setupSystemInfo(ExtentReports extentReports) {
        String browser = Constants.getBrowser();
        if (browser.equalsIgnoreCase(DriverType.CHROME.toString()) || browser.equalsIgnoreCase(DriverType.FIREFOX.toString())) {
            extentReports.setSystemInfo("Browser", browser);
            extentReports.setSystemInfo("Execution Machine OS", Constants.getExecutionOS());
        } else if (browser.equalsIgnoreCase(DriverType.ANDROID.toString())) {
            extentReports.setSystemInfo("Mobile OS", "Android");
            if (Constants.getAppiumExecutionType().equalsIgnoreCase("browserstack")) {
                extentReports.setSystemInfo("Device", Constants.getBrowserStackDeviceName());
            } else if (Constants.getAppiumExecutionType().equalsIgnoreCase("lambdatest")) {
                extentReports.setSystemInfo("Device", Constants.getLambdaTestDeviceName());
            }

        } else if (browser.equalsIgnoreCase(DriverType.IOS.toString())) {
            extentReports.setSystemInfo("Mobile OS", "iOS");
            if (Constants.getAppiumExecutionType().equalsIgnoreCase("browserstack")) {
                extentReports.setSystemInfo("Device", Constants.getBrowserStackDeviceName());
            } else if (Constants.getAppiumExecutionType().equalsIgnoreCase("lambdatest")) {
                extentReports.setSystemInfo("Device", Constants.getLambdaTestDeviceName());
            }
        }
    }
}


