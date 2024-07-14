package com.crawldata.back_end.export_plugin_builder;

import com.crawldata.back_end.novel_plugin_builder.PluginFactory;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * This interface defines methods for export detail novel chapter
 */
public interface ExportPluginFactory {

    /**
     * Export novel
     * @param  plugin The plugin of novel
     * @param novelId The ID of the novel.
     * @param fromChapterId the id of first chapter
     * @param numChapters number of chapters to export
     * @param response response for client
     */
    void export(PluginFactory plugin, String novelId, String fromChapterId, int numChapters, HttpServletResponse response) throws  IOException;
}
