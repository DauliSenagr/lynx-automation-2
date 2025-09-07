package com.adda52.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Dauli Sengar
 * @since 15th May 2022
 * Interface providing logging capabilities.
 * Contains default and static methods to retrieve logger instances.
 */
public interface Logging {

    /**
     * Default method to get the logger instance associated with the implementing class.
     * Configures log4j and returns the logger.
     *
     * @return The logger instance for the implementing class
     */
    default Logger getLogger() {
        System.setProperty("log4j.configurationFile", "log4j2.xml");
        return LogManager.getLogger(getClass());
    }

    /**
     * Static method to get the logger instance for the specified class.
     * Configures log4j and returns the logger for the provided class.
     *
     * @param clazz The class for which the logger is requested
     * @return The logger instance for the specified class
     */
    static Logger getLogger(Class<?> clazz) {
        System.setProperty("log4j.configurationFile", "log4j2.xml");
        return LogManager.getLogger(clazz);
    }
}

