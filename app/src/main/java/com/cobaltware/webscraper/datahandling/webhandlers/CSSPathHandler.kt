package com.cobaltware.webscraper.datahandling.webhandlers

import android.util.Log
import androidx.annotation.Nullable
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*

/** Reads css paths in the format for processing a configuration with a Url
 * @see customSyntaxAnalyzer
 * @param document The document containing elements disclosed by the paths
 * @param contentPath The css path used to get the text of the book
 * @param prevPath The css path used to get the url of the previous url of the book
 * @param nextPath The css path used to get the url of the next url of the book
 * @return Returns data important to the ui in the order (title, main text content, previous url, next url, current url)
 */
fun csspathReader(document : Document, contentPath : String,
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
    return listOf(title, text, prevUrl, nextUrl, document.location())
}

/** Analyzes custom syntax defined to get attributes for processing if needed
 * @see csspathReader
 * @param document The document to get the attribute from
 * @param CssPath The css path with the attribute syntax stored in it
 * @return The string from the attribute selected; or null if the attribute is not found in the csspath
 * @return If the syntax is not found, it will return the normal Element type
 */
@Suppress("KDocUnresolvedReference")
fun customSyntaxAnalyzer(document : Document, cssPath : String) : Any?
{
    val split = cssPath.split(" ")
    val first = split[0]
    if (cssPath.trim().startsWith("$"))
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