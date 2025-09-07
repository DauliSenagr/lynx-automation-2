package com.adda52.context.driver;

import com.adda52.controllers.ExecutionController;
import com.adda52.driver.DriverManager;
import io.appium.java_client.NoSuchContextException;
import io.appium.java_client.remote.SupportsContextSwitching;

/**
 * @author Dauli Sengar
 * @since 10th Jan 2024
 * The DriverContext class manages switching between different contexts for the WebDriver.
 * It provides methods to switch to a specified context and retrieve the current context.
 * This class assumes the WebDriver used implements the SupportsContextSwitching interface.
 */
public class DriverContext {

    /**
     * Switches the WebDriver context to the specified context.
     *
     * @param context The name of the context to switch to.
     */
    public static void switchToContext(String context) {
        ((SupportsContextSwitching) DriverManager.getDriver()).context(context);
    }

    /**
     * Attempts to switch to the specified context with retries.
     *
     * @param context       The name of the context to switch to.
     * @param retry         The maximum number of retry attempts.
     * @param pauseDuration The duration to pause (in milliseconds) between retry attempts.
     * @throws RuntimeException if unable to switch context after the maximum number of retries.
     */
    public static void switchToContext(String context, int retry, int pauseDuration) {
        int attempt = 0;
        while (attempt <= retry) {
            try {
                ((SupportsContextSwitching) DriverManager.getDriver()).context(context);
                return; // No need to continue the loop if successful
            } catch (NoSuchContextException e) {
                attempt++;
                if (attempt <= retry) {
                    ExecutionController.pauseExecution(pauseDuration); // Pause before retry
                }
            }
        }
        throw new RuntimeException("Unable to switch context");
    }

    /**
     * Retrieves the current context of the WebDriver.
     *
     * @return The current context as a string.
     */
    public static String getCurrentContext() {
        return ((SupportsContextSwitching) DriverManager.getDriver()).getContext();
    }


}
