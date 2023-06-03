package org.jsoup;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.JsonTreeBuilder;

/**
 * 
 * The JsoupJsonTest class is a test class that demonstrates the usage of Jsoup
 * and the JsonTreeBuilder to parse and convert JSON data into HTML using Jsoup.
 * It showcases parsing JSON data from a string, a URL, and a file, and printing
 * the converted Jsoup nodes.
 * Usage:
 * Modify the JSON string, URL, and file path according to your requirements.
 * Run the main method of the JsoupJsonTest class.
 * Note: This class requires the Jsoup library to be included in the classpath.
 */

public class JsoupJsonTest {

    /**
     * Main method to demonstrate the usage of Jsoup and JsonTreeBuilder for JSON to
     * HTML conversion.
     * 
     * @param args command-line arguments
     */

    public static void main(String[] args) {
        String json = "{\"json\": {\"title\": \"Hello\", \"content\": \"World\"}}";

        // Parse JSON data as HTML using Jsoup's Parser
        Document doc = Jsoup.parseBodyFragment(json);

        // Get the body element from the parsed document
        Element bodyElement = doc.body();

        // Print the converted Jsoup nodes
        if (bodyElement != null) {
            System.out.println("Print from html");
            System.out.println(bodyElement.html());
            System.out.println("\n");
        }

        // Parse JSON data from a URL
        String url = "https://filesamples.com/samples/code/json/sample1.json";
        try {
            Document parsedJson = Jsoup.connect(url).ignoreContentType(true).get();
            Element body = parsedJson.body();
            if (body != null) {
                System.out.println("Print from url");
                System.out.println(body.html());
                System.out.println("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Parse JSON from a file
        String filePath = "C:/Users/sonal/OneDrive/Desktop/SAJsoup/jsoup/src/main/java/org/jsoup/test.json";
        try {
            JsonTreeBuilder jsonTreeBuilder = new JsonTreeBuilder();
            Element rootElementFromFile = jsonTreeBuilder.parseJsonFromFile(filePath);
            System.out.println("Parsed JSON from file:");
            System.out.println(rootElementFromFile.toString());
            System.out.println("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
