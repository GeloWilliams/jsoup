package org.jsoup.parser;

import org.jsoup.nodes.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class ParallelParserTest {
    public static void main(String[] args) {

        
        ParallelParser parser = new ParallelParser(); // Create an instance of ParallelParser
        parser.setMaxThreads(4); // Set the desired number of threads
     
        String[] urls = {
            "https://www.uefa.com/uefachampionsleague/",
             "https://www.realmadrid.com/en"
            // Add more URLs as needed
        };

        List<String> urlList = Arrays.asList(urls); // Convert the array to a List


        Map<String, Document> urlDocuments = parser.parseDocumentsFromUrls(urlList); // Parse the URL documents
        
        List<String> fileNames = Arrays.asList("D:/SA/Real Madrid CF _ Real Madrid CF Oficial Website.html"); // Specify the file names

        // Convert the file names to File objects
        List<File> files = convertFileNamesToFiles(fileNames);

        Map<File, Document> fileDocuments = parser.parseDocumentsFromFiles(files); // Parse the file documents

        // Create a combined map of all parsed documents
       Map<String, Document> allDocuments = new HashMap<>();
        allDocuments.putAll(urlDocuments);

        // Copy the contents of fileDocuments map to allDocuments
        for (Map.Entry<File, Document> entry : fileDocuments.entrySet()) {
            String fileName = entry.getKey().getName();
            Document document = entry.getValue();
            allDocuments.put(fileName, document);
        }

        // Create a StringBuilder to store the combined HTML content
        StringBuilder combinedHtml = new StringBuilder();

        // Access the parsed documents from the map and append their HTML content
        for (String key : allDocuments.keySet()) {
            Document doc = allDocuments.get(key);
            System.out.println("Title for " + key + ": " + doc.title()); // Example: Print the title of each document

            // Append the HTML content of the document to the StringBuilder
            combinedHtml.append(doc.html());
        }

        
    }

    private static List<File> convertFileNamesToFiles(List<String> fileNames) {
        List<File> files = new ArrayList<>();
        for (String fileName : fileNames) {
            File file = new File(fileName);
            files.add(file);
        }
        return files;
    }
}
