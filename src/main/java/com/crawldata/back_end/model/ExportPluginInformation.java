package com.crawldata.back_end.model;

import com.crawldata.back_end.export_plugin_builder.ExportPluginFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents information about a export plugin
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportPluginInformation {
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
     * The object representing the  export plugin, implementing the {@link com.crawldata.back_end.export_plugin_builder.ExportPluginFactory} interface.
     *
     */
    private ExportPluginFactory exportPluginObject;
}
