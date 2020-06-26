package com.example.search_engine.Data;


import java.util.ArrayList;
import java.util.List;

public class Data {

    public List<String>  documentsName ;
    public List<String> documentsBody;
    public List<Integer> occurencesOfWordsCount;
    public List<Integer> occurencesOfWordsInTitle;
    public List<Integer> occurencesOfWordsInHeader;
    public List<Integer> occurencesOfWordsInPlainText;
    public List<String> images;
    public List<String> titlelist;
    public List<String> headerslist;
    public List<String> plaintextlist;
    public static List<String> urlsFromCrawler;
    public static List<String>urlsFromIndexer;
    public static List<String> documentsPhraseSearch;
    public static List<Integer> countPhraseSearch;
    public static List<String> similarWords;
    public static List<String> originalWord;

    public  Data() {
        documentsName =new ArrayList<String>();
        documentsBody=new ArrayList<String>();
        occurencesOfWordsCount=new ArrayList<Integer>();
        occurencesOfWordsInTitle=new ArrayList<Integer>();
        occurencesOfWordsInHeader=new ArrayList<Integer>();
        occurencesOfWordsInPlainText=new ArrayList<Integer>();
        images=new ArrayList<String>();
        titlelist=new ArrayList<String>();
        headerslist=new ArrayList<String>();
        plaintextlist=new ArrayList<String>();
        urlsFromCrawler = new ArrayList<String>();
        urlsFromIndexer = new ArrayList<String>();
        documentsPhraseSearch = new ArrayList<String>();
        countPhraseSearch = new ArrayList<Integer>();
        similarWords = new ArrayList<String>();
        originalWord = new ArrayList<String>();

    }
    public void SetDataFromIndexer(List<String>  documentsNametemp,List<String> documentsBodyemp,
                                   List<Integer> occurencesOfWordsCounttemp,List<Integer> occurencesOfWordsInTitletemp,List<Integer> occurencesOfWordsInHeadertemp,
                                   List<Integer> occurencesOfWordsInPlainTexttemp,List<String>  imagestemp,
                                   List<String>  titletemp,List<String>  headertemp,List<String>  plaintexttemp,List<String> urlsFromIndexertemp) {
        this.documentsName = documentsNametemp;
        this.documentsBody = documentsBodyemp;
        this.occurencesOfWordsCount = occurencesOfWordsCounttemp;
        this.occurencesOfWordsInTitle =occurencesOfWordsInTitletemp ;
        this.occurencesOfWordsInHeader= occurencesOfWordsInHeadertemp;
        this.occurencesOfWordsInPlainText = occurencesOfWordsInPlainTexttemp;
        this.images = imagestemp;
        this.titlelist = titletemp;
        this.headerslist = headertemp;
        this.plaintextlist = plaintexttemp;
        this.urlsFromIndexer= urlsFromIndexertemp;
    }

    public static void AddFromCrawler(String url) {
        urlsFromCrawler.add(url);
    }

    public static void AddFromPhraseSearch(String htmlDoc) {
        documentsPhraseSearch.add(htmlDoc);
    }
}