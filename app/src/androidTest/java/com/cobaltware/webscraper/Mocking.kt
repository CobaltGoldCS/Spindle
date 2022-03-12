package com.cobaltware.webscraper

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun testDocument(): Document {
    return Jsoup.parse("<!DOCTYPE html><html> <div id=\"test\"> <p class=\"text\">Test text</p><h1>Test Header</h1> </div> </html>")!!
}