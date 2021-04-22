package com.cobaltware.webscraper.datahandling.webhandlers

import android.util.Log
import androidx.annotation.Nullable
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*

fun csspathReader(document : Document, url : String, contentPath : String,
                  prevPath : String, nextPath :String) : List<String?>
{
    val title = document.title()
    val content = customSyntaxAnalyzer(document, contentPath)
    var text = ""
    if (content is Element)
    {
        try {
            if (content.tagName() == "div"){ content.children().forEach { text += "\n" + it.text() } }
            else {text = content.text()}
        } catch (e : NullPointerException){text = "Invalid Content Selector"}
    }
    else if (content is String) // Added because customSyntaxAnalyzer requires it to be explicit
        text = content
    
    val prevElement = customSyntaxAnalyzer(document, prevPath)
    val prevUrl : String? = if(prevElement is Element && prevElement.attr("abs:href") !in listOf("#", ""))
        prevElement.absUrl("href")
    else { prevElement as String? }

    val nextElement = customSyntaxAnalyzer(document, nextPath)
    val nextUrl : String? = if(nextElement is Element && nextElement.attr("abs:href") !in listOf("#", ""))
        nextElement.absUrl("href")
    else { nextElement as String? }
    return listOf(title, text, prevUrl, nextUrl, url)
}

// Analyzes custom syntax I will define to work for this program
// TODO: Turn into coroutines to speed up performance
fun customSyntaxAnalyzer(document : Document, cssPath : String) : Any?
{
    val split = cssPath.split(" ")
    val first = split[0]
    if (cssPath.startsWith("$"))
    {
        val newPath = split.drop(1).joinToString(" ")
        val element = document.select(newPath).firstOrNull()
        Log.d("All Attributes", element?.attributes().toString())

        val attrSelector = first.substring(1)

        val attr = if (attrSelector.toLowerCase(Locale.ROOT) != "text") element?.attr(attrSelector) else element?.text()

        return if(attr.isNullOrEmpty()) null else attr
    }
    return document.select(cssPath).firstOrNull()
}