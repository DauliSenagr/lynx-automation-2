package utils.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

public class CsvUtils {


    public static void writeToCSV(ResultSet resultSet, String filePath, Map<String, String> columnMapping) throws IOException, SQLException {
        Path path = Paths.get(filePath);

        // Delete the existing file if it exists
        try {
            Files.deleteIfExists(path);
            System.out.println("Deleted existing file: " + filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete the file: " + e.getMessage());
        }

        // Create a new file and write content to it
        try (FileWriter writer = new FileWriter(filePath)) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Write header row (new column names)
            for (int i = 1; i <= columnCount; i++) {
                String originalColumnName = metaData.getColumnName(i);
                String newColumnName = columnMapping.getOrDefault(originalColumnName, originalColumnName);
                writer.append(newColumnName);
                if (i < columnCount) {
                    writer.append(",");
                }
            }
            writer.append("\n");

            // Write data rows
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    writer.append(resultSet.getString(i));
                    if (i < columnCount) {
                        writer.append(",");
                    }
                }
                writer.append("\n");
            }

            System.out.println("CSV file created successfully at: " + filePath);
        }
    }

}

