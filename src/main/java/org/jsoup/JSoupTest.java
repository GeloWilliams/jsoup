package org.jsoup;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.CssTreeBuilder;

public class JSoupTest {
	public static void main (String[] args) throws IOException {
		
//		getTitle();
//		parseHtmlAndManipulate();
		
//		String css = "h1 { color: red; font-size: 20px; }.some-class { display: none; height: 100vh; }";
//		parseCssString(css); 
		
//		parseCssLink("http://www.hey.com");
		parseCssLink("https://www.bbc.com");
				
	}
	
	public static void getTitle() throws IOException {
		Document doc = Jsoup.connect("https://www.bbc.com/").get();
		String title = doc.title();
				System.out.println(title);
	}
	
	public static void parseHtmlAndManipulate() throws IOException {
		// Parse the document from a URL
        Document doc = Jsoup.connect("https://www.bbc.com/").get();

        // Access and modify elements
        Element heading = doc.selectFirst("h1"); // Select the first <h1> element
        heading.text("New Heading"); // Change the text of the heading element

        Element link = doc.selectFirst("a"); // Select the first <a> element
        link.attr("href", "https://www.google.com"); // Change the href attribute of the link

        // Add a new element
        Element newElement = new Element("div");
        newElement.text("New Element");
        doc.body().appendChild(newElement);

        // Print the modified document
        System.out.println(doc);
	}
	
	public static void parseCssString(String css) throws IOException {
		// Parse CSS styles from string
		CssTreeBuilder cssParser = new CssTreeBuilder();
		cssParser.parseCss(css);
	}
	
	public static void parseCssLink(String url) throws IOException {
		
		// Create document
		Document doc = Jsoup.connect(url).get();
		
		// Create Element
		Element link = doc.selectFirst("link[rel=stylesheet]");
		String cssFilePath = link.attr("href");
		
		// check if path is relative or absolute
		if (cssFilePath.startsWith("/")) {
			cssFilePath = url + cssFilePath;
		}
		
		// Create CSS File Document
		Document cssDoc = Jsoup.connect(cssFilePath).get();
		// Convert cssDoc to a String
		String cssDocStr = cssDoc.html();
		parseCssString(cssDocStr);
//		

	}
}
