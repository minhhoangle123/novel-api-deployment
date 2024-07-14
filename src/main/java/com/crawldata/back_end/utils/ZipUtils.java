package com.crawldata.back_end.utils;

import org.zeroturnaround.zip.ZipUtil;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for reading data from zip files.
 */
public class ZipUtils {

    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private ZipUtils() {
    }

    /**
     * Reads the content of a file within a zip archive and returns it as a byte array.
     *
     * @param zipfile The zip file from which to read.
     * @param filepath The path of the file within the zip archive.
     * @return The content of the file as a byte array.
     */
    public static byte[] readInZipAsByte(File zipfile, String filepath) {
        return ZipUtil.unpackEntry(zipfile, filepath);
    }

    /**
     * Reads the content of a file within a zip archive and returns it as a string, assuming UTF-8 encoding.
     *
     * @param zipfile The zip file from which to read.
     * @param filepath The path of the file within the zip archive.
     * @return The content of the file as a string.
     */
    public static String readInZipAsString(File zipfile, String filepath) {
        return new String(readInZipAsByte(zipfile, filepath), StandardCharsets.UTF_8);
    }
}
