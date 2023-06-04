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

/**
 * The ParallelParser class is a utility class in the Jsoup library that provides methods for parallel parsing of HTML documents.
 * It allows parsing multiple documents simultaneously using multiple threads, which can improve parsing performance for large datasets.
 *
 * Example Usage:
 * ParallelParser parser = new ParallelParser();
 * List<String> urls = Arrays.asList("https://example.com/page1", "https://example.com/page2", "https://example.com/page3");
 * Map<String, Document> parsedDocuments = parser.parseDocumentsFromUrls(urls);
 *
 * // Process the parsed documents
 * for (Map.Entry<String, Document> entry : parsedDocuments.entrySet()) {
 *     String url = entry.getKey();
 *     Document doc = entry.getValue();
 *     // Perform further processing on the document
 *     // ...
 * }
 */
public class ParallelParser {
    private int maxThreads = 3;
    private boolean terminateOnError = false;

    /**
     * Parses HTML documents from a list of URLs.
     *
     * @param urls The list of URLs to parse.
     * @return A map of parsed documents, where the URL is the key and the parsed Document object is the value.
     */
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

    /**
     * Parses HTML documents from a list of files.
     *
     * @param files The list of files to parse.
     * @return A map of parsed documents, where the File object is the key and the parsed Document object is the value.
     */
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

    /**
     * Parses HTML documents from a list of HTML strings.
     *
     * @param htmlStrings The list of HTML strings to parse.
     * @return A map of parsed documents, where the HTML string is the key and the parsed Document object is the value.
     */
    public Map<String, Document> parseDocumentsFromHtmlStrings(List<String> htmlStrings) {
        Map<String, Document> parsedDocuments = new HashMap<>();

        try {
            ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
            for (String htmlString : htmlStrings) {
                executor.submit(() -> {
                    System.out.println("Parsing from HTML string: " + htmlString);
                    Document doc = Jsoup.parse(htmlString);
                    Elements links = doc.select("a[href]");
                    processLinks(links);
                    parsedDocuments.put(htmlString, doc);
                });
            }
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return parsedDocuments;
    }

    /**
     * Sets the maximum number of threads to use for parsing.
     *
     * @param maxThreads The maximum number of threads.
     */
    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    /**
     * Sets whether to terminate the program on HTTP status errors.
     *
     * @param terminateOnError true to terminate the program on HTTP status errors, false otherwise.
     */
    public void setTerminateOnError(boolean terminateOnError) {
        this.terminateOnError = terminateOnError;
    }

    /**
     * Processes the links extracted from a parsed HTML document.
     *
     * @param links The Elements object containing the links.
     */
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

    /**
     * Handles an HTTP status exception that occurred during parsing.
     *
     * @param e The HttpStatusException object.
     */
    private void handleHttpStatusException(HttpStatusException e) {
        int statusCode = e.getStatusCode();
        System.out.println("HTTP status error occurred: " + statusCode);
        if (terminateOnError) {
            System.out.println("Terminating program due to HTTP status error.");
            System.exit(1);
        }
    }

    /**
     * Handles an IOException that occurred during parsing.
     *
     * @param e The IOException object.
     */
    private void handleIOException(IOException e) {
        e.printStackTrace();
    }
}
