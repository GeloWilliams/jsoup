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

import java.io.*;
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
            parseCssFile(cssFilePath);
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
    public void parseCss(String css) {
        try {
            InputSource source = new InputSource(new StringReader(css));
            CSSStyleSheet styleSheet = cssParser.parseStyleSheet(source, null, null);
            CSSRuleList rules = styleSheet.getCssRules();

            for (int i = 0; i < rules.getLength(); i++) {
                CSSRule rule = rules.item(i);

                if (rule instanceof CSSStyleRule) {
                    CSSStyleRule styleRule = (CSSStyleRule) rule;
                    String selectorText = styleRule.getSelectorText();
                    CSSStyleDeclaration styleDeclaration = styleRule.getStyle();

                    // Process the selector and style declaration as needed
                    handleParsing(selectorText, styleDeclaration);
                    
                } // end if
            } // end for
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // end parseCssInline

    /* ---------------------------------------------------
       handleParsing
       ---------------------------------------------------
       - helper method for parseCss
       - currently prints the parsed CSS to the console
    */
    private void handleParsing(String selectorText, CSSStyleDeclaration styleDeclaration) {
        System.out.println("Selector: " + selectorText);
        // System.out.println("Parsed CSS: ");

        for (int i = 0; i < styleDeclaration.getLength(); i++) {
            String propertyName = styleDeclaration.item(i);
            String propertyValue = styleDeclaration.getPropertyValue(propertyName);
            System.out.println("   " + propertyName + ": " + propertyValue);
        }
        System.out.println(""); // spacer
    } // end handleParsing

    /* ---------------------------------------------------
       parseCssFile
       ---------------------------------------------------
       - helper method with a .css file parameter
       - relies on cssparser library to handle the low-level
         parsing functionality
    */
    public void parseCssFile(String filePath) {
        File file = new File(filePath);
        try (FileReader reader = new FileReader(file)) {

            // Use cssparser library to parse the CSS file
            InputSource source = new InputSource(reader);
            SelectorList selectorList = cssParser.parseSelectors(source);

            // Process the parser CSS selector list
            for (int i = 0; i < selectorList.getLength(); i++) {
                String selector = selectorList.item(i).toString();
                System.out.println("Parsed selector: " + selector);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // end parseCssFile
    
    /* ---------------------------------------------------
       parseFragment
       ---------------------------------------------------
       - takes a CSS fragment string and converts to InputSource
    */
    public List<Node> parseFragment(String cssFragment, Element context, String baseUri, Parser parser) {
        List<Node> nodes = new ArrayList<>();

        try {
            InputSource source = new InputSource(new StringReader(cssFragment));
            CSSStyleSheet styleSheet = cssParser.parseStyleSheet(source, null, baseUri);
            CSSRuleList rules = styleSheet.getCssRules();

            for (int i = 0; i < rules.getLength(); i++) {
                CSSRule rule = rules.item(i);

                if (rule instanceof CSSStyleRule) {
                    CSSStyleRule styleRule = (CSSStyleRule) rule;
                    String selectorText = styleRule.getSelectorText();
                    CSSStyleDeclaration styleDeclaration = styleRule.getStyle();

                    // handle parsing
                    handleParsing(selectorText, styleDeclaration);

                    // create a new node based on parsed CSS rule and add to list
                    Node node = createNode(styleRule);
                    nodes.add(node);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
