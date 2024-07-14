package com.crawldata.back_end.model;

import com.crawldata.back_end.novel_plugin_builder.PluginFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents information about a plugin.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginInformation {

    /**
     * The unique identifier of the plugin.
     */
    private String pluginId;

    /**
     * The name of the plugin.
     */
    private String name;

    /**
     * The URL where the plugin is hosted or can be accessed.
     */
    private String url;

    /**
     * The fully qualified class name of the plugin.
     */
    private String className;

    /**
     * The object representing the plugin, implementing the {@link PluginFactory} interface.
     */
    private PluginFactory pluginObject;
}
