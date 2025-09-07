package com.adda52.utils.database.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.adda52.logging.Logging;
import org.apache.logging.log4j.Logger;

/**
 * @author Dauli Sengar
 * @since 17th Aug 2022
 * This class provides a connection pool to MongoDB and methods to access databases.
 */
public class MongoDataBasePool implements Logging {

    private static MongoClient mongoClient;
    private static final Object lock = new Object();

    static Logger logger;

    static {
        connect();
        logger = Logging.getLogger(MongoDataBasePool.class);

    }

    /**
     * Establishes a connection to the MongoDB server during class initialization.
     * Uses the connection URI from Constants to connect to the database.
     * Throws a RuntimeException if the connection fails.
     */
    private static void connect() {
        try {
            String uri = String.format("mongodb://%s:%s@%s/%s",
                    Constants.getMongoUser(),
                    Constants.getMongoPassword(),
                    Constants.getMongoHost(),
                    Constants.getMongoAdminDb());
            MongoClientURI mongoClientURI = new MongoClientURI(uri);
            mongoClient = new MongoClient(mongoClientURI);
        } catch (Exception e) {
            logger.error("Failed to connect to MongoDB: " + e.getMessage());
            throw new RuntimeException("Failed to connect to MongoDB.", e);
        }
    }

    /**
     * Retrieves the MongoDB database instance based on the provided database name.
     *
     * @param database The name of the MongoDB database to retrieve
     * @return The MongoDB database instance
     */
    public static MongoDatabase getDatabase(String database) {
        return mongoClient.getDatabase(database);
    }

    /**
     * Closes the MongoDB client connection.
     * If the client instance is not null, it closes the connection to the MongoDB server.
     */
    public static void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
