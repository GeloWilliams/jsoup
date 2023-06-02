package org.jsoup.parser;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import com.steadystate.css.parser.CSSOMParser;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.css.sac.SelectorList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
// import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class CssTreeBuilder extends TreeBuilder {

    /* cssParser: the parser imported from 
       the sourceforge cssparser library */
    private CSSOMParser cssParser;
    
    // CssTreeBuilder constructor
    public CssTreeBuilder() {
        cssParser = new CSSOMParser();
    }

    // Create a new instance
    public CssTreeBuilder newInstance() {
        return new CssTreeBuilder();
    } 
 
    /* ---------------------------------------------------
       defaultSettings
       ---------------------------------------------------
       - preserves naming conventions to prevent Jsoup from
         conversion to lowercase during parsing
       - required method inherited from TreeBuilder
    */
    protected ParseSettings defaultSettings() {
        return ParseSettings.preserveCase;
    }

    /* ---------------------------------------------------
       process
       ---------------------------------------------------
       - handles the tokenization of CSS inputs
       - considers general style syntax rules
       - considers comments
       - considers '@' rules
       - considers '@media' queries
       - considers the '!important' keyword
       - required method inherited from TreeBuilder
    */
    public boolean process(Token token) {
        if (token.type == Token.TokenType.StartTag) {
            // CSS specific processing for start <style> tag
            String tagName = token.tagName;
            if (tagName.equals("style")) {
                String attributeValue = token.attributeValue("type");
                if (attributeValue != null && attributeValue.equalsIgnoreCase("text/css")) {
                    // process CSS
                    String inlineCss = token.tagName;
                    parseCss(inlineCss);
                }
            }
        } else if (token.type == Token.TokenType.Css) {
            // CSS file processing
            String cssFilePath = token.tagName;
            try{
             parseCssFile(cssFilePath);
            } catch (Exception e){

            }
        }
        return true;
    } // end process

    /* ---------------------------------------------------
       parseCss
       ---------------------------------------------------
       - helper method with a .css file parameter
       - relies on cssparser library to handle the low-level
         parsing functionality
    */
    public List<Node> parseCss(String css) {
        List<Node> nodes = new ArrayList<>();
        try {
            InputSource source = new InputSource(new StringReader(css));
            CSSStyleSheet styleSheet = cssParser.parseStyleSheet(source, null, null);
            CSSRuleList rules = styleSheet.getCssRules();

            // Process the selector and style declaration as needed
            return handleParsing(rules, nodes);
                    
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return nodes;
    } // end parseCssInline

    /* ---------------------------------------------------
       handleParsing
       ---------------------------------------------------
       - helper method for parseCss
       - currently prints the parsed CSS to the console
    */
    private List<Node> handleParsing(CSSRuleList rules, List<Node> nodes) {
        for (int i = 0; i < rules.getLength(); i++) {
            CSSRule rule = rules.item(i);
            
            // appending
            if (rule instanceof CSSStyleRule) {
                CSSStyleRule styleRule = (CSSStyleRule) rule;
                String selectorText = styleRule.getSelectorText();
                CSSStyleDeclaration styleDeclaration = styleRule.getStyle();

                // create a new node based on parsed CSS rule and add to list
                Node node = createNode(styleRule);
                nodes.add(node);
 
                // printing
                System.out.println("Selector: " + selectorText);
                // System.out.println("Parsed CSS: ");

                for (int j = 0; j < styleDeclaration.getLength(); j++) {
                    String propertyName = styleDeclaration.item(j);
                    String propertyValue = styleDeclaration.getPropertyValue(propertyName);
                    System.out.println("   " + propertyName + ": " + propertyValue);
                }
                System.out.println(""); // spacer
            }
        }
        return nodes;
    } // end handleParsing

    /* ---------------------------------------------------
       parseCssFile
       ---------------------------------------------------
       - helper method with a .css file parameter
       - relies on cssparser library to handle the low-level
         parsing functionality
    */
    public void parseCssFile(String filePath) throws IOException {
        File file = new File(filePath);

        FileReader reader = new FileReader(file);
    
        // Use cssparser library to parse the CSS file
        InputSource source = new InputSource(reader);
        SelectorList selectorList = cssParser.parseSelectors(source);
        // Process the parser CSS selector list
        for (int i = 0; i < selectorList.getLength(); i++) {
            String selector = selectorList.item(i).toString();
            System.out.println("Parsed selector: " + selector);
        }
        reader.close();
        
    } // end parseCssFile
    
    /* ---------------------------------------------------
       parseFragment
       ---------------------------------------------------
       - takes a CSS fragment string and converts to InputSource
       - returns a List<Node>
    */
    public List<Node> parseFragment(String cssFragment, Element context, String baseUri, Parser parser) {
        List<Node> nodes = new ArrayList<>();

        try {
            InputSource source = new InputSource(new StringReader(cssFragment));
            CSSStyleSheet styleSheet = cssParser.parseStyleSheet(source, null, baseUri);
            CSSRuleList rules = styleSheet.getCssRules();

            handleParsing(rules, nodes);
        } catch (Exception e) {
            // e.printStackTrace(); // 
        }

        return nodes;
    } // end parseFragment


    /* ---------------------------------------------------
       createNode
       ---------------------------------------------------
       - helper for parseFragement
       - creates a new Node object based on a styleRule
    */
    private Element createNode(CSSStyleRule styleRule) {
        String selectorText = styleRule.getSelectorText();
        CSSStyleDeclaration styleDeclaration = styleRule.getStyle();

        // create a new Element
        Element element = new Element(Tag.valueOf(selectorText),"");

        // set the selector as the element's ID
        element.attr("id", selectorText);

        // extract and set styles from the CSS style declaration as element attributes
        for (int i = 0; i < styleDeclaration.getLength(); i++) {
            String property = styleDeclaration.item(i);
            String value = styleDeclaration.getPropertyValue(property);
            element.attr(property, value);
        }
        return element;
    } // end createNode
    
}
