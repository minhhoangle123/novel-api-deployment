package com.crawldata.back_end.plugin;

import com.crawldata.back_end.export_plugin_builder.ExportPluginFactory;
import com.crawldata.back_end.model.ExportPluginInformation;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.support.HandlerFunctionAdapter;
import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.JclObjectFactory;
import org.xeustechnologies.jcl.JclUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Component
public class ExportPluginLoader {
    private static JarClassLoader jcl = new JarClassLoader();
    private final HandlerFunctionAdapter handlerFunctionAdapter;

    public ExportPluginLoader(HandlerFunctionAdapter handlerFunctionAdapter) {
        this.handlerFunctionAdapter = handlerFunctionAdapter;
    }

    /**
     * Loads a export plugin class from the specified JAR file.
     *
     * @param path      The path to the JAR file containing the plugin class.
     * @param className The fully qualified class name of the plugin.
     * @return An instance of the loaded plugin class.
     * */
    public ExportPluginFactory loadExportPluginClass(String path, String className) {
        jcl.add(path);
        JclObjectFactory factory = JclObjectFactory.getInstance();
        Object obj = factory.create(jcl, className);
        return JclUtils.cast(obj, ExportPluginFactory.class);
    }

    /**
     * Loads export plugin information from the specified JAR file.
     *
     * @param pluginFile The JAR file containing the plugin information.
     * @return The plugin information.
     */
    public ExportPluginInformation loadExportPluginInformation(File pluginFile) {
        ExportPluginInformation exportPluginInfo = JSONToPluginInformationAdapter(pluginFile);
        exportPluginInfo.setExportPluginObject(loadExportPluginClass(pluginFile.getAbsolutePath(), exportPluginInfo.getClassName()));
        return  exportPluginInfo;
    }

    /**
     * Reads plugin information from the "plugin.json" file inside the JAR file.
     *.
     * @param pluginFile The JAR file containing the plugin information.
     */
    public ExportPluginInformation JSONToPluginInformationAdapter(File pluginFile) {
        ExportPluginInformation exportPluginInfo = new ExportPluginInformation();
        try (JarFile jarFile = new JarFile(pluginFile)) {
            JarEntry entry = jarFile.getJarEntry("plugin.json");
            if (entry == null) {
                // Handle error: "plugin.json" not found in the JAR file
                System.err.println("Error: 'plugin.json' not found in the JAR file.");
                return exportPluginInfo;
            }
            try (InputStream inputStream = jarFile.getInputStream(entry)) {
                // Read the JSON content from the input stream
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                String jsonContent = new String(buffer);
                JSONObject js = new JSONObject(jsonContent);
                JSONObject metadata = js.getJSONObject("metadata");
                exportPluginInfo.setPluginId(metadata.getString("id"));
                exportPluginInfo.setName(metadata.getString("name"));
                exportPluginInfo.setClassName(metadata.getString("className"));
            }
        } catch (IOException e) {
            // Handle error: Unable to read "plugin.json" from the JAR file
            e.printStackTrace();
        }
        return exportPluginInfo;
    }

    /**
     * Unloads the all plugin classes.
     *
     */
    public void unloadPluginClasses(List<ExportPluginInformation> plugins) {
        for(ExportPluginInformation plugin : plugins) {
            if(jcl.getLoadedClasses().containsKey(plugin.getClassName())){
                jcl.unloadClass(plugin.getClassName());
            }
        }
        plugins.clear();
        jcl = new JarClassLoader();
        System.gc();
    }
}
