package com.adda52.utils.database.sql;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dauli Sengar
 * @since 2nd July 2023
 * Manages DataSources for various databases based on their configurations.
 */
public class DatabaseManager {

    /**
     * Map to store DataSources with their corresponding names.
     */
    private static final Map<String, DataSource> dataSourceMap = new HashMap<>();

    /**
     * Prefix for database instances.
     */
    public static String dataBasePrefix;
    public static String psqlDataBasePrefix;

    /**
     * Initializes DataSources using provided database configurations.
     * Creates and adds DataSources to the dataSourceMap based on the configurations.
     *
     * @param databaseConfigs A map containing database names as keys and their configurations as values
     */
    public void initializeDataSources(Map<String, DatabaseConfig> databaseConfigs) {
        dataBasePrefix = Constants.getSqlDbPrefix();
        for (Map.Entry<String, DatabaseConfig> entry : databaseConfigs.entrySet()) {
            String dataSourceName = entry.getKey();
            DatabaseConfig config = entry.getValue();
            DataSource dataSource = new MySqlConnectionPool().getDataSource(config.getUrl(), config.getUsername(), config.getPassword());
            dataSourceMap.put(dataSourceName, dataSource);
        }
    }

    public void initializePSQLDataSources(Map<String, DatabaseConfig> databaseConfigs) {
        psqlDataBasePrefix = Constants.getPSQLPrefix();
        for (Map.Entry<String, DatabaseConfig> entry : databaseConfigs.entrySet()) {
            String dataSourceName = entry.getKey();
            DatabaseConfig config = entry.getValue();
            DataSource dataSource = new MySqlConnectionPool().getDataSource(config.getUrl(), config.getUsername(), config.getPassword());
            dataSourceMap.put(dataSourceName, dataSource);
        }

    }


    /**
     * Retrieves a DataSource based on the provided dataSourceName.
     *
     * @param dataSourceName The name of the DataSource to retrieve
     * @return The DataSource corresponding to the provided name
     */
    public static DataSource getDataSource(String dataSourceName) {
        return dataSourceMap.get(dataSourceName);
    }
}
