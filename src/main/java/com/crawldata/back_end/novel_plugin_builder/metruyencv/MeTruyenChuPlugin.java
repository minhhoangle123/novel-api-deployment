package com.crawldata.back_end.novel_plugin_builder.metruyencv;

import com.crawldata.back_end.model.Author;
import com.crawldata.back_end.model.Chapter;
import com.crawldata.back_end.model.Novel;
import com.crawldata.back_end.novel_plugin_builder.PluginFactory;
import com.crawldata.back_end.response.DataResponse;
import com.crawldata.back_end.utils.AppUtils;
import com.crawldata.back_end.utils.ConnectJsoup;
import com.crawldata.back_end.utils.DataResponseUtils;
import com.crawldata.back_end.utils.HandleString;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MeTruyenChuPlugin implements PluginFactory {
    private final String FILTER_NOVEL_API = "https://backend.metruyencv.com/api/books?filter[keyword]=%s&filter[state]=published&limit=100&page=1&include=author";
    private final String NOVEL_LIST_CHAPTERS_API = "https://backend.metruyencv.com/api/chapters?filter[book_id]=%s";
    private final String NOVEL_DETAIL_API = "https://backend.metruyencv.com/api/books/%s";
    private final String AUTHOR_DETAIL_API = "https://backend.metruyencv.com/api/books?filter[author]=%s&include=author&limit=100&page=1&filter[state]=published";
    private final String ALL_NOVELS_API = "https://backend.metruyencv.com/api/books?include=author&sort=-view_count&limit=20&page=%s&filter[state]=published";
    private final String NOVEL_SEARCH_API = "https://backend.metruyencv.com/api/books/search?keyword=%s&limit=20&page=%s&sort=-view_count&filter[state]=published";
    private final String CHAPTER_DETAIL_API = "https://metruyencv.com/truyen/%s/%s";
    private static final String CHROME_DRIVER_PATH = "/novel_plugins/chromedriver.exe";
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; WOW64; Trident/6.0)";

    private static final int ITEMS_PER_PAGE = 30;
    private final int DEFAULT_PAGE_NUMBER = 1;
    private final int MAX_RETRIES = 3;

    /**
     * Reverses a slug string by replacing dashes and spaces with URL-encoded spaces.
     *
     * @param slug The slug string to reverse.
     * @return The reversed slug string.
     */
    private String reverseSlugging(String slug) {
        return slug.replaceAll("-", "%20").replaceAll(" ", "%20");
    }

    /**
     * Connects to the given API URL and returns the JSON response as a JsonObject.
     * If the connection fails, it retries the request up to the specified number of times.
     *
     * @param url The URL of the API to connect to.
     * @return The JSON response as a JsonObject, or null if an error occurs after all retries.
     */
    public JsonObject connectAPI(String url) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(url);
                request.setHeader("User-Agent", USER_AGENT);
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            String result = EntityUtils.toString(entity);
                            return new Gson().fromJson(result, JsonObject.class);
                        }
                    } else if (statusCode == 404) {
                        System.out.println("HTTP 404 error fetching URL: " + url);
                        return null;
                    } else {
                        System.out.println("HTTP error fetching URL: " + url + " Status=" + statusCode);
                    }
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
                System.out.println("Max retries reached for URL: " + url);
                return null;
            }
        }
        return null;
    }

    /**
     * Retrieves the novel details by its slug.
     *
     * @param slug The slug of the novel.
     * @return The JsonObject representing the novel details.
     */
    public JsonObject getNovelDetailBySlug(String slug) {
        String apiUrl = String.format(FILTER_NOVEL_API, reverseSlugging(slug));

        JsonObject jsonObject = connectAPI(apiUrl);
        if (jsonObject != null && jsonObject.has("data")) {
            JsonArray dataArray = jsonObject.getAsJsonArray("data");
            // Loop through the data array to find the matching slug
            for (int i = 0; i < dataArray.size(); i++) {
                JsonObject novel = dataArray.get(i).getAsJsonObject();
                if (novel.get("slug").getAsString().equals(slug)) {
                    return novel;
                }
            }
        }
        return null;
    }

    /**
     * Creates a Novel object from the given JsonObject.
     *
     * @param novelObject The JsonObject representing the novel data.
     * @return The created Novel object.
     */
    public Novel createNovelByJsonData(JsonObject novelObject) {
        String name = novelObject.get("name").getAsString();
        String novelId = novelObject.get("slug").getAsString();
        JsonObject authorObject = novelObject.getAsJsonObject("author");
        Author author = new Author(authorObject.get("id").getAsString() + "-" + HandleString.makeSlug(authorObject.get("name").getAsString()), authorObject.get("name").getAsString());
        String idFirstChapter = "chuong-1";
        String image = novelObject.getAsJsonObject("poster").get("default").getAsString();
        String description = novelObject.get("synopsis").getAsString().replaceAll("\n", "<br>");
        return new Novel(novelId, name, image, description, author, idFirstChapter);
    }

    /**
     * Creates a Chapter object from the given JsonObject and Novel object.
     *
     * @param chapterObject The JsonObject representing the chapter data.
     * @param novel         The Novel object to which the chapter belongs.
     * @return The created Chapter object.
     */
    public Chapter createChapterByJsonData(JsonObject chapterObject, Novel novel) {
        String name = chapterObject.get("name").getAsString();
        String chapterId = "chuong-" + chapterObject.get("index").getAsString();
        return new Chapter(novel.getNovelId(), novel.getName(), chapterId, null, null, name, novel.getAuthor(), "");
    }

    /**
     * Extracts the chapter ID in the form of "chuong-{index}" from the script content based on the given direction.
     *
     * <p>This method searches for the JSON data associated with the specified direction ("next" or "previous")
     * in the provided script content, parses it, and extracts the chapter index. It then formats the index
     * as "chuong-{index}" and returns it.</p>
     *
     * @param direct  the direction to extract the chapter ID from ("next" or "previous").
     * @param content the script content containing the JSON data.
     * @return the chapter ID in the form of "chuong-{index}" if found, or {@code null} if the index is not found.
     */
    public String extractDirectionalChapterId(String direct, String content) {
        // Extract JSON data from script content
        String direction = "\"" + direct + "\":";
        String data = content.substring(
                content.indexOf(direction) + direction.length(),
                content.indexOf("}", content.indexOf(direction + "{")) + 1
        );
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(data, JsonObject.class);
        Integer index = jsonObject.get("index") != null ? jsonObject.get("index").getAsInt() : null;

        return index != null ? "chuong-" + index : null;
    }

    @Override
    public DataResponse getNovelChapterDetail(String novelId, String chapterId) {
        JsonObject novelObject = getNovelDetailBySlug(novelId);
        if (novelObject == null) {
            return DataResponseUtils.getErrorDataResponse("No such novel on this server");
        }
        Novel novel = createNovelByJsonData(novelObject);

        Chapter result = getContentChapter(novelId, chapterId);
        if (result == null) {
            return DataResponseUtils.getErrorDataResponse("Chapter not found");
        }
        // Create chapter details
        Chapter chapterDetail = new Chapter(novelId, novel.getName(), chapterId, result.getNextChapterId(), result.getPreChapterId(), result.getName(), novel.getAuthor(), result.getContent());

        // Prepare response
        DataResponse dataResponse = new DataResponse();
        dataResponse.setStatus("success");
        dataResponse.setData(chapterDetail);

        return dataResponse;
    }

    @Override
    public DataResponse getNovelListChapters(String novelId, int page) {
        JsonObject novelObject = getNovelDetailBySlug(novelId);
        if (novelObject == null) {
            return DataResponseUtils.getErrorDataResponse("Novel not found on this server");
        }
        Novel novel = createNovelByJsonData(novelObject);
        String apiUrl = String.format(NOVEL_LIST_CHAPTERS_API, novelObject.get("id").getAsString());
        JsonObject jsonObject = connectAPI(apiUrl);
        List<Chapter> chapterList = new ArrayList<>();

        int totalPages = 1;
        int totalItems = 0;

        if (jsonObject != null && jsonObject.has("data")) {
            JsonArray dataArray = jsonObject.getAsJsonArray("data");
            totalItems = dataArray.size();
            totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);

            // Calculate the start and end indices for the requested page
            int startIndex = (page - 1) * ITEMS_PER_PAGE;
            int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalItems);

            // Loop through the data and get the subset for the requested page
            for (int i = startIndex; i < endIndex; i++) {
                JsonObject chapterObject = dataArray.get(i).getAsJsonObject();
                chapterList.add(createChapterByJsonData(chapterObject, novel));
            }
        }

        return new DataResponse("success", totalPages, page, chapterList.size(), null, chapterList, "");
    }

    @Override
    public DataResponse getNovelListChapters(String novelId, String fromChapterId, int numChapters) {
        JsonObject novelObject = getNovelDetailBySlug(novelId);
        if (novelObject == null) {
            return DataResponseUtils.getErrorDataResponse("Novel not found on this server");
        }
        Novel novel = createNovelByJsonData(novelObject);
        String apiUrl = String.format(NOVEL_LIST_CHAPTERS_API, novelObject.get("id").getAsString());
        JsonObject jsonObject = connectAPI(apiUrl);
        List<Chapter> chapterList = new ArrayList<>();


        if (jsonObject != null && jsonObject.has("data")) {
            JsonArray dataArray = jsonObject.getAsJsonArray("data");
            // Loop through the data
            for (int i = 0; i < dataArray.size(); i++) {
                JsonObject chapterObject = dataArray.get(i).getAsJsonObject();
                chapterList.add(createChapterByJsonData(chapterObject, novel));
            }
        }

        return new DataResponse("success", DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_NUMBER, chapterList.size(), null, chapterList, "");
    }

    @Override
    public DataResponse getNovelDetail(String novelId) {
        JsonObject novelObject = getNovelDetailBySlug(novelId);
        if (novelObject != null) {
            Novel novel = createNovelByJsonData(novelObject);
            return new DataResponse("success", null, null, null, null, novel, "");
        } else {
            return DataResponseUtils.getErrorDataResponse("Novel not found on this server");
        }
    }

    @Override
    public DataResponse getAuthorDetail(String authorId) {
        String apiUrl = String.format(AUTHOR_DETAIL_API, authorId.split("-")[0]);
        JsonObject jsonObject = connectAPI(apiUrl);
        List<Novel> novelList = new ArrayList<>();
        if (jsonObject != null && jsonObject.has("data")) {
            JsonArray dataArray = jsonObject.getAsJsonArray("data");
            // Loop through the data
            for (int i = 0; i < dataArray.size(); i++) {
                JsonObject novelObject = dataArray.get(i).getAsJsonObject();
                novelList.add(createNovelByJsonData(novelObject));
            }
        }
        return new DataResponse("success", DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_NUMBER, novelList.size(), null, novelList, null);
    }

    @Override
    public DataResponse getAllNovels(int page, String search) {
        String apiUrl = String.format(ALL_NOVELS_API, page);
        JsonObject jsonObject = connectAPI(apiUrl);
        List<Novel> novelList = new ArrayList<>();
        if (jsonObject != null && jsonObject.has("data")) {
            JsonArray dataArray = jsonObject.getAsJsonArray("data");
            // Loop through the data
            for (int i = 0; i < dataArray.size(); i++) {
                JsonObject novelObject = dataArray.get(i).getAsJsonObject();
                novelList.add(createNovelByJsonData(novelObject));
            }
        } else {
            return DataResponseUtils.getErrorDataResponse("Failed to fetch novels");
        }
        int totalPages = jsonObject.getAsJsonObject("pagination").get("last").getAsInt();
        return new DataResponse("success", totalPages, page, novelList.size(), search, novelList, "");
    }

    @Override
    public DataResponse getNovelSearch(int page, String key, String orderBy) {
        String apiUrl = String.format(NOVEL_SEARCH_API, reverseSlugging(key), page);
        JsonObject jsonObject = connectAPI(apiUrl);
        List<Novel> novelList = new ArrayList<>();
        if (jsonObject != null && jsonObject.has("data")) {
            JsonArray dataArray = jsonObject.getAsJsonArray("data");
            // Loop through the data
            for (int i = 0; i < dataArray.size(); i++) {
                JsonObject novelObject = dataArray.get(i).getAsJsonObject();
                novelList.add(createNovelByJsonData(novelObject));
            }
        } else {
            return DataResponseUtils.getErrorDataResponse("Failed to fetch novels");
        }
        int totalPages = jsonObject.getAsJsonObject("pagination").get("last").getAsInt();
        return new DataResponse("success", totalPages, page, novelList.size(), key, novelList, "");
    }

    @Override
    public Chapter getContentChapter(String novelId, String chapterId) {
        String urlChapter = String.format(CHAPTER_DETAIL_API, novelId, chapterId);
        Document doc;
        try {
            doc = ConnectJsoup.connect(urlChapter);
            if (doc == null) {
                return null;
            }
            // Extract the script tag containing JSON data
            Element scriptElement = doc.select("script:containsData(window.chapterData)").first();
            if (scriptElement == null) {
                return null;
            }
            String scriptContent = scriptElement.html();

            String nextChapterId = extractDirectionalChapterId("next", scriptContent);
            String preChapterId = extractDirectionalChapterId("previous", scriptContent);

            // Extract the chapter name
            String chapterName = Objects.requireNonNull(doc.select("h2.text-center.text-gray-600.text-balance").first()).text();

            // Extract the chapter content
            Element contentElement = doc.select("div[data-x-bind='ChapterContent']").first();
            if (contentElement == null) {
                return null;
            }
            StringBuilder content = new StringBuilder(contentElement.html());

            if (content.length() < 2000) {
                // Initialize Selenium WebDriver in headless mode
                System.out.println("metruyencv is using chrome driver");
                System.setProperty("webdriver.chrome.driver", AppUtils.curDir + CHROME_DRIVER_PATH);
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--headless");
                options.addArguments("--disable-gpu");
                options.addArguments("--window-size=1920,1080");
                WebDriver driver = new ChromeDriver(options);
                driver.get(urlChapter);

                // Scroll and load more content
                JavascriptExecutor js = (JavascriptExecutor) driver;
                // Scroll to bottom
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(500);
                // Check if more content has loaded
                doc = Jsoup.parse(driver.getPageSource());
                Element loadMoreElement = doc.getElementById("load-more");

                if (loadMoreElement != null && !loadMoreElement.html().isEmpty()) {
                    // Remove all <canvas> tags within the loadMoreElement
                    Elements canvasElements = loadMoreElement.select("canvas");
                    for (Element canvas : canvasElements) {
                        canvas.remove();
                    }
                    // Append new content
                    content.append(loadMoreElement.html().replaceAll("\\n", "").replaceAll("(<br>){3,}", "<br><br>"));
                }
            }

            return new Chapter(novelId, null, chapterId, nextChapterId, preChapterId, chapterName, null, content.toString());
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
