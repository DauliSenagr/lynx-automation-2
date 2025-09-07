package com.adda52.utils.database.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.adda52.logging.Logging;

import javax.sql.DataSource;

/**
 * @author Dauli Sengar
 * @since 2nd July 2023
 * This class manages a connection pool for MySQL databases using HikariCP DataSource.
 */
public class MySqlConnectionPool implements Logging {
    private volatile DataSource dataSource;

    /**
     * Gets the DataSource instance for the provided database URL, username, and password.
     * If the DataSource instance is not created yet, it creates a new one using HikariConfig.
     *
     * @param dbUrl      The URL of the MySQL database
     * @param dbUserName The username for database access
     * @param dbPassword The password for database access
     * @return The DataSource instance for the specified database
     */
    public DataSource getDataSource(String dbUrl, String dbUserName, String dbPassword) {
        if (dataSource == null) {
            synchronized (MySqlConnectionPool.class) {
                if (dataSource == null) {
                    dataSource = createDataSource(dbUrl, dbUserName, dbPassword);
                }
            }
        }
        getLogger().info("Connecting to SQL database at URL: " + dbUrl);
        return dataSource;
    }

    /**
     * Creates and configures a HikariCP DataSource instance for the specified database URL, username, and password.
     *
     * @param dbUrl      The URL of the MySQL database
     * @param dbUserName The username for database access
     * @param dbPassword The password for database access
     * @return The created DataSource instance configured with HikariConfig
     */
    private DataSource createDataSource(String dbUrl, String dbUserName, String dbPassword) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUserName);
        config.setPassword(dbPassword);
        config.setMaximumPoolSize(Constants.getMaxConnectionPoolSize());
        config.setMinimumIdle(Constants.getMinIdleConnections());
        return new HikariDataSource(config);
    }
}

