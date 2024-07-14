package com.crawldata.back_end.service;

import com.crawldata.back_end.model.export_plugin;
import com.crawldata.back_end.repository.ExportPluginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ExportPluginService {
    @Autowired
    private ExportPluginRepository exportPluginRepository;
    public export_plugin saveFile(MultipartFile file) throws IOException {
        export_plugin plugin = new export_plugin();
        plugin.setId(file.getOriginalFilename().split("\\.")[0]);
        plugin.setData(file.getBytes());
        return exportPluginRepository.save(plugin);
    }

    public List<export_plugin> getPlugins() {
        return exportPluginRepository.findAll();
    }
    public void deletePlugin(String id) {
        exportPluginRepository.deleteById(id);
    }
    public void deleteAllPlugins()
    {
        exportPluginRepository.deleteAll();
    }
}
