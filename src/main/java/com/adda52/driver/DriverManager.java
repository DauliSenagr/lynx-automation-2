package com.adda52.driver;

import com.adda52.logging.Logging;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;

import java.util.Date;

/**
 * @author Dauli Sengar
 * @since 15th May 2022
 * DriverManager class provides static methods to manage WebDriver instances using ThreadLocal.
 */
public class DriverManager implements Logging {

    private static final ThreadLocal<WebDriver> drivers = new ThreadLocal<>();

    /**
     * Retrieves the WebDriver instance associated with the current thread.
     *
     * @return WebDriver instance
     */
    public static WebDriver getDriver() {
        return drivers.get();
    }

    /**
     * Sets the WebDriver instance for the current thread.
     *
     * @param driver WebDriver instance
     */
    public static void setDriver(WebDriver driver) {
        drivers.set(driver);
    }

    /**
     * Quits the current WebDriver instance and removes it from the ThreadLocal.
     */
    public static void quitDriver() {
        getDriver().quit();
        drivers.remove();
    }


    /**
     * Retrieves browser console logs of a specific type and prints them.
     * Deprecated: Use browser logs provided by the WebDriver instance directly.
     *
     * @param logType The type of logs to retrieve (e.g., "browser")
     */
    @Deprecated
    public void printBrowserConsoleLogs(String logType) {
        LogEntries logEntries = getDriver().manage().logs().get(logType);
        for (LogEntry entry : logEntries) {
            getLogger().info(new Date(entry.getTimestamp()) + " " + entry.getLevel() + " " + entry.getMessage());
        }
    }
}

