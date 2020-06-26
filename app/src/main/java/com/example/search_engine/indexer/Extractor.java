package com.example.search_engine.indexer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.sql.SQLException;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class Extractor {


    public void extractFromHtml(String htmlPath, int numberOfUrls) throws IOException, SQLException, InterruptedException {

        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path pathh = Paths.get(htmlPath);
        pathh.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

        boolean poll = true;
        int i=1;
        int countFiles=0;
        while (poll) {
            WatchKey key = watchService.take();
            String lastfilepath ="";
            for (WatchEvent<?> event : key.pollEvents()) {
                Path file = pathh.resolve((Path)event.context());
                System.out.println("Event kind : " + event.kind() + " - File : " + event.context());
                String filePath = pathh + "\\" + event.context();
                if (event.kind() ==ENTRY_MODIFY && !filePath.equals(lastfilepath)) {
                    countFiles++;
                    System.out.println("file path   " + filePath+"    "+file.toFile().lastModified());
                    String multihtml = new String(Files.readAllBytes(Paths.get(filePath)));
                    String[] htmlParts = multihtml.split("(?<=</html>)");
                    org.jsoup.nodes.Document doc;
                    for (String part : htmlParts) {
                        doc = Jsoup.parse(part);

                        //Title
                        String title = "";
                        title = doc.title();

                        //Remove stop words by stemmer
                        // com.example.search_engine.Stemmer S = new com.example.search_engine.Stemmer();
                         //title = S.stem(title);

                        System.out.println("title : " + title);
                        System.out.println("");
                        //Headers
                        Element body = doc.body();
                        Elements paragraphs = body.getElementsByTag("header");
                        String header = "";
                        for (Element paragraph : paragraphs) {
                            header = header + paragraph.text();
                        }

                        //Remove stop words
                       // com.example.search_engine.Stemmer S2 = new com.example.search_engine.Stemmer();
                        //header = S2.stem(header);
                        System.out.println("header:   " + header);


                        // Plain text
                        String string = doc.body().text();

                        //Remove stop words
                       // com.example.search_engine.Stemmer S3 = new com.example.search_engine.Stemmer();
                       // string = S3.stem(string);
                        String tempWord = title + " " + header + " ";
                        string = string.replaceAll(tempWord, "");

                       // com.example.search_engine.Stemmer S4 = new com.example.search_engine.Stemmer();
                       // allBody = S4.stem(allBody);
                        System.out.println("body:  " + string);

                        if (!title.isEmpty() || !header.isEmpty() || !string.isEmpty()) {
                            // Write in files and database
                            try {
                                String filename = "filename" + Integer.toString(i) + ".txt";
                                System.out.println("paaaaaaaath  "+i+"  "+ filename);

                                FileWriter myWriter = new FileWriter("filename" + Integer.toString(i) + ".txt");
                                myWriter.write(title + " , " + header + " , " + string);
                                myWriter.close();
                                i++;
                                System.out.println("Successfully wrote to the file.");
                                 lastfilepath = filePath;
                            } catch (IOException e) {
                                System.out.println("An error occurred.");
                                e.printStackTrace();
                            }
                        }
                    }
                }
            if (countFiles==numberOfUrls)
            {
                break;
            }
            }
            if (countFiles==numberOfUrls)
            {
                break;
            }
            poll = key.reset();
        }
    }
}
