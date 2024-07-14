package com.crawldata.back_end.export_plugin_builder.epub;
import com.crawldata.back_end.export_plugin_builder.ExportPluginFactory;
import com.crawldata.back_end.model.Chapter;
import com.crawldata.back_end.model.Novel;
import com.crawldata.back_end.novel_plugin_builder.PluginFactory;
import com.crawldata.back_end.response.DataResponse;
import com.crawldata.back_end.utils.AppUtils;
import com.crawldata.back_end.utils.FileUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@NoArgsConstructor
public class EpubPlugin implements ExportPluginFactory {
    private PluginFactory pluginFactory;
    private Novel novel;
    private List<Chapter> chapterList;
    @Override
    public void export(PluginFactory plugin, String novelId,String fromChapterId, int numChapters, HttpServletResponse response) throws IOException {
        //read untitled.epub to use it as template.
        String epubJarFilePath = AppUtils.curDir + "/export_plugins/epub.jar";
        try (JarFile jarFile = new JarFile(epubJarFilePath)) {
            JarEntry entry = jarFile.getJarEntry("untitled.epub");
            if (entry == null) {
                System.err.println("Error: 'untitled.epub' not found in the JAR file.");
                return;
            }
            getNovelInfo(plugin, novelId, fromChapterId, numChapters);
            String firstIndex = chapterList.get(0).getChapterId().split("-")[1];
            String lastIndex = chapterList.get(chapterList.size()-1).getChapterId().split("-")[1];
            try (InputStream inputStream = jarFile.getInputStream(entry)) {
                String fileName = novel.getName() + " - " + novel.getAuthor().getName() + " (" + firstIndex + "-" + lastIndex + ").epub";
                fileName = fileName.replaceAll("[:/\\?\\*]", "");
                fileName = FileUtils.validate(AppUtils.curDir + "/out/" + fileName);
                FileUtils.byte2file(FileUtils.stream2byte(inputStream), fileName);
                modifyEpubFile(fileName);
                sendFileToClientAndDelete(fileName, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves and sets the novel information and chapter list from the plugin.
     * Finds the chapter with the specified fromChapterId and slices the chapter list
     * to contain the required number of chapters starting from that chapter.
     *
     * @param plugin The plugin factory instance.
     * @param novelId The ID of the novel to retrieve information for.
     * @param fromChapterId The ID of the chapter to start from.
     * @param numChapters The number of chapters to include in the sliced list.
     */
    private void getNovelInfo(PluginFactory plugin, String novelId, String fromChapterId, int numChapters) {
        pluginFactory = plugin;

        // Retrieve novel details
        DataResponse dataResponse = pluginFactory.getNovelDetail(novelId);
        if (dataResponse != null && "success".equals(dataResponse.getStatus())) {
            novel = (Novel) dataResponse.getData();
        }

        // Retrieve chapter list
        dataResponse = pluginFactory.getNovelListChapters(novel.getNovelId(), fromChapterId, numChapters);
        if (dataResponse != null && "success".equals(dataResponse.getStatus())) {
            Object data = dataResponse.getData();
            if (data instanceof List<?> dataList) {
                if (!dataList.isEmpty() && dataList.get(0) instanceof Chapter) {
                    chapterList = (List<Chapter>) dataList;
                }
            }
        }
    }


    /**
     * Modifies the EPUB file based on the novel and chapter information.
     * @param epubFilePath The file path of the EPUB to modify.
     * @throws IOException If an I/O error occurs.
     */
    private void modifyEpubFile(String epubFilePath) throws IOException {
        File tempFile = File.createTempFile("epub", ".epub");
        List<Map.Entry<String, byte[]>> modifiedChapters = Collections.synchronizedList(new ArrayList<>());

        try (ZipFile zipFile = new ZipFile(epubFilePath);
             ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempFile))) {

            // Create a pool of threads to handle chapter modifications
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<Future<Map.Entry<String, byte[]>>> futures = new ArrayList<>();

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                try (InputStream is = zipFile.getInputStream(entry)) {
                    switch (entry.getName()) {
                        case "OEBPS/chap01.xhtml":
                            // For each chapter, create a task to modify and save it
                            for (Chapter chapter : chapterList) {
                                InputStream chapterIs = zipFile.getInputStream(entry);
                                Callable<Map.Entry<String, byte[]>> task = () -> modifyAndSaveChapterXhtml(chapterIs, chapter);
                                futures.add(executorService.submit(task));
                            }
                            break;
                        case "OEBPS/cover.xhtml":
                            modifyCoverXhtml(zos, entry, is);
                            break;
                        case "OEBPS/images/epublogo.png":
                            modifyEpubLogo(zos, entry);
                            break;
                        case "OEBPS/title_page.xhtml":
                            modifyTitlePageXhtml(zos, entry, is);
                            break;
                        case "OEBPS/toc.ncx":
                            modifyTocNcx(zos, entry, is);
                            break;
                        case "OEBPS/toc.xhtml":
                            modifyTocXhtml(zos, entry, is);
                            break;
                        case "OEBPS/content.opf":
                            modifyContentOpf(zos, entry, is);
                            break;
                        default:
                            // Copy other entries without modification
                            zos.putNextEntry(new ZipEntry(entry.getName()));
                            is.transferTo(zos);
                            break;
                    }
                }
                zos.closeEntry();
            }

            // Collect results from all tasks
            for (Future<Map.Entry<String, byte[]>> future : futures) {
                try {
                    modifiedChapters.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }

            executorService.shutdown();

            // Write modified chapters to the ZipOutputStream
            for (Map.Entry<String, byte[]> entry : modifiedChapters) {
                zos.putNextEntry(new ZipEntry(entry.getKey()));
                zos.write(entry.getValue());
                zos.closeEntry();
            }
        }

        // Replace the original EPUB file with the modified one
        File originalFile = new File(epubFilePath);
        if (!originalFile.delete() || !tempFile.renameTo(originalFile)) {
            throw new IOException("Failed to replace the original EPUB file with the modified one.");
        }
        System.out.println("Export " + novel.getName() + " - " + novel.getAuthor().getName() + ".epub success");
    }

    /**
     * Modifies the cover.xhtml file in the EPUB.
     * @param zos The ZipOutputStream to write the modified file to.
     * @param entry The original ZipEntry of the file.
     * @param is The InputStream of the original file content.
     * @throws IOException If an I/O error occurs.
     */
    private void modifyCoverXhtml(ZipOutputStream zos, ZipEntry entry, InputStream is) throws IOException {
        Document document = Jsoup.parse(is, "UTF-8", "", Parser.xmlParser());

        Element titleElement = document.getElementsByTag("h1").first();
        if (titleElement != null) {
            titleElement.text(novel.getName());
        }

        Element authorElement = document.getElementsByTag("h3").first();
        if (authorElement != null) {
            authorElement.text(novel.getAuthor().getName());
        }
        zos.putNextEntry(new ZipEntry(entry.getName()));
        zos.write(document.outerHtml().getBytes());
    }

    /**
     * Replaces the epublogo.png image in the EPUB with a new one.
     * @param zos The ZipOutputStream to write the modified file to.
     * @param entry The original ZipEntry of the file.
     * @throws IOException If an I/O error occurs.
     */
    private void modifyEpubLogo(ZipOutputStream zos, ZipEntry entry) throws IOException {
        // Use the image URL from the novel object
        String imageUrl = novel.getImage();
        byte[] newImage = downloadImage(imageUrl);
        zos.putNextEntry(new ZipEntry(entry.getName()));
        zos.write(newImage);
    }

    /**
     * Downloads an image from the specified URL.
     * @param imageUrl The URL of the image to download.
     * @return A byte array containing the image data.
     * @throws IOException If an I/O error occurs.
     */
    private byte[] downloadImage(String imageUrl) throws IOException {
        try (InputStream in = new URL(imageUrl).openStream()) {
            return in.readAllBytes();
        }
    }

    /**
     * Modifies the title_page.xhtml file in the EPUB.
     * @param zos The ZipOutputStream to write the modified file to.
     * @param entry The original ZipEntry of the file.
     * @param is The InputStream of the original file content.
     * @throws IOException If an I/O error occurs.
     */
    private void modifyTitlePageXhtml(ZipOutputStream zos, ZipEntry entry, InputStream is) throws IOException {
        Document document = Jsoup.parse(is, "UTF-8", "", Parser.xmlParser());
        Element titleElement = document.getElementsByTag("h1").first();
        if (titleElement != null) {
            titleElement.text(novel.getName());
        }

        Element authorElement = document.getElementsByTag("h2").first();
        if (authorElement != null) {
            authorElement.text(novel.getAuthor().getName());
        }
        zos.putNextEntry(new ZipEntry(entry.getName()));
        zos.write(document.outerHtml().getBytes());
    }

    /**
     * Modifies the toc.ncx file in the EPUB.
     * @param zos The ZipOutputStream to write the modified file to.
     * @param entry The original ZipEntry of the file.
     * @param is The InputStream of the original file content.
     * @throws IOException If an I/O error occurs.
     */
    private void modifyTocNcx(ZipOutputStream zos, ZipEntry entry, InputStream is) throws IOException {
        try {
            // Create a DocumentBuilder to parse XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document document = builder.parse(new InputSource(is));

            // Find and modify the title element
            NodeList titleNodes = document.getElementsByTagName("docTitle");
            if (titleNodes.getLength() > 0) {
                org.w3c.dom.Element titleElement = (org.w3c.dom.Element) titleNodes.item(0);
                Node textNode = titleElement.getElementsByTagName("text").item(0);
                if (textNode != null) {
                    textNode.setTextContent(novel.getName() + " - " + novel.getAuthor().getName());
                }
            }

            // Find the navMap element and add new chapters to it
            NodeList navMapNodes = document.getElementsByTagName("navMap");
            if (navMapNodes.getLength() > 0) {
                org.w3c.dom.Element navMapElement = (org.w3c.dom.Element) navMapNodes.item(0);
                int index = navMapElement.getElementsByTagName("navPoint").getLength() + 1;
                for (Chapter chapter : chapterList) {
                    org.w3c.dom.Element navPointElement = document.createElement("navPoint");
                    navPointElement.setAttribute("id", chapter.getChapterId().trim());
                    navPointElement.setAttribute("playOrder", String.valueOf(index++));

                    org.w3c.dom.Element navLabelElement = document.createElement("navLabel");
                    org.w3c.dom.Element textElement = document.createElement("text");
                    textElement.setTextContent(chapter.getName().trim());

                    org.w3c.dom.Element contentElement = document.createElement("content");
                    contentElement.setAttribute("src", "text/"+chapter.getChapterId().trim() + ".xhtml");

                    navLabelElement.appendChild(textElement);
                    navPointElement.appendChild(navLabelElement);
                    navPointElement.appendChild(contentElement);
                    navMapElement.appendChild(navPointElement);
                }
            }

            // Write the modified content back to the ZIP output stream
            zos.putNextEntry(new ZipEntry(entry.getName()));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(zos);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
        } catch (ParserConfigurationException | SAXException | TransformerException e) {
            throw new IOException("Failed to modify toc.ncx", e);
        }
    }

    /**
     * Modifies the toc.xhtml file in the EPUB.
     * @param zos The ZipOutputStream to write the modified file to.
     * @param entry The original ZipEntry of the file.
     * @param is The InputStream of the original file content.
     * @throws IOException If an I/O error occurs.
     */
    private void modifyTocXhtml(ZipOutputStream zos, ZipEntry entry, InputStream is) throws IOException {
        Document document = Jsoup.parse(is, "UTF-8", "", Parser.xmlParser());
        Element divElement = document.getElementsByTag("div").first();
        List<String> defaultElement = Arrays.asList("cover.xhtml", "title_page.xhtml", "copyright.xhtml", "toc.xhtml");
        List<String> defaultText = Arrays.asList("Cover", "Title Page", "Copyright", "Mục lục");
        if (divElement != null) {
            for(int i = 0; i<defaultElement.size(); i++) {
                Element aElement = document.createElement("a");
                aElement.attr("href", defaultElement.get(i));
                aElement.text(defaultText.get(i));
                divElement.appendChild(new Element("p").appendChild(aElement));
            }

            for(Chapter chapter : chapterList) {
                Element aElement = document.createElement("a");
                aElement.attr("href", "text/"+chapter.getChapterId().trim().trim()+".xhtml");
                aElement.text(chapter.getName().trim());
                divElement.appendChild(new Element("p").appendChild(aElement));
            }

        }
        zos.putNextEntry(new ZipEntry(entry.getName()));
        zos.write(document.outerHtml().getBytes());
    }

    /**
     * Modifies the content.opf file in the EPUB.
     * @param zos The ZipOutputStream to write the modified file to.
     * @param entry The original ZipEntry of the file.
     * @param is The InputStream of the original file content.
     * @throws IOException If an I/O error occurs.
     */
    private void modifyContentOpf(ZipOutputStream zos, ZipEntry entry, InputStream is) throws IOException {
        Document document = Jsoup.parse(is, "UTF-8", "", Parser.xmlParser());
        Element titleElement = document.getElementsByTag("dc:title").first();
        if (titleElement != null) {
            titleElement.text(novel.getName() + " - " + novel.getAuthor().getName());
        }

        Element manifestElement = document.getElementsByTag("manifest").first();
        if (manifestElement != null) {
            for (Chapter chapter : chapterList) {
                Element itemElement = document.createElement("item");
                itemElement.attr("id", chapter.getChapterId().trim());
                itemElement.attr("href", "text/"+chapter.getChapterId().trim()+".xhtml");
                itemElement.attr("media-type", "application/xhtml+xml");
                manifestElement.appendChild(itemElement);
            }
        }

        Element spineElement = document.getElementsByTag("spine").first();
        if (spineElement != null) {
            for (Chapter chapter : chapterList) {
                Element itemrefElement = document.createElement("itemref");
                itemrefElement.attr("idref", chapter.getChapterId().trim());
                spineElement.appendChild(itemrefElement);
            }
        }
        zos.putNextEntry(new ZipEntry(entry.getName()));
        zos.write(document.outerHtml().getBytes());
    }

    /**
     * Modifies a chapter xhtml file in the EPUB.
     * @param is The InputStream of the original file content.
     * @param chapter The chapter information to modify the file with.
     * @throws IOException If an I/O error occurs.
     */
    private Map.Entry<String, byte[]> modifyAndSaveChapterXhtml(InputStream is, Chapter chapter) {
        // Parse the original chap01.xhtml content
        Document document = null;
        try {
            document = Jsoup.parse(is, "UTF-8", "", Parser.xmlParser());
        } catch (IOException e) {
           throw new RuntimeException(e.getMessage());
        }

        // Make the necessary modifications to the chapter content
        Element bodyElement = document.getElementsByTag("body").first();
        if (bodyElement != null) {
            int attempt = 10;
            Chapter data;
            do {
                data = pluginFactory.getContentChapter(novel.getNovelId(), chapter.getChapterId().trim());
                attempt--;
            } while ( attempt > 0 && data == null);

            if(data != null) {
                // Set title element
                Element titleElement = bodyElement.getElementsByTag("h2").first();
                if(titleElement != null) {
                    titleElement.text(data.getName().trim());
                }

                // Set the HTML content of the content container
                Element contentElement = bodyElement.getElementsByTag("div").first();
                if(contentElement != null) {
                    contentElement.html(data.getContent().replaceAll("<br>", "<br></br>"));
                }
            } else {
                System.out.println("Failed to get chapter " + chapter.getChapterId().trim());
                String chapterEntryName = "OEBPS/text/" + chapter.getChapterId().trim() + ".xhtml";
                return new AbstractMap.SimpleEntry<>(chapterEntryName, null);
            }
        }

        String chapterEntryName = "OEBPS/text/" + chapter.getChapterId().trim() + ".xhtml";
        byte[] modifiedContent = document.outerHtml().getBytes(StandardCharsets.UTF_8);
        return new AbstractMap.SimpleEntry<>(chapterEntryName, modifiedContent);
    }

    /**
     * Sends the modified EPUB file to the client and deletes the file from the server.
     * @param fileName The file path of the EPUB to send.
     * @param response The HttpServletResponse to send the file to.
     * @throws IOException If an I/O error occurs.
     */
    private void sendFileToClientAndDelete(String fileName, HttpServletResponse response) throws IOException {
        File file = new File(fileName);

        // URL encode the filename to handle non-ASCII characters
        String encodedFileName = encodeFileName(file.getName());

        response.setContentType("application/epub+zip");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        response.setContentLength((int) file.length());

        try (InputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }

        if (!file.delete()) {
            System.err.println("Failed to delete temporary file: " + file.getAbsolutePath());
        }
    }

    /**
     * Encode the EPUB file name
     * @param fileName The file path of the EPUB to send.
     * @return the encoded name
     */
    private String encodeFileName(String fileName) {
        return URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
