package com.crawldata.back_end.service;

import com.crawldata.back_end.model.novel_plugin;
import com.crawldata.back_end.repository.NovelPluginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class NovelPluginService {
    @Autowired
    private NovelPluginRepository novelPluginRepository;
    public novel_plugin saveFile(MultipartFile file) throws IOException {
       novel_plugin plugin = new novel_plugin();
       //System.out.println(file.getOriginalFilename().split("\\.")[0]);
       plugin.setId(file.getOriginalFilename().split("\\.")[0]);
       plugin.setData(file.getBytes());
       return novelPluginRepository.save(plugin);
    }

    public novel_plugin getPlugin(String id) {
        return novelPluginRepository.findById(id).get();
    }

    public List<novel_plugin> getAllNovelPlugins() {
        return novelPluginRepository.findAll();
    }

    public void deleteNovelPlugin(String id) {
        novelPluginRepository.deleteById(id);
    }

    public void deleteAllPlugins()
    {
        novelPluginRepository.deleteAll();
    }

    public static void main(String[] args) {

    }
}
