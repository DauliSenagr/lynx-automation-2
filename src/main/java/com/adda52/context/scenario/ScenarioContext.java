package com.adda52.context.scenario;

import com.adda52.logging.Logging;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dauli Sengar
 * @since 8th Jan 2024
 * Provides a thread-local context to store and retrieve scenario-specific data.
 * Uses a ThreadLocal instance to manage a map of key-value pairs associated with each scenario.
 */
public class ScenarioContext {

    // Thread-local storage to hold scenario-specific data
    private static final ThreadLocal<Map<String, Object>> scenarioData = ThreadLocal.withInitial(HashMap::new);

    /**
     * Sets the scenario-specific data identified by the given key.
     *
     * @param key   The key associated with the data to be stored
     * @param value The value to be stored in the scenario context
     */
    public static void setScenarioData(String key, Object value) {
        scenarioData.get().put(key, value);
    }

    /**
     * Retrieves the scenario-specific data associated with the provided key.
     *
     * @param key The key associated with the data to retrieve from the scenario context
     * @return The value corresponding to the provided key in the scenario context, or null if not found
     */
    public static Object getScenarioData(String key) {
        return scenarioData.get().get(key);
    }
}

