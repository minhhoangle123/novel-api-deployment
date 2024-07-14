package com.crawldata.back_end.utils;

import java.nio.file.FileSystems;

/**
 * Utility class for common application operations.
 * Provides methods related to file paths and directory handling.
 */
public class AppUtils {
    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private AppUtils() {
    }
     /**
     * The current directory path.
     */
    public static String curDir = System.getProperty("user.dir");
    /**
     * The cache directory path, initialized to the current directory.
     */
    public static String cacheDir = curDir;

    /**
     * The file separator used by the underlying file system.
     */
    public static final String SEPARATOR = FileSystems.getDefault().getSeparator();

    /**
     * Adjusts the current directory path to remove any trailing file separator.
     * This method is useful for ensuring consistency in directory path representation.
     */
    public static void doLoad() {
        try {
            if (curDir.endsWith(SEPARATOR))
                curDir = curDir.substring(0, curDir.length() - 1);
        } catch (Exception e) {
            // Print stack trace if an exception occurs, but continue execution
            e.printStackTrace();
        }
    }
}
