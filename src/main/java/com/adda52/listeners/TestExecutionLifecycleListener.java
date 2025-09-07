package com.adda52.listeners;

import com.adda52.analytics.manager.AnalyticsDataManager;
import com.adda52.communication.slack.SlackCommunication;
import com.adda52.driver.DriverManager;
import com.adda52.logging.Logging;
import com.adda52.utils.file.FileUtils;
import com.adda52.utils.parsers.FeatureFileParser;
import com.adda52.utils.screenshot.ScreenshotUtil;
import com.adda52.utils.support.SupportUtils;
import com.google.gson.JsonObject;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dauli Sengar
 * @since 11th December 2024
 */

public class TestExecutionLifecycleListener implements ConcurrentEventListener, Logging {
    private int testRunId = 0;
    AnalyticsDataManager analyticsDataManager;
    SupportUtils supportUtils;

    SlackCommunication slackCommunication;
    private final ThreadLocal<Map<Long, List<String>>> threadLocalAllTestSteps = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<Long, String>> threadLocalTestCaseNames = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<Long, String>> threadLocalTestCaseFailedScreenshots = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<Long, Boolean>> threadLocalSkipStepsAfterFailure = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<Long, String>> threadLocalTestCasesUdid = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<Long, String>> threadLocalTestStepsUdid = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<Long, String>> threadLocalFeatureNames = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<Long, String>> threadLocalFeatureIds = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<Long, Integer>> threadLocalStepOrder = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<Long, List<TestStep>>> threadLocalTestSteps = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<Long, Integer>> threadLocalTestCasesId = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<Long, String>> threadLocalTestRunTimeStamp = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<Long, String>> threadLocalTestCaseTimeStamp = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<Long, String>> threadLocalTestStepTimeStamp = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<Map<Long, String>> threadLocalTestCaseExecutionIds = ThreadLocal.withInitial(HashMap::new);


    public TestExecutionLifecycleListener() {
        analyticsDataManager = new AnalyticsDataManager();
        supportUtils = new SupportUtils();
        slackCommunication = new SlackCommunication();
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunStarted.class, this::onTestRunStarted);
        publisher.registerHandlerFor(TestSourceRead.class, this::onTestSourceRead);
        publisher.registerHandlerFor(TestCaseStarted.class, this::onTestCaseStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::onTestStepFinished);
        publisher.registerHandlerFor(TestCaseFinished.class, this::onTestCaseFinished);
        publisher.registerHandlerFor(TestRunFinished.class, this::onTestRunFinished);
        publisher.registerHandlerFor(TestStepStarted.class, this::onTestStepStarted);

    }

    private void onTestRunStarted(TestRunStarted event) {

        if (!Constants.getAnalyticsFlag().equalsIgnoreCase("true")) {
            getLogger().info("Analytics flag is set to false. Skipping analytics operations");
            return;
        }
        String testCycle = Constants.getTestCycle();
        String buildName = Constants.getBuildName();
        String platform = Constants.getPlatform();
        getLogger().info("Test Run Started for Test Cycle: " + testCycle + " on Build: " + buildName + " for Platform: " + platform);
        try {
            testRunId = analyticsDataManager.insertTestRun(testCycle, buildName, Constants.getServerName(), platform, Constants.getReleaseName());
            getLogger().info("Inserted new test run with ID: " + testRunId);
            zthreadLocalTestRunTimeStamp.get().put(Thread.currentThread().getId(), String.valueOf(System.currentTimeMillis()));
        } catch (IOException e) {
            getLogger().error("An error occurred during database operations for test run", e);
        }

        try {
            String message = "<!channel> Automation Test Cycle: `" + testCycle + "` has started on environment: `" + Constants.getServerName() + "`\n" +
                    "\n" +
                    "`Build:` " + buildName + "\n" +
                    "`Platform:` " + platform + "\n" +
                    "`Release:` " + Constants.getReleaseName() + "\n" +
                    "\n" +
                    "View real time report at: https://lynx-qa360.adda52poker.com/execution-report?cycle_id=" + testRunId;


                JsonObject response = slackCommunication.sendMessage(Constants.getSlackUrl(), "C06PY3YC7UY", message, Constants.getSlackToken());
                getLogger().info("Slack response: " + response);


        } catch (Exception e) {
            getLogger().info("An error occurred during slack communication", e);
        }

    }

    private void onTestSourceRead(TestSourceRead event) {
    }

    private void onTestCaseStarted(TestCaseStarted event) {
        if (!Constants.getAnalyticsFlag().equalsIgnoreCase("true")) {
            getLogger().info("Analytics flag is set to false. Skipping analytics operations");
            return;
        }
        long threadId = Thread.currentThread().getId();
        String testCaseName = event.getTestCase().getName().replace("\"", "");
        getLogger().info("Test Case Started: " + testCaseName + " on thread: " + threadId);

        // Store test case name and other details in ThreadLocal
        threadLocalTestCaseNames.get().put(threadId, testCaseName);
        threadLocalTestCaseTimeStamp.get().put(threadId, String.valueOf(System.currentTimeMillis()));
        String featurePath = event.getTestCase().getUri().getPath();
        String featureName = FeatureFileParser.getFeatureName(featurePath).replace("\"", "");
        threadLocalFeatureNames.get().put(threadId, featureName);

        // Generate a unique ID
        List<TestStep> steps = event.getTestCase().getTestSteps();
        StringBuilder stepTextBuilder = new StringBuilder();
        for (TestStep step : steps) {
            if (step instanceof PickleStepTestStep pickleStep) {
                stepTextBuilder.append(pickleStep.getStep().getText()).append("|");
            }
        }
        String uniqueID = supportUtils.generateUniqueID(featureName + "|" + testCaseName + "|" + stepTextBuilder);
        threadLocalTestCasesUdid.get().put(threadId, uniqueID);

        try {
            // Fetch or insert feature ID
            int featureId = analyticsDataManager.getFeatureIdIfExists(featureName);
            if (featureId == 0) {
                featureId = analyticsDataManager.insertFeature(featureName, null, null, "automated");
            }
            threadLocalFeatureIds.get().put(threadId, String.valueOf(featureId));

            // Fetch or insert test case ID using uniqueID
            int testCaseId = analyticsDataManager.fetchTestCaseIdIfExists(uniqueID);
            if (testCaseId == 0) {
                testCaseId = analyticsDataManager.insertTestCase(uniqueID, String.valueOf(featureId), testCaseName, "In Progress");
                getLogger().info("Inserted new test case with ID: " + testCaseId);
            } else {
                getLogger().info("Test Case already exists with ID: " + testCaseId);
            }
            threadLocalTestCasesId.get().put(threadId, testCaseId);
            getLogger().info("Stored test case ID in ThreadLocal: " + testCaseId);

        } catch (Exception e) {
            getLogger().error("An error occurred during database operations for test case: " + testCaseName, e);
        }

        if (threadLocalTestCasesId.get().get(threadId) != 0) {

            try {
                int testCaseExecutionId = analyticsDataManager.insertTestCaseExecution(String.valueOf(threadLocalTestCasesId.get().get(threadId)), String.valueOf(testRunId), "In Progress");
                threadLocalTestCaseExecutionIds.get().put(threadId, String.valueOf(testCaseExecutionId));
            } catch (IOException e) {
                getLogger().error("An error occurred during database operations for test case execution: " + testCaseName, e);
            }

        }
    }

    private void onTestStepStarted(TestStepStarted event) {
        if (!Constants.getAnalyticsFlag().equalsIgnoreCase("true")) {
            getLogger().info("Analytics flag is set to false. Skipping analytics operations");
            return;
        }
        long threadId = Thread.currentThread().getId();
        String stepName = getStepNameFromTestStep(event.getTestStep()).replace("\"", "");
        getLogger().info("Test step started for test case id: " + threadLocalTestCasesId.get().get(threadId));
        if (!stepName.equalsIgnoreCase("BEFORE") && !stepName.equalsIgnoreCase("AFTER")) {
            threadLocalTestStepTimeStamp.get().put(threadId, String.valueOf(System.currentTimeMillis()));
        }
    }

    private void onTestStepFinished(TestStepFinished event) {
        String failureCause = event.getResult().getError() != null ? event.getResult().getError().getMessage() : null;
        if (!Constants.getAnalyticsFlag().equalsIgnoreCase("true")) {
            getLogger().info("Analytics flag is set to false. Skipping analytics operations");
            return;
        }

        if (event.getResult().getStatus().is(Status.FAILED)) {
            byte[] screenshotBytes = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
            byte[] compressedBytes = ScreenshotUtil.compressImage(screenshotBytes, 0.5f);
            String failureScreenshot = Base64.getEncoder().encodeToString(compressedBytes);
            threadLocalTestCaseFailedScreenshots.get().put(Thread.currentThread().getId(), failureScreenshot);
        }

        String stepName = getStepNameFromTestStep(event.getTestStep()).replace("\"", "");
        long threadId = Thread.currentThread().getId();
        getLogger().info("Test step finished for test case id: " + threadLocalTestCasesId.get().get(threadId));
        if (!stepName.equalsIgnoreCase("AFTER_STEP") && !stepName.equalsIgnoreCase("AFTER") && !stepName.equalsIgnoreCase("BEFORE")) {
            int stepOrder = threadLocalStepOrder.get().getOrDefault(threadId, 0) + 1;
            // Retrieve and log the stored testCaseId
            Integer testCaseId = threadLocalTestCasesId.get().get(threadId);
            if (testCaseId != null) {
                String key = testCaseId + "|" + stepOrder + "|" + stepName;
                getLogger().info("Generated key for test step: " + key);
                String udid = supportUtils.generateUniqueID(key);
                try {
                    int stepId = analyticsDataManager.fetchTestStepIdIfExists(udid);
                    if (stepId != 0) {
                        long duration = System.currentTimeMillis() - Long.parseLong(threadLocalTestStepTimeStamp.get().get(threadId));
                        getLogger().info("Test Step already exists with ID: " + udid);
                        analyticsDataManager.updateTestStepStatus(udid, event.getResult().getStatus().toString());
                        analyticsDataManager.updateTestStepDuration(udid, String.valueOf(duration));
                        int stepExecutionId = analyticsDataManager.insertTestStepExecution(String.valueOf(stepId), threadLocalTestCaseExecutionIds.get().get(threadId), event.getResult().getStatus().toString());
                        analyticsDataManager.updateTestStepExecutionDuration(String.valueOf(stepExecutionId), String.valueOf(duration));
                    } else {
                        getLogger().info("Test step does not exist. Inserted new test step with ID: " + udid);
                        int newStepId = analyticsDataManager.insertTestStep(udid, String.valueOf(stepOrder), String.valueOf(testCaseId), stepName, event.getResult().getStatus().toString());
                        int stepExecutionId = analyticsDataManager.insertTestStepExecution(String.valueOf(newStepId), threadLocalTestCaseExecutionIds.get().get(threadId), event.getResult().getStatus().toString());
                        long duration = System.currentTimeMillis() - Long.parseLong(threadLocalTestStepTimeStamp.get().get(threadId));
                        analyticsDataManager.updateTestStepDuration(String.valueOf(stepExecutionId), String.valueOf(duration));
                        analyticsDataManager.updateTestStepExecutionDuration(String.valueOf(stepExecutionId), String.valueOf(duration));
                    }
                } catch (IOException | URISyntaxException e) {
                    getLogger().error("An error occurred while updating test step status", e);
                }
            } else {
                getLogger().warn("Test Step Finished. No Test Case ID found for thread: " + threadId);
            }
            threadLocalStepOrder.get().put(threadId, stepOrder);
        }

    }


    private void onTestCaseFinished(TestCaseFinished event) {
        String failureCause = event.getResult().getError() != null ? event.getResult().getError().getMessage() : null;
        if (!Constants.getAnalyticsFlag().equalsIgnoreCase("true")) {
            getLogger().info("Analytics flag is set to false. Skipping analytics operations");
            return;
        }

        long threadId = Thread.currentThread().getId();
        try {

            String testCaseUdid = threadLocalTestCasesUdid.get().get(threadId);
            String testCaseName = threadLocalTestCaseNames.get().get(threadId);
            List<String> testSteps = threadLocalAllTestSteps.get().get(threadId);
            Status status = event.getResult().getStatus();
            String screenshot = threadLocalTestCaseFailedScreenshots.get().get(threadId);
            getLogger().info("Test Case Finished: " + testCaseName +
                    " - Status: " + status);

            // Map statuses to their corresponding strings
            Map<Status, String> statusMap = Map.of(
                    Status.FAILED, "Failed",
                    Status.PASSED, "Passed",
                    Status.SKIPPED, "Skipped",
                    Status.PENDING, "Pending"
            );


            // Update test case status asynchronously
            String statusString = statusMap.getOrDefault(status, "Unknown");

            try {
                getLogger().info("Updating Test Case: " + testCaseUdid + " - Status: " + statusString);
                long duration = System.currentTimeMillis() - Long.parseLong(threadLocalTestCaseTimeStamp.get().get(threadId));
                analyticsDataManager.updateTestCaseStatus(testCaseUdid, statusString);
                analyticsDataManager.updateTestCaseExecutionStatus(threadLocalTestCaseExecutionIds.get().get(threadId), statusString, "NA", "NA", String.valueOf(threadLocalTestCasesId.get().get(threadId)));
                analyticsDataManager.updateTestCaseDuration(testCaseUdid, String.valueOf(duration));
                analyticsDataManager.updateTestCaseExecutionDuration(threadLocalTestCaseExecutionIds.get().get(threadId), String.valueOf(duration), String.valueOf(threadLocalTestCasesId.get().get(threadId)));
                if (event.getResult().getStatus().is(Status.FAILED)) {
                    try {
                        analyticsDataManager.insertTestExecutionFailureScreenshot(threadLocalTestCaseExecutionIds.get().get(threadId), threadLocalTestCaseFailedScreenshots.get().get(threadId));
                        analyticsDataManager.updateTestCaseFailureCause(threadLocalTestCaseExecutionIds.get().get(threadId), failureCause);
                    } catch (Exception e) {
                        getLogger().error("Unable to insert test execution failure screenshot", e);
                    }
                }
            } catch (IOException e) {
                getLogger().error("An error occurred while updating test case status", e);
            }
        } catch (Exception e) {
            getLogger().error("An error occurred while updating test case status", e);
        } finally {
            threadLocalTestCaseNames.get().remove(threadId);
            threadLocalTestCasesUdid.get().remove(threadId);
            threadLocalAllTestSteps.get().remove(threadId);
            threadLocalTestCaseFailedScreenshots.get().remove(threadId);
            threadLocalTestCasesUdid.get().remove(threadId);
            threadLocalSkipStepsAfterFailure.get().remove(threadId); // Clean up the flag
            threadLocalTestStepsUdid.get().remove(threadId);
            threadLocalStepOrder.get().remove(threadId);
            threadLocalFeatureIds.get().remove(threadId);
            threadLocalFeatureNames.get().remove(threadId);
            threadLocalTestSteps.get().remove(threadId);
            threadLocalTestCasesId.get().remove(threadId);
            threadLocalTestCaseTimeStamp.get().remove(threadId);
            threadLocalTestStepTimeStamp.get().remove(threadId);
            threadLocalTestCaseExecutionIds.get().remove(threadId);
        }


    }

    private void onTestRunFinished(TestRunFinished event) {
        if (!Constants.getAnalyticsFlag().equalsIgnoreCase("true")) {
            getLogger().info("Analytics flag is set to false. Skipping analytics operations");
            return;
        }
        getLogger().info("Test Cycle Finished");
        try {
            analyticsDataManager.updateTestRunStatus(String.valueOf(testRunId), "COMPLETED");
            analyticsDataManager.updateTestRunDuration(String.valueOf(testRunId), String.valueOf(System.currentTimeMillis() - Long.parseLong(threadLocalTestRunTimeStamp.get().get(Thread.currentThread().getId()))));
        } catch (IOException e) {
            getLogger().error("Unable to update test run status", e);
        }

        if (new FileUtils().doesFileExist(new File("test-output/Html"), "ExtentHtml.html")) {
            String fileContent = new FileUtils().readFileContent(new File("test-output/Html/ExtentHtml.html").getAbsolutePath());
            try {
                analyticsDataManager.insertTestExecutionHtmlReport(String.valueOf(testRunId), fileContent);
                getLogger().info("Inserted test execution HTML report");
            } catch (Exception e) {
                getLogger().error("Unable to insert test execution HTML report", e);
            }
        }

        try {
            HashMap<String, String> reportData;
            reportData = analyticsDataManager.getExecutionCycleReport(Integer.toString(testRunId));
            int totalFailed = Integer.parseInt(reportData.get("totalFailed"));
            int totalTestCases = Integer.parseInt(reportData.get("totalTestCases"));
            String environmentHealth = "Good";
            double failurePercentage = (double) totalFailed / totalTestCases * 100;
            if (failurePercentage <= 5) {
                environmentHealth = "Good";
            } else if (failurePercentage > 5 && failurePercentage <= 20) {
                environmentHealth = "Average";
            } else {
                environmentHealth = "Bad";
            }

            analyticsDataManager.updateTestRunHealth(String.valueOf(testRunId), environmentHealth);

            String message = "<!channel> Automation Test Cycle: `" + Constants.getTestCycle() + "` has ended on Environment: `" + Constants.getServerName() + "`\n" +
                    "\n" +
                    "`Build:` " + Constants.getBuildName() + "\n" +
                    "`Platform:` " + Constants.getPlatform() + "\n" +
                    "`Release:` " + Constants.getReleaseName() + "\n" +
                    "`Total Test Cases:` " + reportData.get("totalTestCases") + "\n" +
                    "`Passed:` " + reportData.get("totalPassed") + "\n" +
                    "`Failed:` " + reportData.get("totalFailed") + "\n" +
                    "`In Progress:` " + reportData.get("totalInProgress") + "\n" +
                    "`Environment Health:` " + environmentHealth + "\n" +
                    "\n" +
                    "View report at: https://lynx-qa360.adda52poker.com/execution-report?cycle_id=" + testRunId;
            getLogger().info("Sending slack message: " + message);
            String[] channels = Constants.getSlackChannels().split(",");
            for (String channel : channels) {
              JsonObject response =  slackCommunication.sendMessage(Constants.getSlackUrl(), channel, message, Constants.getSlackToken());
              getLogger().info("Slack response: " + response);
            }
        } catch (Exception e) {
            getLogger().info("An error occurred during slack communication", e);
        }
    }

    private String getStepNameFromTestStep(TestStep testStep) {
        if (testStep instanceof PickleStepTestStep pickleStep) {
            return pickleStep.getStep().getText();
        } else if (testStep instanceof HookTestStep hookStep) {
            return hookStep.getHookType().name();
        }
        return "Unknown Step";
    }

}

