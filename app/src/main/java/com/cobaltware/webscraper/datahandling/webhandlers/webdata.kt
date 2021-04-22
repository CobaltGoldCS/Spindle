package com.cobaltware.webscraper.datahandling.webhandlers

import org.jsoup.Jsoup

fun webdata (url : String, contentPath : String,
             prevPath : String, nextPath :String) : List<String?>
{
    val document = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0").get()
    if (customSyntaxAnalyzer(document, contentPath) != null)
        return csspathReader(document, url, contentPath, prevPath, nextPath)
    return listOf()
}