package com.crawldata.back_end.utils;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;
import java.io.UnsupportedEncodingException;

public class HandleString {
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGESDHASHES = Pattern.compile("(^-|-$)");

    public static String makeSlug(String input) {
        String processed = input.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        processed = processed.replace("Đ", "D").replace("đ", "d")
                .replace("Ä", "A").replace("ä", "a")
                .replace("Ö", "O").replace("ö", "o")
                .replace("Ü", "U").replace("ü", "u")
                .replace("Á", "A").replace("á", "a")
                .replace("É", "E").replace("é", "e")
                .replace("Í", "I").replace("í", "i")
                .replace("Ó", "O").replace("ó", "o")
                .replace("Ú", "U").replace("ú", "u")
                .replace("Ñ", "N").replace("ñ", "n")
                .replace("Ç", "C").replace("ç", "c")
                .replace("Å", "A").replace("å", "a");
        String nowhitespace = WHITESPACE.matcher(processed).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        slug = EDGESDHASHES.matcher(slug).replaceAll("");
        slug = slug.replaceAll("-{2,}", "-");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    public static String getValidURL(String invalidURLString) {
        try {
            // Convert the String and decode the URL into the URL class
            URL url = new URL(URLDecoder.decode(invalidURLString, StandardCharsets.UTF_8.toString()));
            // Use the methods of the URL class to achieve a generic solution
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            // return String
            return uri.toString();
        } catch (URISyntaxException | UnsupportedEncodingException | MalformedURLException ignored) {
            return null;
        }
    }
}

