package com.crawldata.back_end.service;

import com.crawldata.back_end.model.PluginInformation;
import com.crawldata.back_end.plugin.PluginManager;
import com.crawldata.back_end.novel_plugin_builder.PluginFactory;
import com.crawldata.back_end.response.DataResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class NovelServiceImpl implements NovelService{
    private PluginManager pluginManager;
    private final  String getPluginErrorMessage = "This server does not exist";

    @Override
    public PluginFactory getPluginFactory(String pluginId) {
        pluginManager.updatePlugins();
        PluginInformation pluginInformation = pluginManager.getPluginById(pluginId);
        if(pluginInformation == null) {
            return null;
        } else {
            return pluginInformation.getPluginObject();
        }
    }

    @Override
    public List<String> getAllNovelPlugins() {
        ArrayList<String> keyNovelPlugins = new ArrayList<>();
        List<PluginInformation> listPlugins = pluginManager.getAllPlugins();
        listPlugins.forEach(plugin -> {
            keyNovelPlugins.add(plugin.getPluginId());
        });
        return keyNovelPlugins;
    }

    @Override
    public DataResponse getNovelChapterDetail(String pluginId, String novelId, String chapterId) {
        PluginFactory pluginFactory = getPluginFactory(pluginId);
        if(pluginFactory == null) {
            return new DataResponse().status("error").message(getPluginErrorMessage);
        } else {
            return pluginFactory.getNovelChapterDetail(novelId, chapterId);
        }
    }

    @Override
    public DataResponse getNovelListChapters(String pluginId, String novelId, int page) {
        PluginFactory pluginFactory = getPluginFactory(pluginId);
        if(pluginFactory == null) {
            return new DataResponse().status("error").message(getPluginErrorMessage);
        } else {
            return pluginFactory.getNovelListChapters(novelId, page);
        }
    }

    @Override
    public DataResponse getNovelDetail(String pluginId, String novelId) {
        PluginFactory pluginFactory = getPluginFactory(pluginId);
        if(pluginFactory == null) {
            return new DataResponse().status("error").message(getPluginErrorMessage);
        } else {
            return pluginFactory.getNovelDetail(novelId);
        }
    }

    @Override
    public DataResponse getDetailAuthor(String pluginId, String authorId) {
        PluginFactory pluginFactory = getPluginFactory(pluginId);
        if(pluginFactory == null) {
            return new DataResponse().status("error").message(getPluginErrorMessage);
        } else {
            return pluginFactory.getAuthorDetail(authorId);
        }
    }

    @Override
    public DataResponse getAllNovels(String pluginId, int page, String search) {
        PluginFactory pluginFactory = getPluginFactory(pluginId);
        if (pluginFactory == null) {
            return new DataResponse().status("error").message(getPluginErrorMessage);
        } else {
            return pluginFactory.getAllNovels(page, search);
        }
    }

    @Override
    public DataResponse getSearchedNovels(String pluginId, int page, String key, String orderBy) {
        PluginFactory pluginFactory = getPluginFactory(pluginId);
        if (pluginFactory == null) {
            return new DataResponse().status("error").message(getPluginErrorMessage);
        } else {
            return pluginFactory.getNovelSearch(page, key,orderBy);
        }
    }
}
