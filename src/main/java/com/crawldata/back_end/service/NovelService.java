package com.crawldata.back_end.service;

import com.crawldata.back_end.novel_plugin_builder.PluginFactory;
import com.crawldata.back_end.response.DataResponse;

import java.util.List;

/**
 * Service interface for managing novels.
 */
public interface NovelService {
     /**
     * Retrieves the plugin factory for the specified plugin ID.
     * @param pluginId The ID of the plugin.
     * @return The plugin factory.
     */
     PluginFactory getPluginFactory(String pluginId);

    /**
     * @return The key novel plugins
     */
    List<String> getAllNovelPlugins();

    /**
     * Retrieves the detail of a specific chapter in a novel.
     *
     * @param pluginId  The ID of the plugin.
     * @param novelId   The ID of the novel.
     * @param chapterId The ID of the chapter.
     * @return The chapter detail.
     */
    DataResponse getNovelChapterDetail(String pluginId, String novelId, String chapterId);

    /**
     * Retrieves a paginated list of chapters for a novel.
     *
     * @param pluginId The ID of the plugin.
     * @param novelId  The ID of the novel.
     * @param page     The page number.
     * @return A paginated list of chapters.
     */
    DataResponse getNovelListChapters(String pluginId, String novelId, int page);

    /**
     * Retrieves the detail of a novel.
     *
     * @param pluginId The ID of the plugin.
     * @param novelId  The ID of the novel.
     * @return The novel detail.
     */
    DataResponse getNovelDetail(String pluginId, String novelId);

    /**
     * Retrieves novels authored by a specific author.
     *
     * @param pluginId The ID of the plugin.
     * @param authorId The ID of the author.
     * @return A list of novels authored by the author.
     */
    DataResponse getDetailAuthor(String pluginId, String authorId);

    /**
     * Retrieves all novels based on search criteria and pagination.
     *
     * @param pluginId The ID of the plugin.
     * @param page     The page number.
     * @param search   The search criteria.
     * @return A list of novels matching the search criteria.
     */
    DataResponse getAllNovels(String pluginId, int page, String search);
    /**
     * Retrieves all novels based on search criteria and pagination.
     *
     * @param pluginId The ID of the plugin.
     * @param page     The page number.
     * @param key   The search criteria.
     * @param orderBy   The search criteria.
     * @return A list of novels matching the search criteria.
     */
    DataResponse getSearchedNovels(String pluginId, int page, String key, String orderBy);

    /**
     * Retrieves all novels based on search criteria and pagination.
     *
     * @param pluginId The ID of the plugin.
     * @param novelId     The id novel.
     * @param chapterId  The chapter Id.
     * @param response   The response to client.
     * @return file to export for client
     */
    //void exportPDF(String pluginId, String novelId, String chapterId,  HttpServletResponse response);




}
