package com.adda52.utils.database.sql;

import lombok.Getter;

/**
 * @author Dauli Sengar
 * @since 2nd July 2023
 * Represents the configuration for a database including URL, username, and password.
 */
@Getter
public class DatabaseConfig {

    /**
     * The URL of the database.
     */
    private String url;

    /**
     * The username used to connect to the database.
     */
    private String username;

    /**
     * The password used to connect to the database.
     */
    private String password;

    /**
     * Constructs a DatabaseConfig instance with the provided URL, username, and password.
     *
     * @param url      The URL of the database
     * @param username The username used for database access
     * @param password The password used for database access
     */
    public DatabaseConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * Sets the URL of the database.
     *
     * @param url The URL of the database to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Sets the username used for database access.
     *
     * @param username The username to set for database access
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the password used for database access.
     *
     * @param password The password to set for database access
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
