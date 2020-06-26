package com.example.search_engine.imageSearch;

import java.io.*;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;

public class ImageSearch {
    public static void extractImage(String webSiteURL, String searchedSentence) {
        try {
            //Connect to the website and get the html
            Document doc = Jsoup.connect(webSiteURL).get();
            //Get all elements with img tag ,
            Elements img = doc.getElementsByTag("img");
            for (Element el : img) {
                //for each element get the srs url
                String src = el.absUrl("src");
                if (src.contains("png") ==false)
                {
                    break;
                }
                else {
                    getImages(src,searchedSentence);
                }
            }
        } catch (IOException ex) {
            System.err.println("There was an error");
            Logger.getLogger(ImageSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void getImages(String src, String searchedSentence) throws IOException {

        String folder = null;
        String folderPath = System.getProperty("user.dir") + "\\images";
        //Exctract the name of the image from the src attribute
        int indexname = src.lastIndexOf("/");
        if (indexname == src.length()) {
            src = src.substring(1, indexname);
        }
        indexname = src.lastIndexOf("/");
        String name = src.substring(indexname, src.length());

        //System.out.println(name);

        int found =0;
        String[] searchedWords = searchedSentence.split(" ");
        for(String word : searchedWords)
        {
            System.out.println(word);
            if (name.toLowerCase().contains(word.toLowerCase()))
            {
                found =1;
                break;
            }
        }
        if (found ==1) {
            //Open a URL Stream
            URL url = new URL(src);
            InputStream in = url.openStream();

            OutputStream out = new BufferedOutputStream(new FileOutputStream(folderPath + name));

            for (int b; (b = in.read()) != -1; ) {
                out.write(b);
            }
            out.close();
            in.close();
        }
    }
}
