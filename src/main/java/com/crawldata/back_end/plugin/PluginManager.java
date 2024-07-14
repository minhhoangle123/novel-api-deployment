package com.crawldata.back_end.plugin;

import com.crawldata.back_end.model.PluginInformation;
import com.crawldata.back_end.model.novel_plugin;
import com.crawldata.back_end.repository.NovelPluginRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the lifecycle of plugins in the application.
 */
@Service
@AllArgsConstructor
public class PluginManager {

    private static final String PLUGIN_DIRECTORY = "/novel_plugins";

    private List<PluginInformation> plugins = new ArrayList<>();

    private PluginLoader pluginLoader;

    @Autowired private NovelPluginRepository repo;

    public File createTempFileFromEntity(novel_plugin fileEntity) {

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
     * Updates the list of available plugins by loading plugins from the plugin directory.
     */
    public void updatePlugins()  {
        // Clear existing plugins
        unloadPlugins();
        plugins.clear();
        // Load plugins from JAR files in the directory
        //File[] files = pluginsDir.listFiles();
        List<File> files = new ArrayList<>();

        List<novel_plugin> novelPlugins = repo.findAll();
        for (novel_plugin novelPlugin : novelPlugins) {
            files.add(createTempFileFromEntity(novelPlugin));
        }

        for (File file : files) {
            if (file.getName().endsWith(".jar")) {
                PluginInformation pluginInformation = pluginLoader.loadPluginInformation(file);
                if (pluginInformation != null) {
                    plugins.add(pluginInformation);
                }
            }
        }
    }

    /**
     * Unloads classes of all plugins.
     */
    private void unloadPlugins() {
        pluginLoader.unloadPluginClasses(plugins);
    }

    /**
     * Retrieves plugin information by ID.
     *
     * @param id The ID of the plugin.
     * @return The plugin information, or null if not found.
     */
    public PluginInformation getPluginById(String id) {
        return plugins.stream()
                .filter(plugin -> plugin.getPluginId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Retrieves all available plugins.
     *
     * @return The list of available plugins.
     */
    public List<PluginInformation> getAllPlugins() {
        updatePlugins();
        return new ArrayList<>(plugins);
    }
}
