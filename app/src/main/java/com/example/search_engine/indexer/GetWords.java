package com.example.search_engine.indexer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GetWords {

    public String document;
    public GetWords(String docc) {
        document = docc;
    }
    public void get(String htmlPath)throws IOException
    {
        File dir = new File(htmlPath);
        File[] directoryListing = dir.listFiles();
        int i=1;
        for (File child : directoryListing) {

            String filename = child.getName();
            String first = new String(filename);
            String second = new String(document);

            if (first.equals(second)) {
                String path = child.getAbsolutePath();
                String multihtml = new String(Files.readAllBytes(Paths.get(path)));
                String[] htmlParts = multihtml.split("(?<=</html>)");
                org.jsoup.nodes.Document doc;

                for (String part : htmlParts) {
                    doc = Jsoup.parse(part);
                    //Title
                    String title = "";
                    title = doc.title();

                    //Headers
                    Element body = doc.body();
                    Elements paragraphs = body.getElementsByTag("header");
                    String header = "";
                    for (Element paragraph : paragraphs) {
                        header = header + paragraph.text();
                    }
                    // Plain text
                    String string = doc.body().text();
                    String tempWord = title + " " + header + " ";
                    string = string.replaceAll(tempWord, "");

                    tempWord = " " + title + " " + header;
                    string = string.replaceAll(tempWord, "");

                    System.out.println(title+" "+ header+" "+ string);
                }
            }

        }
    }
}
