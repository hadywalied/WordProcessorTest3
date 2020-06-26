package com.example.search_engine.webCrawler;

import com.example.search_engine.Data.Data;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

public class WebCrawler implements Runnable {

    public static int count = 0;
    //public static final int MAX_NUM = 5;
    public static final boolean DEBUG = true;
    public static final String DISALLOW = "Disallow:";
    public String urlLink;
    public static int numberOfurls;
    File folder;

    public WebCrawler(String urlLink, int numberOfurls, File mFolder) {
        this.urlLink = urlLink;
        this.numberOfurls = numberOfurls;
        this.folder = mFolder;
    }

    public static int[] foundWords = new int[numberOfurls];
    //	public static String word = "";
//	public static boolean wantToFindWord = false;
    public static String phrase = "";

    public static HashSet<String> links = new HashSet<String>();

    public void getPageLinks(String URL, String title) {
        //4. Check if you have already crawled the URLs 
        //(we are intentionally not checking for duplicate content in this example)
        if (!links.contains(URL)) {
            try {
                //4. (i) If not add it to the index
                if (links.add(URL)) {
                    System.out.println(URL);
                    htmlSaver(URL, String.valueOf(count) + "_" + title);
                    count++;
                    Data.AddFromCrawler(URL);
                }

                //2. Fetch the HTML code
                Document document = Jsoup.connect(URL).ignoreContentType(true).get();
                //3. Parse the HTML to extract links to other URLs
                Elements linksOnPage = document.select("a[href]");

                //5. For each extracted URL... go back to Step 4.
                for (Element page : linksOnPage) {
                    String url = null;
                    if (links.size() < numberOfurls && robotSafe(url = page.attr("abs:href"))) {
//            		if (links.size() < numberOfurls) {
                        if (isStatic(url))
                            getPageLinks(url, page.attr("title"));
                    } else
                        break;
                    saveLinks();
//                    mostRelevantToWord();
                }
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }
        }
    }

    //Check that the robot exclusion protocol does not disallow downloading url.
    public boolean robotSafe(String url_str) {
        URL url = null;
        try {
            url = new URL(url_str);
        } catch (MalformedURLException e) {
            // something weird is happening, so don't trust it
            return false;
        }
        String strHost = url.getHost();

        // form URL of the robots.txt file
        String strRobot = "http://" + strHost + "/robots.txt";
        URL urlRobot = null;
        try {
            urlRobot = new URL(strRobot);
        } catch (MalformedURLException e) {
            // something weird is happening, so don't trust it
            return false;
        }

        if (DEBUG) System.out.println("Checking robot protocol " +
                urlRobot.toString());

        String strCommands;

        InputStream urlRobotStream = null;

        try {
            urlRobotStream = urlRobot.openStream();

            // read in entire file
            byte b[] = new byte[1000];

            int numRead = 0;
            strCommands = new String(b, 0, numRead);

            while ((numRead = urlRobotStream.read(b)) != -1) {
                strCommands = new String(b, 0, numRead);
                numRead = urlRobotStream.read(b);

                if (numRead != -1) {
                    String newCommand = new String(b, 0, numRead);
                    strCommands += newCommand;
                }
            }

            urlRobotStream.close();

        } catch (IOException e) {
            // if there is no robots.txt file, it is OK to search
            return true;
        }

        if (DEBUG) System.out.println(strCommands);

        // assume that this robots.txt refers to us and search for "Disallow:" commands.
        String strURL = url.getFile();
        int index = 0;

        while ((index = strCommands.indexOf(DISALLOW, index)) != -1) {
            index += DISALLOW.length();
            String strPath = strCommands.substring(index);
            StringTokenizer st = new StringTokenizer(strPath);

            if (!st.hasMoreTokens())
                break;

            String strBadPath = st.nextToken();

            // if the URL starts with a disallowed path, it is not safe
            if (strURL.indexOf(strBadPath) == 0)
                return false;
        }

        return true;
    }

    boolean isStatic(String url) {
        if (url.charAt(url.length() - 1) == '#')
            return false;
        return true;
    }

    // to save html
    public void htmlSaver(String webpage, String title) {
        try {

            // Create URL object 
            URL url = new URL(webpage);
            BufferedReader readr =
                    new BufferedReader(new InputStreamReader(url.openStream()));

            // Enter filename in which you want to download 
            BufferedWriter writer =
                    new BufferedWriter(new FileWriter(folder.getPath() + "/" + title + ".html"));

            BufferedWriter writer2 =
                    new BufferedWriter(new FileWriter(folder.getPath() + "/" + title + ".html"));

            // read each line from stream till end
            String line;
            int count = 0;
            while ((line = readr.readLine()) != null) {
                // find a specific word here
//            	if (main.wantToFindWord) {
//	            	count += findWord(main.word, line);
//	            	foundWords[Integer.valueOf(title.charAt(0)) - 48] = count;
//            	}
                writer.write(line);
                writer2.write(line);


            }

            readr.close();
            writer.close();
            writer2.close();

            System.out.println("Successfully Downloaded.");
        }

        // Exceptions 
        catch (MalformedURLException mue) {
            System.out.println("Malformed URL Exception raised");
        } catch (IOException ie) {
            System.out.println("IOException raised");
        }
    }

//    public static void main(String[] args) {
//        //1. Pick a URL from the frontier
////        new WebCrawler().getPageLinks("https://en.wikipedia.org/");
//        int numberOfThreads = 3;
//        Thread crawlerThreads[] = new Thread[numberOfThreads];
//
//        for (int i = 0; i < numberOfThreads; ++i) {
//            crawlerThreads[i] = new Thread(new WebCrawler("https://en.wikipedia.org/", numberOfurls, this.folder));
//            crawlerThreads[i].start();
//        }
//    }

    @Override
    public void run() {
        getPageLinks(urlLink, "");

    }

    public int findWord(String word, String line) {
        int count = 0;
        boolean isFound = (line.toLowerCase()).contains(word.toLowerCase());
        if (isFound) {
            count++;
        }
        return count;
    }

    public void saveLinks() {
        try {
            BufferedWriter writer =
                    new BufferedWriter(new FileWriter(folder.getPath() + "/" + "URLs.txt"));

            Iterator<String> i = links.iterator();
            while (i.hasNext()) {
                writer.write(i.next() + "\n");
            }
            writer.close();


//            FileWriter myWriter = new FileWriter("URLs.txt");
//            Iterator<String> i = links.iterator();
//            while (i.hasNext())
//                myWriter.write(i.next() + "\n");
//            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

//    public void mostRelevantToWord() {
//        try {
//            FileWriter myWriter = new FileWriter(folder.getPath() + "mostFound.txt");
//            int[] visited = new int[numberOfurls];
//            int min = 0;
//            Data.urlsFromCrawler.clear();
//            for (int j = 0; j < numberOfurls; ++j) {
//                visited[j] = 0;
//                if (foundWords[j] < foundWords[min])
//                    min = j;
//            }
//            for (int j = 0; j < numberOfurls; ++j) {
//                int maxIndex = min;
//                for (int k = 0; k < numberOfurls; ++k) {
//                    if (foundWords[k] > foundWords[maxIndex] && visited[k] == 0)
//                        maxIndex = k;
//                }
//                visited[maxIndex] = 1;
//                myWriter.write(links.toArray()[maxIndex] + "\n");
//                Data.AddFromCrawler(links.toArray()[maxIndex].toString());
//            }
//
//            myWriter.close();
//            System.out.println("Successfully wrote to the file.");
//        } catch (IOException e) {
//            System.out.println("An error occurred.");
//            e.printStackTrace();
//        }
//    }
}

