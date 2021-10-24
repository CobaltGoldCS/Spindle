package com.cobaltware.webscraper.datahandling.webhandlers

import android.util.Log
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*

/** Reads css paths in the format for processing a configuration with a Url, using [customSyntaxAnalyzer]
 * @see customSyntaxAnalyzer
 * @param document The document containing elements disclosed by the paths
 * @param contentPath The css path used to get the text of the book
 * @param prevPath The css path used to get the url of the previous url of the book
 * @param nextPath The css path used to get the url of the next url of the book
 * @return Returns data important to the ui in the order (title, main text content, previous url, next url, current url)
 */
fun csspathReader(
    document: Document, contentPath: String,
    prevPath: String, nextPath: String
): List<String?> {

    val title = document.title()
    val content = customSyntaxAnalyzer(document, contentPath)
    var text = ""
    if (content is Element) {
        try {
            if (content.tagName() == "div")
                content.children().forEach { text += "\n\n     " + it.text() }
            else
                text = content.text()
        } catch (e: NullPointerException) {
            text = "Invalid Content Selector"
        }
    } else if (content is String) // Added because customSyntaxAnalyzer requires it to be explicit
        text = content

    val prevElement = customSyntaxAnalyzer(document, prevPath)
    val nextElement = customSyntaxAnalyzer(document, nextPath)

    val currentUrl = document.location()
    // Check a bunch of conditions and make sure that it is correct
    val prevUrl: String? = processElementIntoUrl(currentUrl, prevElement)
    val nextUrl: String? = processElementIntoUrl(currentUrl, nextElement)

    return listOf(title, text, prevUrl, nextUrl, currentUrl)
}

/** Changes a given element into a url/null
 * @param currentUrl The current url given (we don't want duplicates)
 * @param element The element to translate into a url
 * @return The url, or null if the url was invalid
 * **/
fun processElementIntoUrl(currentUrl: String?, element: Any?): String? {
    // Make sure that the href is an existing location
    val url: String? = if (
        element is Element &&
        element.absUrl("href") !in listOf("#", "")
    ) {
        element.absUrl("href")
    } else {
        if (element is Element) element.toString() else element as String?
    }
    return if (url != currentUrl) url else null
}

/** Analyzes custom syntax defined to get attributes for processing if needed
 * @see csspathReader
 * @param document The document to get the attribute from
 * @param cssPath The css path with the attribute syntax stored in it
 * @return The string from the attribute selected; or null if the attribute is not found in the css path.
 * If the syntax is not found, it will return the normal Element type
 */
fun customSyntaxAnalyzer(document: Document, cssPath: String): Any? {
    // Handle normal csspath syntax
    if (!cssPath.trim().startsWith("$"))
        return document.select(cssPath).firstOrNull()

    // Custom syntax analyzer
    val split = cssPath.trim().split(" ")

    val pathOnly = split.drop(1).joinToString(" ")
    val element = document.select(pathOnly).firstOrNull()

    Log.d("All Attributes", element?.attributes().toString())

    val attrSelector = split[0].drop(1) // Takes away '$'

    return if (attrSelector.lowercase(Locale.ROOT) == "text") element?.text()
    else element?.attr(attrSelector)
}

fun isCssPath(doc: Document, cssPath: String) = customSyntaxAnalyzer(doc, cssPath) != null