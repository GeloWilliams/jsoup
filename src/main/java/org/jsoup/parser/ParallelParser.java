package org.jsoup.parser;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;



public class ParallelParser {
    private int maxThreads = 3;
    private boolean terminateOnError = false;

    public Map<String, Document> parseDocumentsFromUrls(List<String> urls) {
        Map<String, Document> parsedDocuments = new HashMap<>();

        try {
            ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
            for (String url : urls) {
                executor.submit(() -> {
                    try {
                        System.out.println("Parsing from URL: " + url);
                        Document doc = Jsoup.connect(url).get();
                        parsedDocuments.put(url, doc);
                        Elements links = doc.select("a[href]");
                        processLinks(links);
                    } catch (HttpStatusException e) {
                        handleHttpStatusException(e);
                    } catch (IOException e) {
                        handleIOException(e);
                    }
                });
            }
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return parsedDocuments;
    }

    public Map<File, Document> parseDocumentsFromFiles(List<File> files) {
        Map<File, Document> parsedDocuments = new HashMap<>();
    
        try {
            ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
            for (File file : files) {
                executor.submit(() -> {
                    try {
                        System.out.println("Parsing from file: " + file.getAbsolutePath());
                        Document doc = Jsoup.parse(file, "UTF-8");
                        Elements links = doc.select("a[href]");
                        System.out.println("Before processing links for file: " + file.getAbsolutePath());
                        processLinks(links);
                        System.out.println("After processing links for file: " + file.getAbsolutePath());
                        parsedDocuments.put(file, doc);
                    } catch (IOException e) {
                        handleIOException(e);
                    }
                });
            }
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    
        return parsedDocuments;
    }
    
    
    public Map<String, Document> parseDocumentsFromHtmlStrings(List<String> htmlStrings) {
        Map<String, Document> parsedDocuments = new HashMap<>();

        try {
            ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
            for (String htmlString : htmlStrings) {
                executor.submit(() -> {
                    try {
                        System.out.println("Parsing from HTML string: " + htmlString);
                        Document doc = Jsoup.parse(htmlString);
                        Elements links = doc.select("a[href]");
                        processLinks(links);
                        parsedDocuments.put(htmlString, doc);
                    } catch (IOException e) {
                        handleIOException(e);
                    }
                });
            }
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return parsedDocuments;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public void setTerminateOnError(boolean terminateOnError) {
        this.terminateOnError = terminateOnError;
    }

    private void processLinks(Elements links) {
        System.out.println("Processing links...");
        for (Element link : links) {
            String linkUrl = link.attr("abs:href");
            try {
                System.out.println("Processing link: " + linkUrl);
                Document linkDoc = Jsoup.connect(linkUrl).get();
                String title = linkDoc.title();
                System.out.println("Link: " + linkUrl);
                System.out.println("Title: " + title);
                System.out.println();
            } catch (IOException e) {
                handleIOException(e);
            }
        }
        System.out.println("Finished processing links.");
    }
    

    private void handleHttpStatusException(HttpStatusException e) {
        int statusCode = e.getStatusCode();
        System.out.println("HTTP status error occurred: " + statusCode);
        if (terminateOnError) {
            System.out.println("Terminating program due to HTTP status error.");
            System.exit(1);
        }
    }

    private void handleIOException(IOException e) {
        e.printStackTrace();
    }
}
