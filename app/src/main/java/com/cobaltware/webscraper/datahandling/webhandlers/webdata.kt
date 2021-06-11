package com.cobaltware.webscraper.datahandling.webhandlers

import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.IOException

fun webdata (url : String, contentPath : String,
             prevPath : String, nextPath :String) : List<String?>
{
    val connection : Connection.Response = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0").execute()

    if (connection.statusCode() != 200)
        throw IOException("Connection failed; Error ${connection.statusCode()}")

    val document = connection.parse()
    if (customSyntaxAnalyzer(document, contentPath) != null)
        return csspathReader(document, contentPath, prevPath, nextPath)
    return listOf()
}