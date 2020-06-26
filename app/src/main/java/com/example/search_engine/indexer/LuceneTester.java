package com.example.search_engine.indexer;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.example.search_engine.Data.Data;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class LuceneTester {

    String indexDir = System.getProperty("user.dir") + "//Index";
    String dataDir = System.getProperty("user.dir");
    Indexer indexer;
    Searcher searcher;

    public void createIndex() throws IOException {
        indexer = new Indexer(indexDir);
        int numIndexed;
        numIndexed = indexer.createIndex(dataDir, new TextFileFilter());
        indexer.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void search(String search_word, Data data, String htmlPath, List<String> urlsFromCrawler) throws IOException, ParseException {
        searcher = new Searcher(indexDir);

        TopDocs hits = searcher.search(search_word);


        // List of names of html documents found
        List<String> documentsName = new ArrayList<String>();
        List<String> documentsBody = new ArrayList<String>();
        List<Integer> occurencesOfWordsCount = new ArrayList<Integer>();
        List<Integer> occurencesOfWordsInTitle = new ArrayList<Integer>();
        List<Integer> occurencesOfWordsInHeader = new ArrayList<Integer>();
        List<Integer> occurencesOfWordsInPlainText = new ArrayList<Integer>();
        List<String> images = new ArrayList<String>();
        List<String> titleslist = new ArrayList<String>();
        List<String> headerslist = new ArrayList<String>();
        List<String> plaintextlist = new ArrayList<String>();
        List<String> urlsFromIndexer = new ArrayList<String>();

        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = searcher.getDocument(scoreDoc);

            String filename = doc.get(LuceneConstants.FILE_NAME);
            filename = filename.replaceAll("\\D+", "");

            // TO GET Name of HTML document
            File dir = new File(htmlPath);
            File[] directoryListing = dir.listFiles();
            int i = Integer.parseInt(filename);
            int count = 1;

            String[] searchedWords = search_word.split(" ");


            // Get the file include each word in searched words
            for (File child : directoryListing) {
                if (count == i) {

                    //Urls fromIndexer
                    System.out.println("cooount   " + count);
                    System.out.println(urlsFromCrawler.size());
                    System.out.println(urlsFromCrawler.size());

                    String url = urlsFromCrawler.get(count - 1);
                    urlsFromIndexer.add(url);

                    System.out.println("File name: " + child.getName());
                    documentsName.add(child.getName());
                    System.out.println("naaaaaaaaaaaame  " + child.getName());

                    File file = new File(doc.get(LuceneConstants.FILE_PATH));
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String st;
                    String text = "";
                    while ((st = br.readLine()) != null)
                        text = text + st;

                    documentsBody.add(text);

                    String[] arr = text.split(" ");

                    //Get position of commas
                    List<Integer> commaPositions = new ArrayList<Integer>();
                    int commaPosition = 0;
                    for (String ss : arr) {
                        if (new String(",").equals(ss)) {
                            commaPositions.add(commaPosition);
                        }
                        commaPosition++;
                    }
                    for (String word : searchedWords) {

                        // Get number of occurence in each document
                        int countWord = 0;
                        int position = 0;
                        List<Integer> positions = new ArrayList<Integer>();
                        for (String ss : arr) {
                            if (new String(word).equals(ss)) {
                                countWord++;
                                positions.add(position);
                            }
                            position++;
                        }

                        // Get type of searched word
                        int titleCount = 0;
                        int headerCount = 0;
                        int plainTextCount = 0;
                        for (int n = 0; n < positions.size(); n++) {
                            if (positions.get(n) < commaPositions.get(0)) {
                                titleCount++;
                            } else if (positions.get(n) < commaPositions.get(1)) {
                                headerCount++;
                            } else
                                plainTextCount++;
                        }

                        System.out.println("Number of occurence of word " + word + " = " + countWord);
                        occurencesOfWordsCount.add(countWord);
                        occurencesOfWordsInTitle.add(titleCount);
                        occurencesOfWordsInHeader.add(headerCount);
                        occurencesOfWordsInPlainText.add(plainTextCount);
                        System.out.println("Type of word   " + word + " :  " + " Title Count = " + titleCount + " Header Count = " + headerCount + "  Plain Text Count = " + plainTextCount);


                        //image search
                        String multihtml = new String(Files.readAllBytes(Paths.get(child.getPath())));
                        String[] htmlParts = multihtml.split("(?<=</html>)");
                        org.jsoup.nodes.Document htmllDoc;

                        // Get all img tags
                        for (String part : htmlParts) {
                            htmllDoc = Jsoup.parse(part);
                            Elements img = htmllDoc.getElementsByTag("img");
                            // Loop through img tags
                            for (Element el : img) {
                                images.add(el.attr("src"));
                                //  System.out.println("image tag: " + el.attr("src") + " Alt: " + el.attr("alt"));
                            }

                            //Title
                            String title = "";
                            title = htmllDoc.title();
                            //paragraphs
                            Element body = htmllDoc.body();
                            //header
                            Elements headers = body.getElementsByTag("header");
                            String header = "";
                            for (Element paragraph : headers) {
                                header = header + paragraph.text();
                            }

                            // Plain text
                            String tempWord = header;
                            String allBody = htmllDoc.body().text();
                            String plainText = allBody.replaceAll(tempWord, "");
                            int indexFound = -1;
                            for (String searchWord : searchedWords) {
                                indexFound = plainText.indexOf(searchWord);
                                if (indexFound > -1) {
                                    break;
                                }
                            }
                            if (indexFound == -1)
                                indexFound = 0;


                            titleslist.add(title);
                            headerslist.add(header);
                            if (plainText.length() - indexFound < 300) {
                                plaintextlist.add(plainText.substring(indexFound, plainText.length()) + "....");
                            } else {
                                plaintextlist.add(plainText.substring(indexFound, indexFound + 300) + "....");
                            }
                        }
                    }
                    break;
                }
                count++;
            }
        }
        data.SetDataFromIndexer(documentsName, documentsBody, occurencesOfWordsCount, occurencesOfWordsInTitle, occurencesOfWordsInHeader, occurencesOfWordsInPlainText, images, titleslist, headerslist, plaintextlist, urlsFromIndexer);
        searcher.close();
    }


    public void delete(String htmlPath, String imagePath) throws IOException {
        File folder = new File(htmlPath);
        if (folder.isFile()) {
            folder.delete();
            return;
        }
        File folder2 = new File(imagePath);
        if (folder2.isFile()) {
            folder2.delete();
            return;
        }
        for (File f : folder.listFiles()) {
            f.delete();
        }
        for (File f : folder2.listFiles()) {
            f.delete();
        }
    }
}