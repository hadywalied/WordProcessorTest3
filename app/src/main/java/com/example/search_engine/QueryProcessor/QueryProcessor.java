package com.example.search_engine.QueryProcessor;

import com.example.search_engine.Stemmer.Stemmer;

import java.util.ArrayList;
import java.util.List;

public class QueryProcessor {

    public static String searchSentence;
    public List<String> documentsName;
    List<String> stemmWords = new ArrayList<String>();

//    final String PATH = "E:\\Study\\2nd Semester\\APT\\Eclipse\\search_engine\\html2\\";

    public QueryProcessor(String searchSentence, List<String> mSetmmWords) {
        this.searchSentence = searchSentence;
        this.documentsName = new ArrayList<String>();
        this.documentsName = documentsName;
        this.stemmWords = mSetmmWords;
    }

    ////// functions

    public String getNewSearchSentence() {

        String result = "";
        Stemmer stemmer = new Stemmer();
        String[] arr = searchSentence.split(" ");

        List<String> searchList = new ArrayList<String>();

        for (String ss : arr)
            searchList.add(ss);

        for (int i = 0; i < searchList.size(); ++i)
            result += ' ' + stemmer.stem(searchList.get(i));

        searchList.clear();

        String[] arr2 = result.split(" ");

//        List<String> allWords = new ArrayList<String>();


        for (int j = 1; j < arr2.length; ++j) {
            String ss = arr2[j];

            for (int i = 0; i < stemmWords.size(); ++i) {
//                Pattern pattern = Pattern.compile(ss, Pattern.CASE_INSENSITIVE);
//                Matcher matcher = pattern.matcher(allWords.get(i));
//                if (matcher.find())
//                {
//                    searchList.add(allWords.get(i));
//                }
                if (stemmWords.get(i).equals(ss)) {
                    searchList.add(stemmWords.get(i));
                }
            }
        }


        result = "";

        for(String it : searchList){
            result = result + " " + it;
        }

        return result;
    }

}

