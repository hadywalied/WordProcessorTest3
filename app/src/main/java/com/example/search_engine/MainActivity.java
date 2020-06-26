package com.example.search_engine;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.search_engine.Data.Data;
import com.example.search_engine.QueryProcessor.QueryProcessor;
import com.example.search_engine.imageSearch.ImageSearch;
import com.example.search_engine.indexer.Extractor;
import com.example.search_engine.indexer.LuceneTester;
import com.example.search_engine.phraseSearch.PhraseSearch;
import com.example.search_engine.ranker.Ranker;
import com.example.search_engine.webCrawler.WebCrawler;

import org.apache.lucene.queryParser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    TextView textViewResult;
    EditText editTextQuery;
    Button buttonSearch;

    public static String location = "Egypt";
    public static String linksToCrawl[] = {"https://stackoverflow.com/", "https://en.wikipedia.org/", "https://www.geeksforgeeks.org/", "https://www.w3schools.com/"};

    File folder, file1, file2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewResult = findViewById(R.id.tv_result);
        editTextQuery = findViewById(R.id.et_query);
        buttonSearch = findViewById(R.id.btn_search);

        folder = getFilesDir();
        folder.mkdir();

        file1 = new File(folder, "html1.html");
        try {
            file1.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        file2 = new File(folder, "html2.html");
        try {
            file2.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    searchQuery(editTextQuery.getText().toString());
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void searchQuery(String word) throws IOException, SQLException, InterruptedException {

//        main.word = word;

        InputStream is   = getResources().openRawResource(R.raw.words_alpha);
        InputStreamReader inputreader = new InputStreamReader(is);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line = buffreader.readLine();
        List<String> listtext = new ArrayList<>();
        while (line != null) {
            listtext.add(line);
            line = buffreader.readLine();
        }



        int numberOFurls = 10;
        int numberOfThreads = 4;

        String htmlPath = file1.getPath();
        String imagePath = file2.getPath();

//        String filenamePath = System.getProperty("user.dir");

/*
        LuceneTester tester2 = new LuceneTester();
        tester2.delete(htmlPath, imagePath);
*/

//        String searchSentence ="education full protection";
        String searchSentence = "\"Stack Overflow\"";
        // remove quotes from the sentence
        searchSentence = word;
        String searchPhrase = word;
        searchPhrase = word.replaceAll("^\"|\"$", "");

        ///////////// query processor ///////////////////////
        writeToScreen(searchPhrase);
        QueryProcessor queryProcessor = new QueryProcessor(searchSentence,listtext);
        String newSearchSentence = queryProcessor.getNewSearchSentence();
        ///////////// query processor ///////////////////////
        searchSentence = newSearchSentence;
        writeToScreen("leeeeeeeeeeeength  " + searchSentence);

        //Crawler
        Thread crawlerThreads[] = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; ++i) {
            crawlerThreads[i] = new Thread(new WebCrawler(linksToCrawl[0], numberOFurls, folder));
            crawlerThreads[i].start();
        }


        //Extractor ///
        Data dataa = new Data();
        Extractor extract = new Extractor();
        extract.extractFromHtml(htmlPath, numberOFurls);


        //Indexer

        try {
            LuceneTester tester;
            tester = new LuceneTester();
            long startTimeIndexer = System.currentTimeMillis();
            tester.createIndex();
            try {
                tester.search(searchSentence, dataa, htmlPath, dataa.urlsFromCrawler);
            } catch (ParseException e) {
                e.printStackTrace();
            }
//            tester.delete(htmlPath, imagePath);
            long endTimeIndexer = System.currentTimeMillis();
            writeToScreen("Indexer, time taken: " + (endTimeIndexer - startTimeIndexer) + " ms");
        } catch (IOException e) {
            e.printStackTrace();
        }

        ////////////// Phrase Search ///////////////////
        writeToScreen("**********************");
        PhraseSearch phraseSearch = new PhraseSearch(searchPhrase, dataa.occurencesOfWordsCount, dataa.documentsName, folder);
        boolean phraseSearchTest = phraseSearch.checkPhraseSearch();
        if (phraseSearchTest) {
            phraseSearch.countPhrase();
            writeToScreen(phraseSearch.wordsToBeSearch.toString());
            for (int i = 0; i < phraseSearch.foundWords.length; ++i)
                writeToScreen(" index " + i + " = " + phraseSearch.foundWords[i]);
            writeToScreen("there is phrase search");
        } else {
            writeToScreen("no phrase search");
        }
        ////////////// Phrase Search ///////////////////

        //Ranker
        Boolean wantLocationScore = false;
        Boolean wantDateScore = false;
        Ranker ranker = new Ranker(dataa, searchSentence, location, wantLocationScore, wantDateScore);


        writeToScreen("**********************");
        writeToScreen(dataa.documentsName.toString());
        System.out.println("hellooooooooo");
        writeToScreen("helloooooooooooooooo");
        System.out.println(dataa.documentsName);
        writeToScreen(dataa.documentsBody.toString());
        writeToScreen(dataa.occurencesOfWordsCount.toString());
        writeToScreen(dataa.occurencesOfWordsInTitle.toString());
        writeToScreen(dataa.occurencesOfWordsInHeader.toString());
        writeToScreen(dataa.occurencesOfWordsInPlainText.toString());
        writeToScreen(dataa.titlelist.toString());

        writeToScreen(dataa.urlsFromCrawler.toString());
        writeToScreen(dataa.urlsFromIndexer.toString());
        //com.example.search_engine.ranker output
        writeToScreen(ranker.rank(dataa, dataa.urlsFromIndexer).toString());
        writeToScreen(ranker.rankIndices(dataa, dataa.urlsFromIndexer).toString());

        writeToScreen(String.valueOf(dataa.titlelist.size()));


        //Write com.example.search_engine.Data in txt files
        List<String> plaintexts = new ArrayList<String>();
        List<String> documentstitles = new ArrayList<String>();


        writeToScreen(dataa.documentsName.toString());

        try {
            String documentsName = "data\\documentsName" + ".txt";
            FileWriter myWriter = new FileWriter(documentsName);
            for (int i : ranker.rankIndices(dataa, dataa.urlsFromIndexer)) {
                myWriter.write(dataa.documentsName.get(i) + System.lineSeparator());
            }
            myWriter.close();

            FileWriter myWriter2 = new FileWriter("data\\documentstitle.txt");
            for (int i = 0; i < dataa.titlelist.size(); i = i + dataa.titlelist.size() / dataa.documentsName.size()) {
                documentstitles.add(dataa.titlelist.get(i));
            }
            for (int i : ranker.rankIndices(dataa, dataa.urlsFromIndexer)) {
                myWriter2.write(documentstitles.get(i) + System.lineSeparator());
            }
            myWriter2.close();

            FileWriter myWriter3 = new FileWriter("data\\documentsurls.txt");
            for (int i : ranker.rankIndices(dataa, dataa.urlsFromIndexer)) {
                myWriter3.write(dataa.urlsFromIndexer.get(i) + System.lineSeparator());
            }
            myWriter3.close();


            for (int i = 0; i < dataa.plaintextlist.size(); i = i + dataa.plaintextlist.size() / dataa.documentsName.size()) {
                plaintexts.add(dataa.plaintextlist.get(i));
            }
            FileWriter myWriter4 = new FileWriter("data\\documentsplaintext.txt");
            for (int i : ranker.rankIndices(dataa, dataa.urlsFromIndexer)) {
                myWriter4.write(plaintexts.get(i) + System.lineSeparator());
            }
            myWriter4.close();


            FileWriter myWriter5 = new FileWriter("data\\documentsheader.txt");
            for (int i : ranker.rankIndices(dataa, dataa.urlsFromIndexer)) {
                if (dataa.headerslist.get(i) == null) {
                    myWriter5.write(dataa.headerslist.get(i) + " * ");
                }
            }
            myWriter5.close();
        } catch (IOException e) {
            writeToScreen("An error occurred.");
            e.printStackTrace();
        }


        //Write to DataBase
        for (int i : ranker.rankIndices(dataa, dataa.urlsFromIndexer)) {
            String sql = "INSERT INTO documentsUrl(url) VALUES(?)";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, dataa.urlsFromIndexer.get(i));
                pstmt.executeUpdate();
            } catch (SQLException e) {
                writeToScreen(e.getMessage());
            }
        }

        for (int i : ranker.rankIndices(dataa, dataa.urlsFromIndexer)) {
            String sql = "INSERT INTO documentsTitle(title) VALUES(?)";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, documentstitles.get(i));
                pstmt.executeUpdate();
            } catch (SQLException e) {
                writeToScreen(e.getMessage());
            }
        }
        for (int i : ranker.rankIndices(dataa, dataa.urlsFromIndexer)) {
            String sql = "INSERT INTO documentsParagraphs(paragraph) VALUES(?)";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, plaintexts.get(i));
                pstmt.executeUpdate();
            } catch (SQLException e) {
                writeToScreen(e.getMessage());
            }
        }


        //Image search
        //Ranked urls
        List<String> rankedurls = new ArrayList<String>();
        for (int i : ranker.rankIndices(dataa, dataa.urlsFromIndexer)) {
            rankedurls.add(dataa.urlsFromIndexer.get(i));
        }
        for (String str : rankedurls) {
            ImageSearch im = new ImageSearch();
            im.extractImage(str, searchSentence);
        }

    }

    void writeToScreen(String text) {
        if (textViewResult != null) {
            textViewResult.append("\n" + text);
        }
    }

    public static Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:" + System.getProperty("user.dir") + "\\DataBase.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
//            writeToScreen(e.getMessage().toString());
        }
        return conn;
    }

}
