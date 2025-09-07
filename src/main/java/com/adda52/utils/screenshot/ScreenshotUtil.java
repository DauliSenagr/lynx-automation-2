package com.adda52.utils.screenshot;

import com.adda52.driver.DriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

/**
 * @author Dauli Sengar
 * @since 15th May 2022
 * Utility class for capturing and manipulating screenshots.
 */
public class ScreenshotUtil {
    private final String screenshotDir = System.getProperty("user.dir") + "/test-output/Screenshots/";
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("ddMMyyyy_HHmmss");

    /**
     * Captures the screenshot as Base64 encoded string.
     *
     * @param driver WebDriver instance
     * @return Base64 encoded string representation of the screenshot
     * @throws IOException If an I/O error occurs while capturing the screenshot
     */
    public String getBase64Screenshot(WebDriver driver) throws IOException {
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        saveScreenshot(src, "_base64");
        byte[] fileContent = FileUtils.readFileToByteArray(src);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(fileContent);
    }

    /**
     * Captures and saves the screenshot.
     *
     * @param driver WebDriver instance
     * @return Path to the saved screenshot
     * @throws IOException If an I/O error occurs while capturing the screenshot
     */
    public String getScreenshot(WebDriver driver) throws IOException {
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        return saveScreenshot(src, "_up");
    }

    private String saveScreenshot(File src, String suffix) throws IOException {
        String path;
        Date currentDate = new Date();
        String formattedDate = dateFormatter.format(currentDate);
        path = screenshotDir + "Screenshot_" + formattedDate + suffix + ".png";
        FileUtils.copyFile(src, new File(path));
        return path;
    }


    /**
     * Compresses the image represented by the provided byte array.
     *
     * @param screenshotBytes Byte array representing the image
     * @param quality         Compression quality (0.0 to 1.0)
     * @return Compressed byte array of the image
     */
    public static byte[] compressImage(byte[] screenshotBytes, Float quality) {
        try {
            // Load the screenshot bytes into a BufferedImage
            BufferedImage screenshotImage = ImageIO.read(new ByteArrayInputStream(screenshotBytes));

            // Create a ByteArrayOutputStream to hold the compressed image bytes
            ByteArrayOutputStream compressedOutputStream = new ByteArrayOutputStream();

            // Create a new BufferedImage with reduced quality
            BufferedImage compressedImage = new BufferedImage(screenshotImage.getWidth(), screenshotImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = compressedImage.createGraphics();
            graphics.drawImage(screenshotImage, 0, 0, null);
            graphics.dispose();

            // Get the writer for JPEG format
            ImageWriter imageWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam imageWriteParam = new JPEGImageWriteParam(null);
            imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            imageWriteParam.setCompressionQuality(quality);

            // Write the compressed image
            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(compressedOutputStream);
            imageWriter.setOutput(imageOutputStream);
            imageWriter.write(null, new IIOImage(compressedImage, null, null), imageWriteParam);

            // Cleanup
            imageOutputStream.close();
            imageWriter.dispose();

            // Get the compressed image bytes
            return compressedOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to compress the image", e);
        }
    }

    public static void takeElementScreenshot(WebElement element, String outputPath) throws IOException {
        File sourceFile = element.getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(sourceFile, new File(outputPath));
    }

    public static void captureSmartUIScreenshot(WebDriver driver, String screenshotName) {
        ((JavascriptExecutor) driver).executeScript("smartui.takeScreenshot=<" + screenshotName + ">");
    }


}
