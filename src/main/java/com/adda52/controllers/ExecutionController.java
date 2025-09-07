package com.adda52.controllers;

import com.adda52.driver.DriverManager;
import org.openqa.selenium.JavascriptExecutor;

/**
 * Controller class to manage execution-related actions.
 */
public class ExecutionController {

    /**
     * @param secs The duration to pause the execution in seconds
     * @author Dauli Sengar
     * @since 20th Dec 2023
     * Pauses the execution for a specified duration in seconds.
     */
    public static void pauseExecution(long secs) {
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the execution status and remarks for LambdaTest.
     *
     * @param status  The status to set for the test execution.
     * @param remarks Additional remarks or information about the test execution status.
     */
    public static void setLambdaTestExecutionStatus(String status, String remarks) {
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript("lambda-hook: {\"action\": \"setTestStatus\",\"arguments\": {\"status\":\"" + status + "\", \"remark\":\"" + remarks + " \"}} ");
    }

    /**
     * Sets the name of the test for LambdaTest.
     *
     * @param testName The name to set for the test running on LambdaTest.
     */
    public static void setLambdaTestName(String testName) {
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript("lambda-name=" + testName);
    }

    /**
     * Sets the execution status and remarks for BrowserStack.
     *
     * @param status  The status to set for the test execution.
     * @param remarks Additional remarks or information about the test execution status.
     */
    public static void setBrowserStackExecutionStatus(String status, String remarks) {
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"" + status + "\", \"reason\": \"" + remarks + "\"}}");
    }

    /**
     * Sets the name of the test for BrowserStack.
     *
     * @param name The name to set for the test running on BrowserStack.
     */
    public static void setBrowserStackTestName(String name) {
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript("browserstack_executor: {\"action\": \"setSessionName\", \"arguments\": {\"name\": \"" + name + "\"}}");

    }
}
