package com.crawldata.back_end.export_plugin_builder.audio;

import com.crawldata.back_end.export_plugin_builder.ExportPluginFactory;
import com.crawldata.back_end.model.Chapter;
import com.crawldata.back_end.novel_plugin_builder.PluginFactory;
import com.crawldata.back_end.response.DataResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.springframework.scheduling.annotation.Async;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class AudioPlugin implements ExportPluginFactory {
    private PluginFactory pluginFactory;
    private List<Chapter> chapters = new ArrayList<>();
    private final String API_TTS_URL = "https://texttospeech.googleapis.com/v1/text:synthesize?key=";
    private final String API_KEY = "AIzaSyB1ucFkcQcjXaWZMyQRq_R7JGYW0tQ-x7w";

    public String getTTS(String apiUrl, String datajson) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(apiUrl);
        StringEntity body = new StringEntity(datajson, "UTF-8");
        request.addHeader("content-type", "application/json;charset=UTF-8");
        request.setEntity(body);
        HttpResponse response = httpClient.execute(request);
        InputStream content = response.getEntity().getContent();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(content))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            String jsonResponse = result.toString();
            int startIndex = jsonResponse.indexOf("\"audioContent\": \"") + 17;
            int endIndex = jsonResponse.indexOf("\"", startIndex);
            return jsonResponse.substring(startIndex, endIndex);
        }
    }
    private void getNovelInfo(PluginFactory plugin, String fromChapterId, int numChapters, String novelId) {
        pluginFactory = plugin;
        int from = Integer.parseInt(fromChapterId.split("-")[1]);
        for (int i = from; i < numChapters + from; i++) {
            String idChapter = "chuong-" + i;
            DataResponse dataResponse = pluginFactory.getNovelChapterDetail(novelId, idChapter);
            if (dataResponse != null && dataResponse.getStatus().equals("success")) {
                Chapter chapter = (Chapter) dataResponse.getData();
                chapters.add(chapter);
            }
        }
    }
    private void combineAudioFiles(List<File> inputFiles, File outputFile) throws IOException, UnsupportedAudioFileException {
        try (SequenceInputStream sis = new SequenceInputStream(Collections.enumeration(inputFiles.stream().map(file -> {
            try {
                return new AudioInputStream(new FileInputStream(file), AudioSystem.getAudioFileFormat(file).getFormat(), file.length());
            } catch (UnsupportedAudioFileException | IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList())))) {
            AudioInputStream combinedAudioInputStream = new AudioInputStream(sis,
                    AudioSystem.getAudioFileFormat(inputFiles.get(0)).getFormat(),
                    inputFiles.stream().mapToLong(file -> {
                        try {
                            return AudioSystem.getAudioFileFormat(file).getFrameLength();
                        } catch (UnsupportedAudioFileException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).sum());
            AudioSystem.write(combinedAudioInputStream, AudioFileFormat.Type.WAVE, outputFile);
        }
    }

    public List<String> splitTextIntoWordChunks(String text, int maxWordsPerChunk) {
        List<String> chunks = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder chunk = new StringBuilder();
        int wordCount = 0;
        for (String word : words) {
            if (wordCount >= maxWordsPerChunk) {
                chunks.add(chunk.toString());
                chunk.setLength(0);
                wordCount = 0;
            }
            chunk.append(word).append(" ");
            wordCount++;
        }
        if (chunk.length() > 0) {
            chunks.add(chunk.toString());
        }
        return chunks;
    }

    @Async
    @Override
    public void export(PluginFactory plugin, String novelId, String fromChapterId, int numChapters, HttpServletResponse response) throws IOException {
        List<File> chapterFiles = new ArrayList<>();
        getNovelInfo(plugin, fromChapterId, numChapters, novelId);
        String fileName = novelId.replace("-", "_") + "_" + fromChapterId.replace("-", "_") + "_" + numChapters;

        ExecutorService executorService = Executors.newFixedThreadPool(10); // Create a thread pool with 10 threads
        List<Future<File>> futures = new ArrayList<>();

        try {
            for (Chapter chapter : chapters) {
                String content = Jsoup.parse(chapter.getContent()).text().replace("\"", "");
                List<String> textChunks = splitTextIntoWordChunks(content, 500);
                for (String chunk : textChunks) {
                    Callable<File> task = () -> {
                        String dataJson = String.format(
                                "{\"input\": {\"text\": \"%s\"}, \"voice\": {\"languageCode\": \"vi-VN\", \"name\": \"vi-VN-Standard-D\"}, \"audioConfig\": {\"audioEncoding\": \"LINEAR16\",\"speakingRate\": 1.3}}", chunk);
                        String base64Audio = getTTS(API_TTS_URL + API_KEY, dataJson);
                        byte[] audioData = Base64.getDecoder().decode(base64Audio);
                        File tempFile = File.createTempFile("chunk_", ".wav");
                        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                            fos.write(audioData);
                        }
                        return tempFile;
                    };
                    futures.add(executorService.submit(task));
                }
            }
            // Wait for all tasks to complete and collect results
            for (Future<File> future : futures) {
                try {
                    chapterFiles.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            File combinedFile = File.createTempFile(fileName, ".wav");
            combineAudioFiles(chapterFiles, combinedFile);
            response.setContentType("audio/wav");
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s.wav\"", fileName));
            try (InputStream is = new FileInputStream(combinedFile);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
            for (File file : chapterFiles) {
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }
}
