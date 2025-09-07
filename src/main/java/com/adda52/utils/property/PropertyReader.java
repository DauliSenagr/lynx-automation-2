package com.adda52.utils.property;

import com.adda52.logging.Logging;

import java.io.*;
import java.util.Properties;

/**
 * @author Dauli Sengar
 * @since 15th May 2022
 * Utility class to read and manage properties from a file.
 */
public class PropertyReader implements Logging {

    private final Properties prop = new Properties();
    private final String propertyFile;

    /**
     * Constructs a PropertyReader object with the specified property file.
     *
     * @param propertyFile The name of the property file.
     */
    public PropertyReader(String propertyFile) {
        this.propertyFile = propertyFile;
        loadProperties();
    }

    /**
     * Loads properties from the property file.
     */
    private void loadProperties() {
        try (InputStream in = getClass().getResourceAsStream("/" + propertyFile)) {
            if (in != null) {
                prop.load(in);
                getLogger().debug("Loaded .properties file: " + propertyFile);
            } else {
                getLogger().error(propertyFile + " Property file not found");
            }
        } catch (IOException e) {
            getLogger().error("Error reading file " + propertyFile, e);
        }
    }

    /**
     * Retrieves a string property by its name.
     *
     * @param propertyName The name of the property.
     * @return The value of the property as a string.
     */
    public String getString(String propertyName) {
        return prop.getProperty(propertyName);
    }

    /**
     * Retrieves an integer property by its name.
     *
     * @param propertyName The name of the property.
     * @return The value of the property as an integer.
     */
    public Integer getInt(String propertyName) {
        int temp = -1;

        try {
            String propertyValue = prop.getProperty(propertyName);
            if (propertyValue != null) {
                temp = Integer.parseInt(propertyValue);
            } else {
                getLogger().error("Property " + propertyName + " does not exist.");
            }
        } catch (NumberFormatException e) {
            getLogger().error("The property named: " + propertyName + " cannot be parsed to an Int.");
        }
        return temp;
    }

    /**
     * Sets a string property with the specified name and value.
     *
     * @param propertyName  The name of the property.
     * @param propertyValue The value to set for the property.
     */
    public void setString(String propertyName, String propertyValue) {
        prop.setProperty(propertyName, propertyValue);
        saveProperties();
    }

    /**
     * Deletes a property by its name.
     *
     * @param propertyName The name of the property to delete.
     */
    public void deleteProperty(String propertyName) {
        prop.remove(propertyName);
        saveProperties();
    }

    /**
     * Adds a new property with the specified name and value.
     *
     * @param propertyName  The name of the property.
     * @param propertyValue The value to set for the new property.
     */
    public void addProperty(String propertyName, String propertyValue) {
        prop.setProperty(propertyName, propertyValue);
        saveProperties();
    }

    /**
     * Saves properties to the property file.
     */
    private void saveProperties() {
        try (OutputStream out = new FileOutputStream(propertyFile)) {
            prop.store(out, null);
            getLogger().debug("Saved properties to file: " + propertyFile);
        } catch (IOException e) {
            getLogger().error("Error saving properties to file", e);
        }
    }
}
