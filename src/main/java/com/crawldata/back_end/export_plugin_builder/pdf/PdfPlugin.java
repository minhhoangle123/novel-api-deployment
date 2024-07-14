package com.crawldata.back_end.export_plugin_builder.pdf;
import com.crawldata.back_end.export_plugin_builder.ExportPluginFactory;
import com.crawldata.back_end.model.Chapter;
import com.crawldata.back_end.model.Novel;
import com.crawldata.back_end.novel_plugin_builder.PluginFactory;
import com.crawldata.back_end.response.DataResponse;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.jsoup.Jsoup;
import org.springframework.core.io.ClassPathResource;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PdfPlugin implements ExportPluginFactory {
    @Getter
    private PluginFactory pluginFactory;
    @Getter
    private Novel novel;
    private List<Chapter> chapterList = new ArrayList<>();
    private final int maxThreadNum = 3;
    @Getter
    private List<Chapter> listDetailChapter;
    private List<ReadDataThread> listThreads;

    @Override
    public void export(PluginFactory plugin, String novelId, String fromChapterId, int numChapters, HttpServletResponse response) throws IOException {
        getNovelInfo(plugin, novelId, fromChapterId, numChapters);
        listDetailChapter = new ArrayList<>();
        listThreads = new ArrayList<>();
        try {
            getDetailChapter();
            generatePdfAllChapter(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getDetailChapter() {
        int totalChapters = chapterList.size();
        int chaptersPerThread = totalChapters / maxThreadNum;
        int remainingChapters = totalChapters % maxThreadNum;

        for (int i = 0; i < maxThreadNum; i++) {
            int start = i * chaptersPerThread;
            int end = (i == maxThreadNum - 1) ? start + chaptersPerThread + remainingChapters : start + chaptersPerThread;
            List<Chapter> chapters = chapterList.subList(start, end);
            ReadDataThread thread = new ReadDataThread(chapters, this);
            thread.start();
            listThreads.add(thread);
        }

        try {
            joinThread();
        } catch (Exception e) {
            e.printStackTrace();
        }

        sortChaptersASCByName(listDetailChapter);
    }

    public Double getChapterNumber(String chapterName) {
        String[] components = chapterName.split(":");
        return Double.valueOf(components[0].split(" ")[1].trim());
    }

    public void sortChaptersASCByName(List<Chapter> chapters) {
        Collections.sort(chapters, new Comparator<Chapter>() {
            @Override
            public int compare(Chapter o1, Chapter o2) {
                Double chapter1Number = getChapterNumber(o1.getName());
                Double chapter2Number = getChapterNumber(o2.getName());
                Double delta = chapter1Number - chapter2Number;
                if(delta == 0)
                {
                    return 0;
                }
                else if(delta <0)
                {
                    return -1;
                }
                else {
                    return 1;
                }
            }
        });
    }

    public void joinThread() throws InterruptedException {
        for (ReadDataThread thread : listThreads) {
            thread.join();
        }
    }

    private void getNovelInfo(PluginFactory plugin, String novelId, String fromChapterId, int numChapters) {
        pluginFactory = plugin;
        DataResponse dataResponse = pluginFactory.getNovelDetail(novelId);
        if (dataResponse != null && "success".equals(dataResponse.getStatus())) {
            novel = (Novel) dataResponse.getData();
        }
        dataResponse = pluginFactory.getNovelListChapters(novel.getNovelId(), fromChapterId, numChapters);
        if (dataResponse != null && "success".equals(dataResponse.getStatus())) {
            Object data = dataResponse.getData();
            if (data instanceof List<?>) {
                chapterList = (List<Chapter>) data;
            }
        }
    }

    private void generatePdfAllChapter(HttpServletResponse response) throws DocumentException, IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            BaseFont baseFont = getCustomFont();
            addTitlePage(document, baseFont);
            PdfOutline rootOutline = writer.getRootOutline();

            for (Chapter detailChapter : listDetailChapter) {
                document.newPage();
                addChapter(document, writer, rootOutline, detailChapter, baseFont);
            }

            document.close();

            String filename = getFileName();
            prepareResponse(response, baos, filename);
        }
    }

    private BaseFont getCustomFont() throws DocumentException, IOException {
        ClassPathResource fontResource = new ClassPathResource("fonts/ArialUnicodeMSRegular.ttf");
        FontFactory.register(fontResource.getURI().toString(), "CustomFont");
        return BaseFont.createFont(fontResource.getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    }

    private void addTitlePage(Document document, BaseFont baseFont) throws DocumentException, IOException {
        Font titleFont = new Font(baseFont, 35, Font.BOLD);
        Font authorFont = new Font(baseFont, 25, Font.NORMAL);
        Font normalFont = new Font(baseFont, 12, Font.NORMAL);

        Paragraph novelTitle = new Paragraph(novel.getName(), titleFont);
        novelTitle.setAlignment(Element.ALIGN_CENTER);
        novelTitle.setSpacingAfter(20);
        document.add(novelTitle);

        Paragraph author = new Paragraph("Tác giả: " + novel.getAuthor().getName(), authorFont);
        author.setAlignment(Element.ALIGN_CENTER);
        author.setSpacingAfter(30);
        document.add(author);

        Image coverImage = Image.getInstance(new URL(novel.getImage()));
        coverImage.setAlignment(Image.ALIGN_CENTER);
        coverImage.scaleToFit(400, 400);
        document.add(coverImage);
    }

    private void addChapter(Document document, PdfWriter writer, PdfOutline rootOutline, Chapter detailChapter, BaseFont baseFont) throws DocumentException, IOException {
        Font chapterTitleFont = new Font(baseFont, 28, Font.BOLD);
        Anchor anchor = new Anchor(detailChapter.getName(), chapterTitleFont);
        anchor.setName(detailChapter.getName());
        Paragraph chapterTitle = new Paragraph(anchor);
        chapterTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(chapterTitle);
        document.add(new Paragraph("\n\n"));

        int pageNumber = writer.getPageNumber();
        PdfDestination destination = new PdfDestination(PdfDestination.FITH);
        writer.getDirectContent().localDestination(detailChapter.getName(), destination);
        new PdfOutline(rootOutline, PdfAction.gotoLocalPage(pageNumber, destination, writer), detailChapter.getName());

        StyleSheet styles = new StyleSheet();
        styles.loadTagStyle("body", "face", "CustomFont");
        styles.loadTagStyle("body", "encoding", "Identity-H");
        styles.loadTagStyle("body", "size", "15pt");
        styles.loadTagStyle("p", "style", "text-align: justify;");

        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(detailChapter.getContent());
        String htmlContent = jsoupDoc.body().html();
        List<Element> elements = HTMLWorker.parseToList(new StringReader(htmlContent), styles);
        for (Element element : elements) {
            document.add(element);
        }
    }

    private String getFileName() {
        if (chapterList.isEmpty()) {
            return novel.getName();
        } else if (chapterList.size() == 1) {
            return novel.getName() + "_" + chapterList.get(0).getChapterId();
        } else {
            return novel.getName() + "_" + chapterList.get(0).getChapterId() + "-" + chapterList.get(chapterList.size() - 1).getChapterId();
        }
    }

    private void prepareResponse(HttpServletResponse response, ByteArrayOutputStream baos, String filename) throws IOException {
        response.setContentType("application/pdf");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename=" + new String((filename + ".pdf").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        response.setContentLength(baos.size());

        try (OutputStream os = response.getOutputStream()) {
            baos.writeTo(os);
            os.flush();
        }
    }
}

class ReadDataThread extends Thread {
    private final List<Chapter> listChapter;
    private final PdfPlugin pdf;

    public ReadDataThread(List<Chapter> listChapter, PdfPlugin pdf) {
        this.listChapter = listChapter;
        this.pdf = pdf;
    }

    @Override
    public void run() {
        PluginFactory plugin = pdf.getPluginFactory();
        for (Chapter chapter : listChapter) {
            Chapter contentChapter = plugin.getContentChapter(chapter.getNovelId(), chapter.getChapterId());
            pdf.getListDetailChapter().add(contentChapter);
        }
    }
}
