package com.crawldata.back_end.plugin;

import com.crawldata.back_end.model.ExportPluginInformation;
import com.crawldata.back_end.model.export_plugin;
import com.crawldata.back_end.repository.ExportPluginRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ExportPluginManager {
    private static final String EXPORT_PLUGIN_DIRECTORY = "/export_plugins";

    private List<ExportPluginInformation> exportPluginsInfo = new ArrayList<>();

    private ExportPluginLoader exportPluginLoader;

    @Autowired
    private ExportPluginRepository exportPluginRepository;

    public File createTempFileFromEntity(export_plugin fileEntity) {

        // Create a temporary file
        try {
            System.out.println(fileEntity.getId());
            File tempFile = File.createTempFile(fileEntity.getId(), ".jar");

            // Write the data to the temporary file
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(fileEntity.getData());
            }
            return tempFile;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Updates the list of available export plugins by loading export plugins from the plugin directory.
     */
    public void updateExportPlugins() {

        // Clear existing plugins
        unloadExportPlugins();
        exportPluginsInfo.clear();

        // Load plugins from JAR files in the directory
        //File[] files = pluginsDir.listFiles();
        List<File> files = new ArrayList<>();

        List<export_plugin> exportPlugins = exportPluginRepository.findAll();
        for (export_plugin exportPlugin : exportPlugins) {
            files.add(createTempFileFromEntity(exportPlugin));
        }
        // Load plugins from JAR files in the directory
        for (File file : files) {
            if (file.getName().endsWith(".jar")) {
                ExportPluginInformation exportPluginInformation = exportPluginLoader.loadExportPluginInformation(file);
                if (exportPluginInformation != null) {
                    exportPluginsInfo.add(exportPluginInformation);
                }
            }
        }
    }

    /**
     * Unloads classes of all export plugins.
     */
    private void unloadExportPlugins() {
        exportPluginLoader.unloadPluginClasses(exportPluginsInfo);
    }

    /**
     * Retrieves export plugin information by ID.
     * @param id The ID of the plugin.
     * @return The export plugin information, or null if not found.
     */
    public ExportPluginInformation getExportPluginById(String id) {
        return exportPluginsInfo.stream()
                .filter(plugin -> plugin.getPluginId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Retrieves all available export plugins.
     *
     * @return The list of available export plugins.
     */
    public List<ExportPluginInformation> getAllExportPlugins() {
        updateExportPlugins();
        return new ArrayList<>(exportPluginsInfo);
    }
}
