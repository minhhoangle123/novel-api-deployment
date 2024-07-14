package com.crawldata.back_end.controller;

import com.crawldata.back_end.model.export_plugin;
import com.crawldata.back_end.model.novel_plugin;
import com.crawldata.back_end.service.ExportPluginService;
import com.crawldata.back_end.service.NovelPluginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/plugin")
public class PluginsController {

    @Autowired
    private NovelPluginService novelSer;

    @Autowired
    private ExportPluginService exportSer;


    @GetMapping("/novel")
    public String novelPluginRender(Model model)
    {
        List<novel_plugin> plugins = novelSer.getAllNovelPlugins();
        model.addAttribute("fieldSideBarName", "novelplugin");
        model.addAttribute("listPlugins", plugins);
        return "novelplugin";
    }

    @GetMapping("/export")
    public String exportPluginRender(Model model)
    {
        List<export_plugin> plugins = exportSer.getPlugins();
        model.addAttribute("fieldSideBarName", "exportplugin");
        model.addAttribute("listPlugins", plugins);
        return "exportplugin";
    }

    @PostMapping("/novel/upload")
    public String uploadNovelPlugin(@RequestParam("file") MultipartFile file) {
        System.out.println("OK");
        try {
            System.out.println(file.isEmpty());
            if(file.isEmpty())
            {
                return "error";
            }
            novelSer.saveFile(file);
            return "redirect:/plugin/novel";
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/novel/delete/{id}")
    public String deleteNovelPlugin(@PathVariable String id) {
        novelSer.deleteNovelPlugin(id);
        return "redirect:/plugin/novel";
    }

    @GetMapping("/novel/deleteAll")
    public String deleteAllNovelPlugin()
    {
        novelSer.deleteAllPlugins();
        return "redirect:/plugin/novel";
    }

    @GetMapping("/export/delete/{id}")
    public String deleteExportPlugin(@PathVariable String id) {
        exportSer.deletePlugin(id);
        return "redirect:/plugin/export";
    }

    @GetMapping("/export/deleteAll")
    public String deleteAllExportPlugins()
    {
        exportSer.deleteAllPlugins();
        return "redirect:/plugin/export";
    }

    @PostMapping("/export/upload")
    public String uploadExportPlugin(@RequestParam("file") MultipartFile file) {
        try {
            if(file.isEmpty())
            {
                return "error";
            }
            exportSer.saveFile(file);
            return "redirect:/plugin/export";
        } catch (IOException e) {
            return "error";
        }
    }
}
