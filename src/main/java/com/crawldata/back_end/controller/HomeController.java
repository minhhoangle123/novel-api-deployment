package com.crawldata.back_end.controller;

import com.crawldata.back_end.response.RootResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
public class HomeController {
    @GetMapping("/")
    public RootResponse getApiLinks() throws Exception {
        RootResponse res = new RootResponse("success");
        res.add(linkTo(methodOn(PluginController.class).getListNovelsPlugin()).withRel("Get all novel plugin's id available in the system"));
        res.add(linkTo(methodOn(PluginController.class).getListExportPlugin()).withRel("Get all export plugin's id available in the system"));

        res.add(linkTo(methodOn(NovelController.class).getAllNovels("truyenfull", 1, "")).withRel("Example links to get novels per page"));
        res.add(linkTo(methodOn(NovelController.class).getListChapterPerPage("truyenfull", "nu-hon-hoa-hong", "1")).withRel("Example links to get novel's chapters per page"));
        res.add(linkTo(methodOn(NovelController.class).getNovelsAuthor("truyenfull", "kim-dang")).withRel("Example links to get author's novels"));
        res.add(linkTo(methodOn(NovelController.class).getDetailNovel("truyenfull", "nu-hon-hoa-hong")).withRel("Example links to get detail novel"));
        res.add(linkTo(methodOn(NovelController.class).getContents("truyenfull", "nu-hon-hoa-hong", "chuong-1")).withRel("Example links to get a detail novel's chapter"));

        res.add(linkTo(methodOn(NovelController.class).getAllNovels("tangthuvien", 1, "")).withRel("Example links to get novels per page"));
        res.add(linkTo(methodOn(NovelController.class).getListChapterPerPage("tangthuvien", "kiem-lai", "1")).withRel("Example links to get novel's chapters per page"));
        res.add(linkTo(methodOn(NovelController.class).getNovelsAuthor("tangthuvien", "483")).withRel("Example links to get author's novels"));
        res.add(linkTo(methodOn(NovelController.class).getDetailNovel("tangthuvien", "kiem-lai")).withRel("Example links to get detail novel"));
        res.add(linkTo(methodOn(NovelController.class).getContents("tangthuvien", "kiem-lai", "chuong-1")).withRel("Example links to get a detail novel's chapter"));

        res.add(linkTo(methodOn(NovelController.class).getAllNovels("metruyencv", 1, "")).withRel("Example links to get novels per page"));
        res.add(linkTo(methodOn(NovelController.class).getListChapterPerPage("metruyencv", "linh-chu-tu-khai-thac-ky-si-bat-dau", "1")).withRel("Example links to get novel's chapters per page"));
        res.add(linkTo(methodOn(NovelController.class).getNovelsAuthor("metruyencv", "11325-khoai-nhac-tuu-hanh-ky-tha-vo-so-vi")).withRel("Example links to get author's novels"));
        res.add(linkTo(methodOn(NovelController.class).getDetailNovel("metruyencv", "linh-chu-tu-khai-thac-ky-si-bat-dau")).withRel("Example links to get detail novel"));
        res.add(linkTo(methodOn(NovelController.class).getContents("metruyencv", "linh-chu-tu-khai-thac-ky-si-bat-dau", "chuong-1")).withRel("Example links to get a detail novel's chapter"));

        res.add(linkTo(methodOn(NovelController.class).getAllNovels("lightnovel", 1, "")).withRel("Example links to get novels per page"));
        res.add(linkTo(methodOn(NovelController.class).getListChapterPerPage("lightnovel", "van-co-than-de", "1")).withRel("Example links to get novel's chapters per page"));
        res.add(linkTo(methodOn(NovelController.class).getNovelsAuthor("lightnovel", "karasuma-ei-48699")).withRel("Example links to get author's novels"));
        res.add(linkTo(methodOn(NovelController.class).getDetailNovel("lightnovel", "van-co-than-de")).withRel("Example links to get detail novel"));
        res.add(linkTo(methodOn(NovelController.class).getContents("lightnovel", "van-co-than-de", "chuong-1")).withRel("Example links to get a detail novel's chapter"));

        res.add((linkTo(methodOn(NovelController.class).export("tangthuvien", "pdf", "kiem-lai", "chuong-1", 2, null))).withRel("Example links to downloads novel into file pdf"));
        res.add((linkTo(methodOn(NovelController.class).export("tangthuvien", "audio", "kiem-lai", "chuong-1", 10, null))).withRel("Example links to downloads novel into file pdf"));
        res.add((linkTo(methodOn(NovelController.class).export("tangthuvien", "epub", "kiem-lai", "chuong-1", 1, null))).withRel("Example links to downloads novel into file pdf"));

        //res.add((linkTo(methodOn(PluginsController.class).novelPluginRender(null))).withRel("Novel plugin management"));
        //res.add((linkTo(methodOn(PluginsController.class).exportPluginRender(null))).withRel("Export plugin management"));
        return res;
    }
}
