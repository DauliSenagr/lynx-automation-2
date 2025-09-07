package com.adda52.controllers;

import com.adda52.driver.DriverManager;
import com.adda52.logging.Logging;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dauli Sengar
 * @since 20th Dec 2023
 * Controller class for managing browser-related actions using WebDriver.
 */
public class BrowserController {
    static Logger logging;

    static {
        logging = Logging.getLogger(BrowserController.class);
    }

    /**
     * Opens the specified URL in the WebDriver instance.
     *
     * @param driver The WebDriver instance
     * @param url    The URL to navigate to
     */
    public static void goToUrl(WebDriver driver, String url) {
        logging.info("Opening the URL: " + url);
        driver.get(url);
    }

    /**
     * Maximizes the browser window.
     *
     * @param driver The WebDriver instance
     */
    public static void maximizeBrowser(WebDriver driver) {
        driver.manage().window().maximize();
    }

    /**
     * Retrieves the current window handle.
     *
     * @param driver The WebDriver instance
     * @return The current window handle as a String
     */
    public static String getCurrentWindowHandle(WebDriver driver) {
        return driver.getWindowHandle();
    }

    /**
     * Switches the WebDriver focus to the default content.
     *
     * @param driver The WebDriver instance
     */
    public static void switchToDefaultContent(WebDriver driver) {
        driver.switchTo().defaultContent();
    }

    /**
     * Opens a specified number of new tabs with the given URL in the WebDriver instance.
     *
     * @param driver   The WebDriver instance
     * @param tabCount The number of tabs to be opened
     * @param url      The URL to be opened in each tab
     */
    public static void openNewTabsWithUrl(WebDriver driver, int tabCount, String url) {
        for (int i = 0; i < tabCount; i++) {
            driver.switchTo().newWindow(WindowType.TAB).get(url);
        }
    }

    /**
     * Switches the focus of the WebDriver to the specified tab number.
     *
     * @param tabNumber The number of the tab to switch to
     */
    public static void switchToTab(int tabNumber) {
        List<String> windowHandles = new ArrayList<>(DriverManager.getDriver().getWindowHandles());
        DriverManager.getDriver().switchTo().window(windowHandles.get(tabNumber - 1));
    }

    /**
     * Switches the focus of the WebDriver to the specified window handle.
     *
     * @param windowHandle The window handle to switch to
     */
    public static void switchToWindow(String windowHandle) {
        DriverManager.getDriver().switchTo().window(windowHandle);
    }

    /**
     * Retrieves all window handles currently available to the WebDriver instance.
     *
     * @param driver The WebDriver instance
     * @return List of window handles as Strings
     */
    public static List<String> getAllWindowHandles(WebDriver driver) {
        return new ArrayList<>(driver.getWindowHandles());
    }

    /**
     * Closes all open windows in the WebDriver instance.
     *
     * @param driver The WebDriver instance
     */
    public static void closeAllOpenWindows(WebDriver driver) {
        for (String windowHandle : driver.getWindowHandles()) {
            driver.switchTo().window(windowHandle);
            driver.close();
        }
    }

    /**
     * Moves the window of the WebDriver instance to the specified position.
     *
     * @param driver The WebDriver instance
     * @param x      The x-coordinate to move the window
     * @param y      The y-coordinate to move the window
     */
    public static void moveWindowToPosition(WebDriver driver, int x, int y) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.moveTo(" + x + "," + y + ")");
    }

    /**
     * Resizes the window of the WebDriver instance to the specified dimensions.
     *
     * @param driver     The WebDriver instance
     * @param targetSize The target size/dimensions for the window
     */
    public static void resizeWindow(WebDriver driver, Dimension targetSize) {
        driver.manage().window().setSize(targetSize);
    }

    /**
     * Refresh the current browser window.
     */
    public static void refreshBrowser() {
        DriverManager.getDriver().navigate().refresh();
    }

    /**
     * Close the current browser window.
     */
    public static void closeBrowser() {
        DriverManager.getDriver().close();
    }

    /**
     * Clear all cookies in the current browser session.
     */
    public static void clearAllCookies() {
        DriverManager.getDriver().manage().deleteAllCookies();
    }

    /**
     * Get the current URL of the browser.
     *
     * @return the current URL as a String.
     */
    public static String getCurrentUrl() {
        return DriverManager.getDriver().getCurrentUrl();
    }

}