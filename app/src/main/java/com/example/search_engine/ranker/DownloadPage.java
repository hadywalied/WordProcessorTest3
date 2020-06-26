package com.example.search_engine.ranker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.time.LocalDate;


public class DownloadPage {

    public static Double distanceRelevence(String userLocation,String webpageLocation) throws IOException
    {

        double distance = Double.NaN;
        userLocation = userLocation.replaceAll(" ", "%20");
        webpageLocation = webpageLocation.replaceAll(" ", "%20");
        URL url = new URL(String.format("https://distancecalculator.globefeed.com/Distance_Between_Countries_Result.asp?fromplace=%s&toplace=%s", userLocation,webpageLocation));
        URLConnection con = url.openConnection();
        InputStream is =con.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = null;

        while ((line = br.readLine()) != null)
        {
            if(line.contains("Kilometers:"))
            {
                int beginIndex = line.indexOf("Kilometers:") + 15;
                int endIndex = line.indexOf("km.");
                distance = Double.parseDouble(line.substring(beginIndex, endIndex));
                break;
            }
        }
        if (distance == Double.NaN)
            distance = 0.5;
        else
        {
            distance /= 20000; // half the circumference of earth to normalize
            distance = 1- distance; // larger distances equal lower relevance
        }
        return distance;
    }
    public static Double dateRelevence(String Url) throws IOException
    {
        String siteDate = "";
        Boolean DateFound = false;
        URL url = new URL(String.format(Url));
        URLConnection con = url.openConnection();
        InputStream is =con.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;

        while ((line = br.readLine()) != null)
        {
            if(line.contains("datePublished"))
            {
                DateFound = true;
                int beginIndex = line.indexOf("datePublished") + 17;
                int endIndex = line.indexOf("dateModified")-3;
                siteDate = line.substring(beginIndex, endIndex);
            }
        }
        if (DateFound)
        {
            LocalDate Dateofdevice = java.time.LocalDate.now();
            LocalDate FormattedSiteDate = LocalDate.parse(siteDate);
            Duration difference = Duration.between(FormattedSiteDate, Dateofdevice);
            Long differenceInDays = difference.toDays();
            Double output = differenceInDays.doubleValue();
            return output;
        }
        else
        {
            return 0.5;
        }
    }
}