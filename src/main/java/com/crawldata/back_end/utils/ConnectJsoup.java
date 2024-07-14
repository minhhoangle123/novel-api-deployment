package com.crawldata.back_end.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.HttpStatusException;

import java.io.IOException;

public class ConnectJsoup {
    private static final String USER_AGENT_STRING = "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; WOW64; Trident/6.0)";
    private static final int DEFAULT_TIMEOUT = 8 * 1000; // 8 seconds
    private static final int MAX_RETRIES = 3;

    /**
     * Connects to the given URL using Jsoup and returns the Document.
     * Retries the connection in case of a timeout or HTTP error, except for HTTP 404.
     *
     * @param url The URL to connect to.
     * @return The Document object.
     * @throws IOException If an I/O error occurs.
     */
    public static Document connect(String url) throws IOException {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                return Jsoup.connect(url)
                        .userAgent(USER_AGENT_STRING)
                        .timeout(DEFAULT_TIMEOUT)
                        .get();
            } catch (HttpStatusException e) {
                if (e.getStatusCode() == 404) {
                    System.out.println("HTTP 404 error fetching URL: " + url);
                    throw new IOException("HTTP 404 error fetching URL: " + url);
                } else {
                    System.out.println("HTTP error fetching URL: " + url + " Status=" + e.getStatusCode());
                }
            } catch (IOException e) {
                System.out.println("Failed to connect to " + url + " on attempt " + (attempt + 1));
            }
            attempt++;
            if (attempt < MAX_RETRIES) {
                try {
                    Thread.sleep(1000); // Wait before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } else {
                throw new IOException("Max retries reached for URL: " + url);
            }
        }
        return null;
    }
}
