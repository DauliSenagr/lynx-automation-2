package com.adda52.utils.screenshot;

/**
 * @author Dauli Sengar
 * @since 15th May 2022
 * Utility class to manage and retrieve the path for screenshots.
 */
public class ScreenshotPathManager {
    private static final ThreadLocal<String> screenshotPath = new ThreadLocal<>();

    /**
     * Sets the path for the screenshot.
     *
     * @param path The path of the screenshot.
     */
    public static void setScreenshotPath(String path) {
        screenshotPath.set(path);
    }

    /**
     * Retrieves the path of the screenshot.
     *
     * @return The path of the screenshot.
     */
    public static String getScreenshotPath() {
        return screenshotPath.get();
    }
}
