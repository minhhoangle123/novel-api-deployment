package com.crawldata.back_end.controller;

import com.crawldata.back_end.service.ExportServiceImpl;
import com.crawldata.back_end.service.NovelServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PluginController {

    private final NovelServiceImpl novelServiceImpl;
    private final ExportServiceImpl exportServiceImpl;

    @GetMapping("/server")
    public ResponseEntity<?> getListNovelsPlugin()
    {
        List<String> listKeyPlugins = novelServiceImpl.getAllNovelPlugins();
        return ResponseEntity.ok(listKeyPlugins);
    }

    @GetMapping("/file")
    public ResponseEntity<?> getListExportPlugin()
    {
        List<String> listKeyPlugins = exportServiceImpl.getAllExportPlugins();
        return ResponseEntity.ok(listKeyPlugins);
    }
}
