package com.crawldata.back_end.plugin;

import com.crawldata.back_end.model.PluginInformation;
import com.crawldata.back_end.novel_plugin_builder.PluginFactory;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.JclObjectFactory;
import org.xeustechnologies.jcl.JclUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Loads plugin classes and retrieves plugin information from JAR files.
 */
@Component
public class PluginLoader {

    private JarClassLoader jcl = new JarClassLoader();

    /**
     * Loads a plugin class from the specified JAR file.
     *
     * @param path      The path to the JAR file containing the plugin class.
     * @param className The fully qualified class name of the plugin.
     * @return An instance of the loaded plugin class.
     */
    public PluginFactory loadPluginClass(String path, String className) {
        jcl.add(path);
        JclObjectFactory factory = JclObjectFactory.getInstance();
        Object obj = factory.create(jcl, className);
        return JclUtils.cast(obj, PluginFactory.class);
    }

    /**
     * Loads plugin information from the specified JAR file.
     *
     * @param pluginFile The JAR file containing the plugin information.
     * @return The plugin information.
     */
    public PluginInformation loadPluginInformation(File pluginFile) {
        PluginInformation pluginInfo = JSONToPluginInformationAdapter(pluginFile);
        System.out.println(pluginFile.getAbsolutePath());
        pluginInfo.setPluginObject(loadPluginClass(pluginFile.getAbsolutePath(), pluginInfo.getClassName() ));
        return pluginInfo;
    }

    /**
     * Reads plugin information from the "plugin.json" file inside the JAR file.
     *
     * @param pluginFile The JAR file containing the plugin information.
     */


    public PluginInformation JSONToPluginInformationAdapter(File pluginFile) {
        PluginInformation pluginInfo = new PluginInformation();
        try (JarFile jarFile = new JarFile(pluginFile)) {
            JarEntry entry = jarFile.getJarEntry("plugin.json");
            if (entry == null) {
                // Handle error: "plugin.json" not found in the JAR file
                System.err.println("Error: 'plugin.json' not found in the JAR file.");
                return pluginInfo ;
            }
            try (InputStream inputStream = jarFile.getInputStream(entry)) {
                // Read the JSON content from the input stream
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                String jsonContent = new String(buffer);

                JSONObject js = new JSONObject(jsonContent);
                JSONObject metadata = js.getJSONObject("metadata");
                pluginInfo.setPluginId(metadata.getString("id"));
                pluginInfo.setName(metadata.getString("name"));
                pluginInfo.setUrl(metadata.getString("url"));
                pluginInfo.setClassName(metadata.getString("className"));
            }
        } catch (IOException e) {
            // Handle error: Unable to read "plugin.json" from the JAR file
            e.printStackTrace();
        }
        return pluginInfo;
    }

    /**
     * Unloads the all plugin classes.
     *
     */
    public void unloadPluginClasses(List<PluginInformation> plugins) {
        for(PluginInformation plugin : plugins) {
            if(jcl.getLoadedClasses().containsKey(plugin.getClassName())){
                jcl.unloadClass(plugin.getClassName());
            }
        }
        plugins.clear();
        jcl = new JarClassLoader();
        System.gc();
    }
}
