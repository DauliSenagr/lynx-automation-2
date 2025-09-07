package com.adda52.device;

import com.adda52.controllers.ExecutionController;
import com.adda52.driver.DriverManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;

public class DeviceController {

    /**
     * Press the back button on an Android device using the specified AndroidDriver.
     *
     * @param driver the AndroidDriver to use for pressing the back button.
     */
    public static void pressAndroidDeviceBackButton(AndroidDriver driver) {
        ((AndroidDriver) DriverManager.getDriver()).pressKey(new KeyEvent().withKey(AndroidKey.BACK));
        // driver.pressKey(new KeyEvent().withKey(AndroidKey.BACK));
    }

    public static void restartApp(AndroidDriver driver) {
        String packageName = ((AndroidDriver) driver).getCurrentPackage();
        driver.terminateApp(packageName);
        driver.activateApp(packageName);
    }

    public static void restartApp(AndroidDriver driver, String packageName) {
        driver.terminateApp(packageName);
        ExecutionController.pauseExecution(2);
        driver.activateApp(packageName);
    }

    public static void resetApp(AndroidDriver driver){
        driver.resetApp();
    }


}
