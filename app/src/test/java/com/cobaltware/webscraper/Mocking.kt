package com.cobaltware.webscraper

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

fun testDocument(): Document {
    return Jsoup.parse("<html>" +
            "<title>Test Title</title>" +
            "<div>" +
            "<a href=\"https://test.com\">Test</a>" +
            "</div>\n" +
            "<div>\n" +
            "<p class=\"test\">Test text</p>\n" +
            "<h1>Test Header</h1>\n" +
            "</div>" +
            "</html>")!!
}

fun w3cCompliantDocument(): org.w3c.dom.Document = DocumentBuilderFactory.newInstance()
    .newDocumentBuilder().parse(InputSource(StringReader(testDocument().html())))