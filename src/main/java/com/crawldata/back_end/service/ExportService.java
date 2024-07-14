package com.crawldata.back_end.service;

import com.crawldata.back_end.export_plugin_builder.ExportPluginFactory;
import com.crawldata.back_end.model.Chapter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;


/**
 * Service interface for managing export plugin
 */
public interface ExportService {
    /**
     * Retrieves the plugin factory for the specified plugin ID.
     *
     * @param pluginId The ID of the plugin.
     * @return The plugin factory.
     */
    ExportPluginFactory getExportPluginFactory(String pluginId);

    /**
     * Export novel
     * @param fileType the type of file to export
     * @param pluginId The ID of the novel.
     * @param novelId The ID of the novel.
     * @param numChapters number of chapters to export
     * @param response response for client
     */
    public void export(String fileType, String pluginId, String novelId, String fromChapterId, int numChapters, HttpServletResponse response) throws IOException;

     /**
     * @return The key novel plugins
     */
    List<String> getAllExportPlugins();
}
