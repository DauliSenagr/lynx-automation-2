package com.adda52.utils.redis;

import com.adda52.logging.Logging;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author Dauli Sengar
 * @since 23rd August 2022
 * Utility class for managing Jedis connections to Redis.
 * Implements Logging for logging capabilities.
 */

public class RedisUtils implements Logging {

    private static RedisUtils instance;
    private final String host = Constants.getRedisHost();
    private final int port = Constants.getRedisPort();
    private JedisPool jedisPool;

    private RedisUtils() {
        // Private constructor to prevent direct instantiation
    }

    /**
     * Retrieves the singleton instance of JedisUtils.
     *
     * @return The singleton instance of JedisUtils
     */
    public static synchronized RedisUtils getInstance() {
        if (instance == null) {
            instance = new RedisUtils();
        }
        return instance;
    }

    /**
     * Initializes the JedisPool if not already initialized.
     */
    private synchronized void initializeJedisPool() {
        if (jedisPool == null) {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            jedisPool = new JedisPool(poolConfig, host, port);
        }
    }

    /**
     * Retrieves a Jedis instance from the JedisPool.
     *
     * @return The Jedis instance
     * @throws RuntimeException if unable to get a Redis connection
     */
    public Jedis getJedis() {
        getLogger().info("Attempting to connect with Redis on host: " + host);
        try {
            if (jedisPool == null) {
                initializeJedisPool();
            }
            return jedisPool.getResource();
        } catch (Exception e) {
            getLogger().error("Unable to get Redis connection", e);
            // Proper error handling based on your application's needs
            throw new RuntimeException("Error getting Redis connection", e);
        }
    }

    /**
     * Shuts down the JedisPool.
     */
    public void shutdown() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}
