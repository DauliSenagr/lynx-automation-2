package com.adda52.driver;

import com.adda52.logging.Logging;
import com.adda52.utils.architecture.Architecture;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.IOSMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dauli Sengar
 * @since 15th May 2022
 * The DriverFactory class is responsible for creating instances of different WebDriver types
 * based on the provided DriverType enum and setting them in the DriverManager for thread-safe retrieval.
 * It implements Logging to handle logging functionalities.
 */

public class DriverFactory implements Logging {
    /**
     * Creates an instance of the specified WebDriver based on the provided DriverType.
     * Sets the created WebDriver instance in the DriverManager for thread-safe retrieval.
     *
     * @param driverName The type of WebDriver to create: CHROME, FIREFOX, ANDROID, or IOS.
     */
    public synchronized void createDriverInstance(DriverType driverName) {
        WebDriver driver = switch (driverName) {
            case CHROME -> createChromeDriver();
            case FIREFOX -> createFirefoxDriver();
            case ANDROID -> createAndroidDriver();
            case IOS -> createIOSDriver();
            default -> throw new IllegalStateException("Unexpected value: " + driverName);
        };

        DriverManager.setDriver(driver);
    }

    /**
     * Creates an instance of the Chrome Driver.
     * Initializes the WebDriverManager for Chrome based on the system architecture and OS type,
     * sets up the appropriate driver version, and returns a new ChromeDriver instance with predefined options.
     *
     * @return A new instance of {@link ChromeDriver} configured with predefined options based on the initialized WebDriverManager settings.
     */
    private WebDriver createChromeDriver() {
        WebDriverManager manager = WebDriverManager.getInstance(DriverManagerType.CHROME);

        if (is64bits() && Architecture.getOperatingSystemType().equals(Architecture.OSType.WINDOWS)) {
            getLogger().info("Creating 64-bit instance of Chrome driver for Windows");
            manager.arch64().setup();
        } else if (!is64bits() && Architecture.getOperatingSystemType().equals(Architecture.OSType.WINDOWS)) {
            getLogger().info("Creating 32-bit instance of Chrome driver for Windows");
            manager.arch32().setup();
        } else {
            getLogger().info("Creating instance of Chrome driver for " + Architecture.getOperatingSystemType());
            manager.setup();
        }

        return new ChromeDriver(getChromeOptions());
    }

    /**
     * Creates an instance of the Firefox Driver.
     * Initializes the WebDriverManager for Firefox based on the system architecture and OS type,
     * sets up the appropriate driver version, and returns a new FirefoxDriver instance.
     *
     * @return A new instance of {@link FirefoxDriver} based on the initialized WebDriverManager settings.
     */
    private WebDriver createFirefoxDriver() {
        WebDriverManager manager = WebDriverManager.getInstance(DriverManagerType.FIREFOX);

        if (is64bits() && Architecture.getOperatingSystemType().equals(Architecture.OSType.WINDOWS)) {
            getLogger().info("Creating 64-bit instance of Firefox driver for Windows");
            manager.arch64().setup();
        } else if (!is64bits() && Architecture.getOperatingSystemType().equals(Architecture.OSType.WINDOWS)) {
            getLogger().info("Creating 32-bit instance of Firefox driver for Windows");
            manager.arch32().setup();
        } else {
            getLogger().info("Creating instance of Firefox driver for " + Architecture.getOperatingSystemType());
            manager.setup();
        }

        return new FirefoxDriver();
    }

    /**
     * Creates an Appium Instance for Android.
     *
     * @return An instance of WebDriver for Android.
     */
    private WebDriver createAndroidDriver() {
        getLogger().info("Creating Appium Instance for Android");
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.FULL_RESET, false);
        capabilities.setCapability(MobileCapabilityType.NO_RESET, false);
        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, Constants.getAppiumCommandTimeOut());
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.ANDROID_UIAUTOMATOR2);
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, Platform.ANDROID);
        capabilities.setCapability(AndroidMobileCapabilityType.AUTO_GRANT_PERMISSIONS, true);
        capabilities.setCapability(AndroidMobileCapabilityType.UNICODE_KEYBOARD, true);
        capabilities.setCapability(AndroidMobileCapabilityType.RESET_KEYBOARD, true);
        capabilities.setCapability(MobileCapabilityType.UNHANDLED_PROMPT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
        capabilities.setCapability("autoWebview", true);
        capabilities.setCapability("autoWebviewTimeout", 10000);

        URL appiumServerURL = null;

        if (Constants.getAppiumExecutionType().equals("local")) {
            getLogger().info("Starting application on local device");
            capabilities.setCapability("deviceName", Constants.getDeviceName());
            capabilities.setCapability("appPackage", Constants.getAppPackage());
            capabilities.setCapability("appActivity", Constants.getAppLaunchActivity());
            capabilities.setCapability("--allow-insecure-localhost", "chromedriver_autodownload");
            try {
                appiumServerURL = new URL("http://localhost:4723/wd/hub");
            } catch (MalformedURLException e) {
                getLogger().error("Unable to set Appium URL", e);
            }
        } else if (Constants.getAppiumExecutionType().equals("lambdatest")) {
            getLogger().info("Starting application on lambdatest");
            capabilities.setCapability("w3c", true);
            capabilities.setCapability("deviceName", Constants.getLambdaTestDeviceName());
            capabilities.setCapability("isRealMobile", true);
            capabilities.setCapability("video", true);
            capabilities.setCapability("app", Constants.getLambdaTestAppName());
            capabilities.setCapability("appProfiling", true);
            capabilities.setCapability("devicelog", true);
            capabilities.setCapability("build", Constants.getBuildName());
            capabilities.setCapability("project", Constants.getProductName());
            capabilities.setCapability("autoAcceptAlerts", true);
            capabilities.setCapability("console", true);
            capabilities.setCapability("network", true);
            capabilities.setCapability("idleTimeout",305);

            try {
                appiumServerURL = new URL("https://" + Constants.getLambdaTestUserName() + ":" + Constants.getLambdaTestKey() + "@" + Constants.getLambdaTestServerUrl());
            } catch (MalformedURLException e) {
                getLogger().error("Unable to set Appium URL", e);
            }
        }

        // Create an instance of the AppiumDriver with the desired capabilities
        assert appiumServerURL != null;
        return new AndroidDriver(appiumServerURL, capabilities);
    }

    /**
     * Creates an Appium Instance for IOS.
     *
     * @return An instance of WebDriver for IOS.
     */
    private WebDriver createIOSDriver() {
        getLogger().info("Creating Appium Instance for IOS");

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.FULL_RESET, true);
        capabilities.setCapability(MobileCapabilityType.NO_RESET, false);
        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, Constants.getAppiumCommandTimeOut());
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, Platform.IOS);
        capabilities.setCapability(IOSMobileCapabilityType.RESET_ON_SESSION_START_ONLY, true);
        capabilities.setCapability(IOSMobileCapabilityType.AUTO_ACCEPT_ALERTS, true);
        URL appiumServerURL = null;

        if (Constants.getAppiumExecutionType().equals("lambdatest")) {
            getLogger().info("Starting application on lambdatest");
            capabilities.setCapability("w3c", true);
            capabilities.setCapability("platformName", "ios");
            capabilities.setCapability("deviceName", "iPhone XR");
            capabilities.setCapability("platformVersion", "15");
            capabilities.setCapability("isRealMobile", true);

            try {
                appiumServerURL = new URL("https://" + Constants.getLambdaTestUserName() + ":" + Constants.getLambdaTestKey() + "@" + Constants.getLambdaTestServerUrl());
            } catch (MalformedURLException e) {
                getLogger().error("Unable to set Appium URL", e);
            }

        }
        // Create an instance of the AppiumDriver with the desired capabilities
        assert appiumServerURL != null;
        return new IOSDriver(appiumServerURL, capabilities);
    }


    /**
     * Checks whether the current system architecture is 64-bit.
     *
     * @return {@code true} if the system architecture is 64-bit, {@code false} otherwise.
     */
    protected boolean is64bits() {
        return Architecture.is64bits();
    }


    /**
     * Retrieves Chrome options for WebDriver configuration.
     * This method configures various ChromeOptions and preferences for WebDriver.
     *
     * @return ChromeOptions object with specified preferences and arguments.
     */
    private ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.extensions_to_open", "application/xml");
        prefs.put("safebrowsing.enabled", true);

        options.addArguments(
                "--safebrowsing-disable-download-protection",
                "--safebrowsing-disable-extension-blacklist",
                "--disable-notifications",
                "--start-maximized",
                "--disable-features=EnableEphemeralFlashPermission",
                "--disable-infobars",
                "--disable-dev-shm-usage",
                "--remote-allow-origins=*"
        );

        if (Constants.getHeadlessChromeFlag().equalsIgnoreCase("true")) {
            options.addArguments("--headless");
        }

        options.setExperimentalOption("prefs", prefs);

        return options;
    }


}
