package com.crawldata.back_end.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for file-related operations.
 */
public class FileUtils {

    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private FileUtils() {
    }

    /**
     * Validates a file path by replacing forward slashes with the file separator
     * defined in the {@link AppUtils} class.
     *
     * @param path The file path to validate.
     * @return The validated file path.
     */
    public static synchronized String validate(String path) {
        return path.replace("/", AppUtils.SEPARATOR);
    }

    public static byte[] stream2byte(InputStream in) {
        try {
            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
            }
        }
    }

    public static synchronized void byte2file(byte[] source, String savepath) {
        FileOutputStream fo = null;
        try {
            File f = new File(validate(savepath));
            if (!f.exists())
                f.createNewFile();
            fo = new FileOutputStream(f);
            fo.write(source);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fo != null) fo.close();
            } catch (IOException e) {
            }
        }
    }
}
