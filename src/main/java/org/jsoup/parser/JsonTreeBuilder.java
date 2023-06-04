package org.jsoup.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.internal.InputStreamUtil;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;


/**
 * The JsonTreeBuilder class is a custom implementation of TreeBuilder in the
 * Jsoup library that allows parsing JSON data and converting it into a Jsoup
 * Document object.
 * It provides methods for parsing JSON data from various sources such as a
 * string, a URL, or a file.
 * 
 * Usage:
 * JsonTreeBuilder builder = new JsonTreeBuilder();
 * Element jsonElement =
 * builder.parseJsonFromUrl("https://example.com/data.json");
 * The parsed JSON data is converted into a Jsoup Document object, and the root
 * element is returned.
 * 
 * Note: This class requires the Jsoup and Gson libraries to be included in the
 * classpath.
 */
public class JsonTreeBuilder extends TreeBuilder {
    /**
     * Constructs a new instance of JsonTreeBuilder.
     */
    public JsonTreeBuilder() {
        super();
    }

    /**
     * Returns the default ParseSettings for the JsonTreeBuilder, which preserves
     * the case of tag names.
     * 
     * @return The default ParseSettings for the JsonTreeBuilder.
     */
    @Override
    protected ParseSettings defaultSettings() {
        return ParseSettings.preserveCase;
    }

    /**
     * Creates a new instance of JsonTreeBuilder.
     * 
     * @return A new instance of JsonTreeBuilder.
     */
    @Override
    public TreeBuilder newInstance() {
        return new JsonTreeBuilder();
    }

    /**
     * Processes the given token and converts it into a Jsoup Document object.
     * 
     * @param token The token to process.
     * @return True to continue processing, false otherwise.
     */
    @Override
    public boolean process(Token token) {
        if (token.type == Token.TokenType.StartTag) {
            String tagName = token.tagName;
            if (tagName.equals("json")) {
                String jsonData = token.getData();
                parseJson(jsonData);
            }
        } else if (token.type == Token.TokenType.Character) {
            String jsonFragment = token.getData();
            parseJsonFragment(jsonFragment);
        }
        return true;
    }
    /**
     * Parses a JSON fragment and converts it into a Jsoup Document object.
     * 
     * @param jsonFragment The JSON fragment to parse.
     */
    private void parseJsonFragment(String jsonFragment) {
        JsonElement jsonElement = JsonParser.parseString(jsonFragment);
        convertJsonToJsoup(jsonElement, null);
    }

    /**
     * Parses JSON data and converts it into a Jsoup Document object.
     * 
     * @param jsonData The JSON data to parse.
     */
    private void parseJson(String jsonData) {
        JsonElement jsonElement = JsonParser.parseString(jsonData);
        convertJsonToJsoup(jsonElement, null);
    }

    /**
     * Converts a JSON element into a Jsoup Document object.
     * 
     * @param jsonElement   The JSON element to convert.
     * @param parentElement The parent Jsoup element to append the converted
     *                      elements to.
     */
    private void convertJsonToJsoup(JsonElement jsonElement, Element parentElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String tagName = entry.getKey();
                JsonElement childElement = entry.getValue();
                Element element = new Element(Tag.valueOf(tagName), "");
                if (parentElement != null) {
                    parentElement.appendChild(element);
                }
                convertJsonToJsoup(childElement, element);
            }
        } else if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (JsonElement childElement : jsonArray) {
                convertJsonToJsoup(childElement, parentElement);
            }
        } else if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
            String data = jsonPrimitive.getAsString();
            parentElement.appendChild(new TextNode(data));
        }
    }

    @Override
    public List<org.jsoup.nodes.Node> parseFragment(String input, Element context, String baseUri, Parser parser) {
        Document doc = Jsoup.parseBodyFragment(input, baseUri);
        return doc.body().childNodes();
    }


    /**
     * Parses a JSON string from a URL and returns the root element as a Jsoup
     * Element.
     * 
     * @param url The URL of the JSON data.
     * @return The root element of the parsed JSON as a Jsoup Element, or null if
     *         parsing fails.
     * @throws IOException If an I/O error occurs while fetching or parsing the JSON
     *                     data.
     */
    public Element parseJsonFromUrl(String url) throws IOException {
        Validate.notEmpty(url, "URL must not be empty");

        String jsonData = fetchJsonData(url);
        if (jsonData != null) {
            Document doc = Jsoup.parse(jsonData, "");
            return doc.body();
        }

        return null;
    }

    /**
     * Parses a JSON string from a file and returns the root element as a Jsoup
     * Element.
     * 
     * @param filePath The path to the JSON file.
     * @return The root element of the parsed JSON as a Jsoup Element, or null if
     *         parsing fails.
     * @throws IOException If an I/O error occurs while reading or parsing the JSON
     *                     file.
     */
    public Element parseJsonFromFile(String filePath) throws IOException {
        Validate.notEmpty(filePath, "File path must not be empty");

        String jsonData = fetchJsonFromFile(filePath);
        if (jsonData != null) {
            Document doc = Jsoup.parse(jsonData, "");
            return doc.body();
        }

        return null;
    }


    /**
     * Fetches JSON data from a URL and returns it as a string.
     * 
     * @param url The URL of the JSON data.
     * @return The JSON data as a string, or null if the request fails or the
     *         response code is not HTTP_OK.
     * @throws IOException If an I/O error occurs while fetching the JSON data.
     */
    private String fetchJsonData(String url) throws IOException {
        URL jsonUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) jsonUrl.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream()) {
                return InputStreamUtil.readAllBytes(inputStream);
            }
        }

        return null;
    }

    /**
     * Reads JSON data from a file and returns it as a string.
     * 
     * @param filePath The path to the JSON file.
     * @return The JSON data as a string, or null if an error occurs while reading
     *         the file.
     * @throws IOException If an I/O error occurs while reading the JSON file.
     */
    private String fetchJsonFromFile(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
