package com.cobaltware.webscraper.datahandling.webhandlers

import org.jsoup.nodes.Document

fun csspathReader(document : Document, url : String, contentPath : String,
                  prevPath : String, nextPath :String) : List<String?>
{
    val title = document.title()
    val content = document.select(contentPath).first()
    var text = ""
    if (content.tagName() == "div"){ content.children().forEach { text += "\n" + it.text() } }
    else {text = content.text()}
    
    val prevElement = document.select(prevPath).firstOrNull()
    val prevUrl : String? = if(prevElement != null && prevElement.attr("abs:href") !in listOf("#", ""))
        prevElement.absUrl("href")
    else { null }

    val nextElement = document.select(nextPath).firstOrNull()
    val nextUrl : String? = if(nextElement != null && nextElement.attr("abs:href") !in listOf("#", ""))
        nextElement.absUrl("href")
    else { null }
    return listOf(title, text, prevUrl, nextUrl, url)
}