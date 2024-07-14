package com.crawldata.back_end.novel_plugin_builder.truyenfull;

import com.crawldata.back_end.model.Author;
import com.crawldata.back_end.model.Chapter;
import com.crawldata.back_end.model.Novel;
import com.crawldata.back_end.novel_plugin_builder.PluginFactory;
import com.crawldata.back_end.response.DataResponse;
import com.crawldata.back_end.utils.ConnectJsoup;
import com.crawldata.back_end.utils.DataResponseUtils;
import com.crawldata.back_end.utils.HandleString;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class  TruyenFullPlugin implements PluginFactory {

    private static String NOVEL_MAIN = "https://truyenfull.vn/";
    private static String API_TRUYENFULL_SEARCH = "https://api.truyenfull.vn/v1/tim-kiem?title=%s&page=%d";
    private static String API_DETAL_NOVEL = "https://api.truyenfull.vn/v1/story/detail/";

    /**
     * Retrieves the JSON response from the specified API URL.
     *
     * @param apiURL The URL of the API from which the JSON response is to be fetched.
     * @return A string containing the JSON response, or "error" if the connection fails
     * or the server response is not HTTP_OK (200).
     */
    public static String getJsonResponse(String apiURL) {
        try {
            URL url = new URL(apiURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return "error";
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Retrieves the total number of pages for a novel from a given URL.
     *
     * @param url The URL of the webpage where the novel's pagination is located.
     * @return The total number of pages of the novel as an integer. If the pagination
     * is not present or an error occurs, it defaults to 1.
     */
    public static int getNovelTotalPages(String url) {
        Document doc = null;
        try {
            doc = ConnectJsoup.connect(url);
            if(doc!=null) {
                Elements pages = doc.select("ul[class=pagination pagination-sm] li");
                int totalPages = 1;
                if (pages.size() != 0) {
                    StringBuilder linkEndPage = new StringBuilder();
                    Element page = pages.get(pages.size() - 2);
                    if (page.text().equals("Cuối »")) {
                        linkEndPage.append(page.select("a").attr("href"));
                        String linkValid = HandleString.getValidURL(linkEndPage.toString());
                        Document docPage = ConnectJsoup.connect(linkValid);
                        Elements allPage = docPage.select("ul[class=pagination pagination-sm] li");
                        totalPages = Integer.parseInt(allPage.get(allPage.size() - 2).text().split(" ")[0]);
                    } else if (page.text().equals("Trang tiếp")) {
                        Element pageNext = pages.get(pages.size() - 1);
                        if (pageNext.text().equals("Chọn trang Đi")) {
                            totalPages = Integer.parseInt(pages.get(pages.size() - 3).text().split(" ")[0]);
                        } else {
                            linkEndPage.append(pageNext.select("a").attr("href"));
                            Document docPage = ConnectJsoup.connect(linkEndPage.toString());
                            Elements allPage = docPage.select("ul[class=pagination pagination-sm] li");
                            totalPages = Integer.parseInt(allPage.get(allPage.size() - 1).text().split(" ")[0]);
                        }
                    } else {
                        totalPages = Integer.parseInt(page.text().split(" ")[0]);
                    }
                }
                return totalPages;
            }
            return 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Extracts the last segment, also known as the slug, from a given URL.
     *
     * @param url The full URL from which the end slug is to be extracted.
     * @return The end slug of the URL as a String.
     */
    public static String getEndSlugFromUrl(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    /**
     * Generates the identifier for the previous chapter based on the current chapter's ID.
     *
     * @param idChapter The ID of the current chapter in the format 'chuong-X'.
     * @return The ID of the previous chapter as a String, or null if the current chapter is the first.
     */
    public static String getValidPreChapter(String idChapter) {
        String[] parts = idChapter.split("-");
        int numberChap = Integer.parseInt(parts[parts.length - 1]);
        if (numberChap > 1) return "chuong-" + (numberChap - 1);
        return null;
    }

    /**
     * Determines the last chapter number available on a given webpage.
     *
     * @param url The URL of the webpage where the novel's chapters are listed.
     * @return The number of the last chapter as an integer. If pagination is not present,
     * it returns the count of chapters listed on the current page.
     */
    public static int getChapterEnd(String url) {
        StringBuilder linkEndPage = new StringBuilder();
        int chapterEnd = 0;
        try {
            Document doc = ConnectJsoup.connect(url);
            if(doc!=null) {
                Elements pages = doc.select("ul[class=pagination pagination-sm] li");
                if (pages.size() == 0) {
                    chapterEnd = doc.select("ul[class=list-chapter] li").size();
                    return chapterEnd;
                } else {
                    Element page = pages.get(pages.size() - 2);
                    if (page.text().equals("Trang tiếp")) {
                        Element pageNext = pages.get(pages.size() - 1);
                        linkEndPage.append(pageNext.select("a").attr("href"));
                    } else {
                        linkEndPage.append(page.select("a").attr("href"));
                    }
                }
                doc = ConnectJsoup.connect(linkEndPage.toString());
                Elements chaptersLink = doc.select("ul[class=list-chapter] li a");
                String linkEndChapter = chaptersLink.get(chaptersLink.size() - 1).attr("href");
                String idEndChapter = getEndSlugFromUrl(linkEndChapter);
                String[] parts = idEndChapter.split("-");
                chapterEnd = Integer.parseInt(parts[parts.length - 1]);
                return chapterEnd;
            }
            return 0;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Constructs the identifier for the next chapter based on the current chapter's ID and the last chapter number.
     *
     * @param idChapter  The ID of the current chapter in the format 'chuong-X', where X is the chapter number.
     * @param endChapter The number of the last chapter in the series.
     * @return The ID of the next chapter as a String, or null if the current chapter is the last one.
     */
    public static String getValidNextChapter(String idChapter, int endChapter) {
        String[] parts = idChapter.split("-");
        int numberChap = Integer.parseInt(parts[parts.length - 1]);
        if (numberChap < endChapter) return "chuong" + (numberChap + 1);
        return null;
    }

    @Override
    public DataResponse getNovelChapterDetail(String novelId, String chapterId) {
        String urlChapter = NOVEL_MAIN + novelId + "/" + chapterId;
        String urlAuthor = NOVEL_MAIN + novelId;
        Document doc = null;
        try {
            doc = ConnectJsoup.connect(urlAuthor);
            if(doc!=null) {
                String nameAuthor = doc.select("a[itemprop=author]").text();
                Author author = new Author(HandleString.makeSlug(nameAuthor), nameAuthor);
                doc = ConnectJsoup.connect(urlChapter);
                if(doc!=null) {
                    String novelName = doc.select("a[class=truyen-title]").first().text();
                    String chapterName = doc.select("a[class=chapter-title]").first().text();
                    Elements content = doc.select("div#chapter-c");
                    String nextChapterURL = doc.select("a[id=next_chap]").attr("href");
                    String idNextChapter = nextChapterURL.split("/").length != 1 ? nextChapterURL.split("/")[nextChapterURL.split("/").length - 1] : null;
                    String preChapterURL = doc.select("a[id=prev_chap]").attr("href");
                    String idPreChapter = preChapterURL.split("/").length != 1 ? preChapterURL.split("/")[preChapterURL.split("/").length - 1] : null;
                    Chapter chapterDetail = new Chapter().novelId(novelId).novelName(novelName).chapterId(chapterId).nextChapterId(idNextChapter).preChapterId(idPreChapter).name(chapterName).author(author).content(content.toString());
                    DataResponse  dataResponse = new DataResponse().status("success").data(chapterDetail);
                    return dataResponse;
                }
                return DataResponseUtils.getErrorDataResponse("Failed to connect "+urlChapter);
            }
            return DataResponseUtils.getErrorDataResponse("Failed to connect "+urlAuthor);
        } catch (Exception e) {
            return DataResponseUtils.getErrorDataResponse(e.getMessage());
        }
    }

    @Override
    public DataResponse getNovelListChapters(String novelId, int page) {
        String url = NOVEL_MAIN + novelId;
        Document doc = null;
        try {
            doc = ConnectJsoup.connect(url);
            if(doc!=null) {
                String name = doc.select("h3[class=title]").first().text();
                String authorName = doc.select("a[itemprop=author]").first().text();
                Author author = new Author(HandleString.makeSlug(authorName), authorName);
                List<Chapter> chapterList = new ArrayList<>();
                Integer totalPages = getNovelTotalPages(url);
                String link = String.format("https://truyenfull.vn/%s/trang-%d", novelId, page);
                doc = ConnectJsoup.connect(link);
                Elements chapters = doc.select("ul[class=list-chapter] li");
                int endChapter = getChapterEnd(NOVEL_MAIN + novelId);
                for (int i=0;i<chapters.size();i++) {
                    Element chapter = chapters.get(i);
                    String nameChapter = chapter.selectFirst("a").text();
                    String linkChapter = chapter.selectFirst("a").attr("href");
                    String idChapter = linkChapter.split("/")[linkChapter.split("/").length - 1];
                    String idPreChapter = getValidPreChapter(idChapter);
                    String idNextChapter = getValidNextChapter(idChapter, endChapter);
                    Chapter chapterObj = new Chapter(novelId, name, idChapter, idNextChapter, idPreChapter, nameChapter, author, "");
                    chapterList.add(chapterObj);
                }
                DataResponse dataResponse = new DataResponse();
                dataResponse.setStatus("success");
                dataResponse.setData(chapterList);
                dataResponse.setTotalPage(totalPages);
                dataResponse.setPerPage(chapterList.size());
                dataResponse.setCurrentPage(page);
                return dataResponse;
            }
            return DataResponseUtils.getErrorDataResponse("Failed to connect "+url);
        } catch (Exception e) {
            return DataResponseUtils.getErrorDataResponse(e.getMessage());
        }
    }

    @Override
    public DataResponse getNovelListChapters(String novelId, String fromChapterId, int numChapters) {
        String url = NOVEL_MAIN + novelId;
        Document doc = null;
        try {
            doc = ConnectJsoup.connect(url);
            if(doc!=null) {
                String name = doc.select("h3[class=title]").first().text();
                String authorName = doc.select("a[itemprop=author]").first().text();
                Author author = new Author(HandleString.makeSlug(authorName), authorName);
                List<Chapter> chapterList = new ArrayList<>();
                Integer totalPages = getNovelTotalPages(url);
                for (int i = 1; i <= totalPages.intValue(); i++) {
                    String link = String.format("https://truyenfull.vn/%s/trang-%d", novelId, i);
                    doc = ConnectJsoup.connect(link);
                    Elements chapters = doc.select("ul[class=list-chapter] li");
                    int endChapter = getChapterEnd(NOVEL_MAIN + novelId);
                    for (Element chapter : chapters) {
                        String nameChapter = chapter.selectFirst("a").text();
                        String linkChapter = chapter.selectFirst("a").attr("href");
                        String idChapter = linkChapter.split("/")[linkChapter.split("/").length - 1];
                        String idPreChapter = getValidPreChapter(idChapter);
                        String idNextChapter = getValidNextChapter(idChapter, endChapter);
                        Chapter chapterObj = new Chapter(novelId, name, idChapter, idNextChapter, idPreChapter, nameChapter, author, "");
                        chapterList.add(chapterObj);
                    }
                }
                DataResponse dataResponse = new DataResponse();
                dataResponse.setStatus("success");
                dataResponse.setData(chapterList);
                dataResponse.setTotalPage(1);
                dataResponse.setPerPage(chapterList.size());
                return dataResponse;
            }
            return DataResponseUtils.getErrorDataResponse("Failed to connect "+url);
        } catch (Exception e) {
            return DataResponseUtils.getErrorDataResponse(e.getMessage());
        }
    }

    @Override
    public DataResponse getNovelDetail(String novelId) {
        String url = NOVEL_MAIN + novelId;
        Document doc = null;
        try {
            doc = ConnectJsoup.connect(url);
            if(doc!=null) {
                String name = doc.select("h3[class=title]").first().text();
                String authorName = doc.select("a[itemprop=author]").first().text();
                Author author = new Author(HandleString.makeSlug(authorName), authorName);
                String firstChapterURL = doc.select("ul[class=list-chapter] li").get(0).select("a").attr("href");
                String idFirstChapter = firstChapterURL.split("/")[firstChapterURL.split("/").length - 1];
                String image = doc.selectFirst("img").attr("src");
                String description = doc.selectFirst("div[itemprop=description]").toString();
                Novel novel = new Novel(novelId, name, image, description, author, idFirstChapter);
                DataResponse dataResponse = new DataResponse();
                dataResponse.setData(novel);
                dataResponse.setStatus("success");
                return dataResponse;
            }
            return DataResponseUtils.getErrorDataResponse("Failed to connect "+url);
        } catch (Exception e) {
            return DataResponseUtils.getErrorDataResponse(e.getMessage());
        }
    }

    @Override
    public DataResponse getAuthorDetail(String authorId) {
        String url = NOVEL_MAIN + "tac-gia/" + authorId;
        Document doc = null;
        try {
            doc = ConnectJsoup.connect(url);
            if(doc!=null) {
                Elements novels = doc.select("div[itemtype=https://schema.org/Book]");
                String nameAuthor = novels.get(0).selectFirst("span[class=author]").text();
                Author author = new Author(HandleString.makeSlug(nameAuthor), nameAuthor);
                List<Novel> novelList = new ArrayList<>();
                for (int i =0;i<novels.size();i++) {
                    Element novel = novels.get(i);
                    String image = novel.selectFirst("div[data-image]").attr("data-image");
                    String name = novel.selectFirst("h3").text();
                    String link = novel.selectFirst("a").attr("href");
                    String idNovel = getEndSlugFromUrl(link);
                    doc = ConnectJsoup.connect(link);
                    String firstChapterURL = doc.select("ul[class=list-chapter] li").get(0).select("a").attr("href");
                    String idFirstChapter = firstChapterURL.split("/")[firstChapterURL.split("/").length - 1];
                    String description = doc.selectFirst("div[itemprop=description]").toString();
                    Novel novelObj = new Novel(idNovel, name, image, description, author, idFirstChapter);
                    novelList.add(novelObj);
                }
                DataResponse dataResponse = new DataResponse("success", 1, 1, novelList.size(), null, novelList, null);
                return dataResponse;
            }
            return DataResponseUtils.getErrorDataResponse("Failed to connect "+url);
        } catch (Exception e) {
            return DataResponseUtils.getErrorDataResponse(e.getMessage());
        }
    }

    @Override
    public DataResponse getAllNovels(int page, String search) {
        String apiGetAll = String.format(API_TRUYENFULL_SEARCH, "", page);
        try {
            List<Novel> novelList = new ArrayList<>();
            String jsonResponse = getJsonResponse(apiGetAll);
            if(jsonResponse.equals("error"))
            {
                return DataResponseUtils.getErrorDataResponse("Failed to get data from API!");
            }
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray data = jsonObject.getJSONArray("data");
            int totalPage = jsonObject.getJSONObject("meta").getJSONObject("pagination").getInt("total_pages");
            for (int i = 0; i < data.length() && i < 26; i++) {
                JSONObject item = data.getJSONObject(i);
                int id = item.getInt("id");
                String nameAuthor = item.getString("author");
                Author author = new Author(HandleString.makeSlug(nameAuthor), nameAuthor);
                String nameNovel = item.getString("title");
                String idNovel = HandleString.makeSlug(nameNovel);
                String apiURL = API_DETAL_NOVEL + id;
                String idFirstChapter = "chuong-1";
                String response = getJsonResponse(apiURL);
                JSONObject object = new JSONObject(response);
                String description = object.getJSONObject("data").getString("description");
                String image = object.getJSONObject("data").getString("image");
                novelList.add(new Novel(idNovel, nameNovel, image, description, author, idFirstChapter));
            }
            DataResponse dataResponse = new DataResponse("success", totalPage, page, novelList.size(), search, novelList, "");
            dataResponse.setCurrentPage(page);
            return dataResponse;
        } catch (Exception e) {
            return DataResponseUtils.getErrorDataResponse(e.getMessage());
        }
    }

    @Override
    public DataResponse getNovelSearch(int page, String key, String orderBy) {
        String apiGetAll = String.format(API_TRUYENFULL_SEARCH, key, page);
        try {
            List<Novel> novelList = new ArrayList<>();
            String jsonResponse = getJsonResponse(apiGetAll);
            if(jsonResponse.equals("error"))
            {
                return DataResponseUtils.getErrorDataResponse("Failed to get data from API!");
            }
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray data = jsonObject.getJSONArray("data");
            int totalPage = jsonObject.getJSONObject("meta").getJSONObject("pagination").getInt("total_pages");
            for (int i = 0; i < data.length() && i < 26; i++) {
                JSONObject item = data.getJSONObject(i);
                int id = item.getInt("id");
                String nameAuthor = item.getString("author");
                Author author = new Author(HandleString.makeSlug(nameAuthor), nameAuthor);
                String nameNovel = item.getString("title");
                String idNovel = HandleString.makeSlug(nameNovel);
                String apiURL = API_DETAL_NOVEL + id;
                String idFirstChapter = "chuong-1";
                String response = getJsonResponse(apiURL);
                JSONObject object = new JSONObject(response);
                String description = object.getJSONObject("data").getString("description");
                String image = object.getJSONObject("data").getString("image");
                novelList.add(new Novel(idNovel, nameNovel, image, description, author, idFirstChapter));
            }
            DataResponse dataResponse = new DataResponse("success", totalPage, page, novelList.size(), key, novelList, "");
            dataResponse.setCurrentPage(page);
            return dataResponse;
        } catch (Exception e) {
            return DataResponseUtils.getErrorDataResponse(e.getMessage());
        }
    }

    @Override
    public Chapter getContentChapter(String novelId, String chapterId) {
        String urlChapter = NOVEL_MAIN + novelId + "/" + chapterId;
        String urlAuthor = NOVEL_MAIN + novelId;
        Document doc = null;
        try {
            doc = ConnectJsoup.connect(urlAuthor);
            if(doc!=null) {
                String nameAuthor = doc.select("a[itemprop=author]").text();
                Author author = new Author(HandleString.makeSlug(nameAuthor), nameAuthor);
                doc = ConnectJsoup.connect(urlChapter);
                if(doc!=null) {
                    String novelName = doc.select("a[class=truyen-title]").first().text();
                    String chapterName = doc.select("a[class=chapter-title]").first().text();
                    Elements content = doc.select("div#chapter-c");
                    String nextChapterURL = doc.select("a[id=next_chap]").attr("href");
                    String idNextChapter = nextChapterURL.split("/").length != 1 ? nextChapterURL.split("/")[nextChapterURL.split("/").length - 1] : null;
                    String preChapterURL = doc.select("a[id=prev_chap]").attr("href");
                    String idPreChapter = preChapterURL.split("/").length != 1 ? preChapterURL.split("/")[preChapterURL.split("/").length - 1] : null;
                    Chapter chapterDetail = new Chapter(novelId, novelName, chapterId, idNextChapter, idPreChapter, chapterName, author, content.toString());
                    return chapterDetail;
                }
                return null;
            }
            return null;
        } catch (Exception e) {
           return null;
        }
    }
}
