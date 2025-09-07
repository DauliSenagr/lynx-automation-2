package com.adda52.utils.parsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FeatureFileParser {

    public static String getFeatureName(String featureFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(featureFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim(); // Remove leading and trailing spaces
                if (line.startsWith("Feature:")) {
                    // Extract and return the feature name
                    return line.substring("Feature:".length()).trim();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "Feature not found"; // If no feature line is found
    }


}
