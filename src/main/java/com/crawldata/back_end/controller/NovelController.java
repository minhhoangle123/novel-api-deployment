package com.crawldata.back_end.controller;

import com.crawldata.back_end.export_plugin_builder.epub.EpubPlugin;
import com.crawldata.back_end.export_plugin_builder.pdf.PdfPlugin;
import com.crawldata.back_end.novel_plugin_builder.PluginFactory;
import com.crawldata.back_end.response.DataResponse;
import com.crawldata.back_end.service.ExportServiceImpl;
import com.crawldata.back_end.service.NovelServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.build.Plugin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class NovelController {

    private final NovelServiceImpl novelServiceImpl;
    private final ExportServiceImpl exportServiceImpl;

    //Get detail chapter
    @GetMapping("{pluginId}/truyen/{novelId}/{chapterId}")
    public ResponseEntity<?> getContents(@PathVariable String pluginId, @PathVariable("novelId") String novelId, @PathVariable("chapterId") String chapterId) {
        DataResponse dataResponse = novelServiceImpl.getNovelChapterDetail(pluginId, novelId, chapterId);
        return ResponseEntity.ok(dataResponse);
    }

    @GetMapping("{pluginId}/truyen/{novelId}/chapters")
    public DataResponse getListChapterPerPage(@PathVariable String pluginId, @PathVariable("novelId") String id, @RequestParam(name =  "page", defaultValue = "1") String page) throws NumberFormatException {
        return novelServiceImpl.getNovelListChapters(pluginId, id, Integer.parseInt(page));
    }

    //get detail novel
    @GetMapping("{pluginId}/truyen/{novelId}")
    public ResponseEntity<?> getDetailNovel(@PathVariable("pluginId") String pluginId, @PathVariable("novelId") String novelId) {
        DataResponse dataResponse = novelServiceImpl.getNovelDetail(pluginId, novelId);
        return ResponseEntity.ok(dataResponse);
    }

    //get list novel of an author
    @GetMapping("{pluginId}/tac-gia/{authorId}")
    public ResponseEntity<?> getNovelsAuthor(@PathVariable("pluginId") String pluginId, @PathVariable("authorId") String authorId)
    {
        DataResponse dataResponse = novelServiceImpl.getDetailAuthor(pluginId, authorId);
        return ResponseEntity.ok(dataResponse);
    }

    //get all novels
    @GetMapping("{pluginId}/ds-truyen")
    public ResponseEntity<?> getAllNovels(@PathVariable("pluginId") String pluginId, @RequestParam(value = "page",defaultValue = "1") int page, @RequestParam(value = "search",defaultValue = "%22") String search) {
        DataResponse dataResponse = novelServiceImpl.getAllNovels(pluginId, page, search);
        return ResponseEntity.ok(dataResponse);
    }

    //find author by name novel or author
    @GetMapping("{pluginId}/tim-kiem")
    public ResponseEntity<?> findNovels(@PathVariable("pluginId") String pluginId, @RequestParam(value = "page",defaultValue = "1") int page,@RequestParam(value = "key",defaultValue = "%22") String key, @RequestParam(name = "orderBy" ,defaultValue = "a-z") String orderBy) {
        DataResponse dataResponse = novelServiceImpl.getSearchedNovels(pluginId,page,key, orderBy);
        return ResponseEntity.ok(dataResponse);
    }

    @GetMapping("{pluginId}/tai-truyen/{novelId}/{fileType}")
    public ResponseEntity<?> export(@PathVariable("pluginId") String pluginId , @PathVariable(name = "fileType") String fileType,@PathVariable(name = "novelId") String novelId, @RequestParam(name = "fromChapterId", defaultValue = "") String fromChapterId, @RequestParam(name = "numChapters" , defaultValue = "0") int numChapters,
                       HttpServletResponse response) throws IOException {
        exportServiceImpl.export(fileType, pluginId, novelId, fromChapterId, numChapters, response);
        return ResponseEntity.ok().build();
    }
}
