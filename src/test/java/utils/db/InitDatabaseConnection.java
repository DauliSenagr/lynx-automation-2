package utils.db;

import com.adda52.logging.Logging;
import com.adda52.utils.database.sql.DatabaseConfig;
import com.adda52.utils.database.sql.DatabaseManager;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dauli Sengar
 * @since 14th July 2024
 * This class initializes database connections for the application.
 * It supports both MySQL and PostgreSQL databases.
 */
public class InitDatabaseConnection implements Logging {

    /**
     * Initializes database connections for application and KYC databases using MySQL.
     * The database configuration is obtained from constants.
     */
    public InitDatabaseConnection() {
        Map<String, DatabaseConfig> databaseConfigMap = new HashMap<>();
        String appDbUrl = "jdbc:mysql://" + Constants.getSqlDbHost() + ":" + Constants.getSqlDbPort() + "/";
        databaseConfigMap.put(DataBaseConnections.APPLICATION.getResource(), new DatabaseConfig(appDbUrl, Constants.getSqlDbUser(), Constants.getSqlDbPass()));
        String kycDbUrl = "jdbc:mysql://" + Constants.getKycDbHost() + ":" + Constants.getKycDbPort() + "/";
        databaseConfigMap.put(DataBaseConnections.KYC.getResource(), new DatabaseConfig(kycDbUrl, Constants.getKycDbUser(), Constants.getKycDbPass()));
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.initializeDataSources(databaseConfigMap);
    }

    /**
     * Initializes a PostgreSQL database connection for a specified database.
     * The database configuration is obtained from constants.
     *
     * @param database the name of the PostgreSQL database to connect to
     */
    public InitDatabaseConnection(String database) {
        Map<String, DatabaseConfig> databaseConfigMap = new HashMap<>();
        String postgreSqlDbUrl = "jdbc:postgresql://" + Constants.getPSQLHost() + ":" + Constants.getPSQLPort() + "/" + database;
        databaseConfigMap.put(DataBaseConnections.PSQL.getResource(), new DatabaseConfig(postgreSqlDbUrl, Constants.getPSQLUser(), Constants.getPSQLPass()));
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.initializePSQLDataSources(databaseConfigMap);
    }

    public InitDatabaseConnection(String database,String username, String password) {
        getLogger().error("username: "+username);
        getLogger().error("password: "+password);
        Map<String, DatabaseConfig> databaseConfigMap = new HashMap<>();
        String postgreSqlDbUrl = "jdbc:postgresql://" + Constants.getPSQLHost() + ":" + Constants.getPSQLPort() + "/" + database;
        databaseConfigMap.put(DataBaseConnections.PSQL.getResource(), new DatabaseConfig(postgreSqlDbUrl, username, password));
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.initializePSQLDataSources(databaseConfigMap);
    }


}
