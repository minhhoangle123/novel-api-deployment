package com.crawldata.back_end.service;

import com.crawldata.back_end.export_plugin_builder.ExportPluginFactory;
import com.crawldata.back_end.model.Chapter;
import com.crawldata.back_end.model.ExportPluginInformation;
import com.crawldata.back_end.plugin.ExportPluginManager;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ExportServiceImpl implements  ExportService{

    private ExportPluginManager exportPluginManager;
    private final String createPluginErrorMessage = "Error when creating plugin";
    private final NovelService novelService;
    @Override
    public ExportPluginFactory getExportPluginFactory(String pluginId) {
        exportPluginManager.updateExportPlugins();
        return exportPluginManager.getExportPluginById(pluginId).getExportPluginObject();
    }

    @Override
    public void export(String fileType, String pluginId, String novelId,String fromChapterId, int numChapters, HttpServletResponse response) throws IOException {
        ExportPluginFactory exportPluginFactory = getExportPluginFactory(fileType);
        if(exportPluginFactory == null) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        } else {
            exportPluginFactory.export(novelService.getPluginFactory(pluginId), novelId, fromChapterId, numChapters, response);
        }
    }

    @Override
    public List<String> getAllExportPlugins() {
        ArrayList<String> keyExportPlugins = new ArrayList<>();
        List<ExportPluginInformation> listPlugins = exportPluginManager.getAllExportPlugins();
        listPlugins.forEach(plugin -> {
            keyExportPlugins.add(plugin.getPluginId());
        });
        return keyExportPlugins;
    }
}
