package com.adda52.utils.file;

import com.adda52.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;


/**
 * Utility class for handling file operations.
 */
public class FileUtils implements Logging {

    /**
     * Deletes files containing the specified name within a directory.
     *
     * @param directory         The directory in which to search for files.
     * @param fileToBeDeleted   The name of the files to be deleted.
     */
    public void deleteFile(File directory, String fileToBeDeleted) {
        File[] dirContents = directory.listFiles();
        if (dirContents != null) {
            for (File dirContent : dirContents) {
                if (dirContent.getName().contains(fileToBeDeleted)) {
                    dirContent.delete();
                }
            }
        }
    }

    /**
     * Recursively deletes the contents of a folder.
     *
     * @param folder The folder whose contents need to be deleted.
     */
    public void deleteFolderContent(Path folder) {
        try (Stream<Path> paths = Files.walk(folder)) {
            paths.sorted((path1, path2) -> -path1.compareTo(path2)) // Delete from deepest first
                    .map(path -> (AutoCloseable) () -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete: " + path, e);
                        }
                    })
                    .forEach(this::closeSilently);
            getLogger().info("Cleared content for the folder.");
        } catch (IOException e) {
            getLogger().error("Failed to clear folder content.", e);
        }
    }

    private void closeSilently(AutoCloseable closeable) {
        try {
            closeable.close();
        } catch (Exception ignored) {
            // Ignore exceptions on close
        }
    }

    /**
     * Checks if a file containing the specified name exists within a directory.
     *
     * @param directory The directory in which to search for files.
     * @param name      The name of the file to check for existence.
     * @return True if the file exists, otherwise false.
     */
    public boolean doesFileExist(File directory, String name) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().contains(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Deletes a file.
     *
     * @param filePath The path of the file to be deleted.
     */
    public void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.delete()) {
                getLogger().info("File deleted successfully.");
            } else {
                getLogger().error("Failed to delete the file.");
            }
        } else {
            getLogger().info("File does not exist.");
        }
    }

    public String readFileContent(String filePath) {
        try {
            return Files.readString(Path.of(filePath));
        } catch (IOException e) {
            getLogger().error("Failed to read file content.", e);
            return null;
        }
    }
}
